package org.xmlsh.aws.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.xmlsh.core.XValue;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.internal.MultipleFileTransfer;
import com.amazonaws.services.s3.transfer.internal.MultipleFileTransferMonitor;
import com.amazonaws.services.s3.transfer.internal.MultipleFileUploadImpl;
import com.amazonaws.services.s3.transfer.internal.ProgressListenerChain;
import com.amazonaws.services.s3.transfer.internal.TransferManagerUtils;
import com.amazonaws.services.s3.transfer.internal.TransferProgressImpl;
import com.amazonaws.services.s3.transfer.internal.TransferProgressUpdatingListener;
import com.amazonaws.services.s3.transfer.internal.TransferStateChangeListener;
import com.amazonaws.services.s3.transfer.internal.UploadCallable;
import com.amazonaws.services.s3.transfer.internal.UploadImpl;
import com.amazonaws.services.s3.transfer.internal.UploadMonitor;
import com.amazonaws.util.VersionInfoUtils;

public class S3TransferManager extends TransferManager {

	private ThreadPoolExecutor threadPool;

	/** Thread used for periodically checking transfers and updating their state. */
	private ScheduledExecutorService timedThreadPool = new ScheduledThreadPoolExecutor(1);

	public S3TransferManager(AmazonS3 s3, int threadct) {
		super(s3);
		threadPool = createCustomExecutorService(threadct);
	}

	public S3TransferManager(AmazonS3 s3) {
		super(s3);
		threadPool = createCustomExecutorService(10);
	}

    public static ThreadPoolExecutor createCustomExecutorService(int threadct) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadCount = 1;

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("s3-xmlsh-xfermgr-worker-" + threadCount++);
                return thread;
            }
        };
        return (ThreadPoolExecutor)Executors.newFixedThreadPool(threadct, threadFactory);
    }

	private static final class AllDownloadsQueuedLock {
		private volatile boolean allQueued = false;
	}

	private static final String USER_AGENT = TransferManager.class.getName() + "/" + VersionInfoUtils.getVersion();

	private static final class MultipleFileTransferStateChangeListener implements TransferStateChangeListener {

		private final AllDownloadsQueuedLock allTransfersQueuedLock;
		private final MultipleFileTransfer multipleFileTransfer;

		public MultipleFileTransferStateChangeListener(AllDownloadsQueuedLock allTransfersQueuedLock,
				MultipleFileTransfer multipleFileDownload) {
			this.allTransfersQueuedLock = allTransfersQueuedLock;
			this.multipleFileTransfer = multipleFileDownload;
		}

		@Override
		public void transferStateChanged(Transfer upload, TransferState state) {

			// There's a race here: we can't start monitoring the state of
			// individual transfers until we have added all the transfers to the
			// list, or we may incorrectly report completion.
			synchronized (allTransfersQueuedLock) {
				if ( !allTransfersQueuedLock.allQueued ) {
					try {
						allTransfersQueuedLock.wait();
					} catch ( InterruptedException e ) {
						throw new AmazonClientException("Couldn't wait for all downloads to be queued");
					}
				}
			}

			synchronized (multipleFileTransfer) {
				if ( multipleFileTransfer.getState() == state || multipleFileTransfer.isDone() )
					return;

				/*
				 * If we're not already in a terminal state, allow a transition
				 * to a non-waiting state. Mark completed if this download is
				 * completed and the monitor says all of the rest are as well.
				 */
				if ( state == TransferState.InProgress ) {
					multipleFileTransfer.setState(state);
				} else if ( multipleFileTransfer.getMonitor().isDone() ) {
					multipleFileTransfer.collateFinalState();
				} else {
					multipleFileTransfer.setState(TransferState.InProgress);
				}
			}
		}
	};

	public void shutdownNow() {
		super.shutdownNow();
		threadPool.shutdownNow();
		timedThreadPool.shutdownNow();
	}

	private Upload upload(final PutObjectRequest putObjectRequest, final TransferStateChangeListener stateListener)
			throws AmazonServiceException, AmazonClientException {

		appendUserAgent(putObjectRequest, USER_AGENT);

		if (putObjectRequest.getMetadata() == null)
			putObjectRequest.setMetadata(new ObjectMetadata());
		ObjectMetadata metadata = putObjectRequest.getMetadata();

		if ( TransferManagerUtils.getRequestFile(putObjectRequest) != null ) {
			File file = TransferManagerUtils.getRequestFile(putObjectRequest);

			// Always set the content length, even if it's already set
			metadata.setContentLength(file.length());

			// Only set the content type if it hasn't already been set
			if ( metadata.getContentType() == null ) {
				metadata.setContentType(Mimetypes.getInstance().getMimetype(file));
			}
		}

		String description = "Uploading to " + putObjectRequest.getBucketName() + "/" + putObjectRequest.getKey();
		TransferProgressImpl transferProgress = new TransferProgressImpl();
		transferProgress.setTotalBytesToTransfer(TransferManagerUtils.getContentLength(putObjectRequest));

		ProgressListenerChain listenerChain = new ProgressListenerChain(new TransferProgressUpdatingListener(
				transferProgress), putObjectRequest.getProgressListener());
		putObjectRequest.setProgressListener(listenerChain);

		UploadImpl upload = new UploadImpl(description, transferProgress, listenerChain, stateListener);

		UploadCallable uploadCallable = new UploadCallable(this, threadPool, upload, putObjectRequest, listenerChain);
		UploadMonitor watcher = new UploadMonitor(this, upload, threadPool, uploadCallable, putObjectRequest, listenerChain);
		watcher.setTimedThreadPool(timedThreadPool);
		upload.setMonitor(watcher);

		return upload;
	}

	
	public MultipleFileUpload uploadFileList(String bucketName, String virtualDirectoryKeyPrefix, List<File> files, File workdir, Map<String, String> userMetadata, String storage) throws FileNotFoundException {

		if ( files == null || files.isEmpty() ) {
			throw new IllegalArgumentException("Must provide at least one file to upload");
		}

		if (virtualDirectoryKeyPrefix == null || virtualDirectoryKeyPrefix.length() == 0) {
			virtualDirectoryKeyPrefix = "";
//		} else if ( !virtualDirectoryKeyPrefix.endsWith("/") ) {
//			virtualDirectoryKeyPrefix = virtualDirectoryKeyPrefix + "/";
		}

		TransferProgressImpl transferProgress = new TransferProgressImpl();
		ProgressListener listener = new TransferProgressUpdatingListener(transferProgress);

		List<UploadImpl> uploads = new LinkedList<UploadImpl>();        
		MultipleFileUploadImpl multipleFileUpload = new MultipleFileUploadImpl("Uploading etc", transferProgress, null, virtualDirectoryKeyPrefix, bucketName, uploads);
		multipleFileUpload.setMonitor(new MultipleFileTransferMonitor(multipleFileUpload, uploads));

		final AllDownloadsQueuedLock allTransfersQueuedLock = new AllDownloadsQueuedLock();        
		MultipleFileTransferStateChangeListener stateChangeListener = new MultipleFileTransferStateChangeListener(
				allTransfersQueuedLock, multipleFileUpload);

		long totalSize = 0;

		for (File f : files) {
			ObjectMetadata meta = new ObjectMetadata();
			meta.setUserMetadata( userMetadata );
			
			
			
			long length = f.length();
			totalSize += length;
			meta.setContentLength(length);
			String key = f.getAbsolutePath().substring(findLCS(workdir.getAbsolutePath(), f.getAbsolutePath()))
					.replaceAll("\\\\", "/");
			if(key.startsWith("/")) key = key.substring(1);
			if(virtualDirectoryKeyPrefix.endsWith("/") || virtualDirectoryKeyPrefix.equals(""))
				key = virtualDirectoryKeyPrefix + key;
			else {
				if(!key.contains("/"))
					key = virtualDirectoryKeyPrefix + key;
				else
					key = virtualDirectoryKeyPrefix + key.substring(key.indexOf("/"));
			}
			PutObjectRequest request = new PutObjectRequest(bucketName, key, f);
			if( storage != null )
				request.setStorageClass(storage);
			request.setMetadata( meta);
			uploads.add((UploadImpl) upload(request.withProgressListener(listener),stateChangeListener));
		}

		transferProgress.setTotalBytesToTransfer(totalSize);

		// Notify all state changes waiting for the uploads to all be queued
		// to wake up and continue
		synchronized (allTransfersQueuedLock) {
			allTransfersQueuedLock.allQueued = true;
			allTransfersQueuedLock.notifyAll();
		}

		return multipleFileUpload;
	}
	
	private int  findLCS(String str1, String str2) {
		int i = 0;
		int length = str1.length();
		if (str2.length() < length) length = str2.length();
		while ( (i < length) && (Character.toLowerCase(str1.charAt(i)) == Character.toLowerCase(str2.charAt(i))) ) i++;
		return i;
	}


}
