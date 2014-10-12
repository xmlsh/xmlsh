package org.xmlsh.aws.clients;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.Security;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.xmlsh.aws.util.AWSClient;
import org.xmlsh.aws.util.AWSCommandCredentialsProviderChain;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;
import com.amazonaws.services.s3.transfer.TransferManager;

public final class S3Client extends AWSClient<AmazonS3Client> {
	private TransferManager tm = null;
	private int mThreads = 10 ;


	protected ThreadPoolExecutor createDefaultExecutorService() {
		ThreadFactory threadFactory = new ThreadFactory() {
			private int threadCount = 1;

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName("s3-transfer-manager-worker-" + threadCount++);
				return thread;
			}
		};
		return (ThreadPoolExecutor)Executors.newFixedThreadPool(mThreads, threadFactory);
	}


	public S3Client(Shell shell,Options opts) throws UnsupportedEncodingException, IOException, CoreException {
		super(	new AmazonS3Client(new AWSCommandCredentialsProviderChain( shell , opts ) ));
	
		if( opts.hasOpt("threads"))
			mThreads = opts.getOptInt("threads", mThreads);
		
		ClientConfiguration clientConfig = new ClientConfiguration();
		if( opts.hasOpt("crypt")){
	
			synchronized( AWSS3Command.class  ){
				if( Security.getProperty(BouncyCastleProvider.PROVIDER_NAME) == null ) 
					Security.addProvider(new BouncyCastleProvider());
			}
	
			XValue sKeypair = opts.getOptValueRequired("keypair");
	
	
			KeyPair keyPair = (KeyPair) readPEM(shell.getEnv().getInput(sKeypair), shell.getSerializeOpts(opts));
	
			mClient =  new AmazonS3EncryptionClient(
					new AWSCommandCredentialsProviderChain( shell , opts  ) ,
					new StaticEncryptionMaterialsProvider(
							new EncryptionMaterials( keyPair )),
							clientConfig ,  new CryptoConfiguration() 
	
					);
	
	
		} else
			mClient =  new AmazonS3Client(
					new AWSCommandCredentialsProviderChain( shell, opts  ) ,
					clientConfig 
	
					);
	
		setEndpoint(shell,opts);
		setRegion(shell,opts);
		
		
	}

	private Object readPEM(InputPort in, SerializeOpts sopts ) throws CoreException, IOException  {
		try ( PEMReader reader = new PEMReader( in.asReader( sopts  )) ){
		   Object obj = reader.readObject();
  		   return obj;
		}
	}

	

	public S3Path getPath( String bucket , String key )
	{
		if( Util.isBlank(bucket) )
			return new S3Path( key );
		else
			return new S3Path( bucket , key );
	}



	public CannedAccessControlList getAcl(String acl) {

		for(CannedAccessControlList c : CannedAccessControlList.values())
			if( c.toString().equals(acl))
				return c;
		return null ;

	}

	public int setAcl(S3Path src, String acl) throws CoreException, IOException,
	XMLStreamException, SaxonApiException {


		mClient.setObjectAcl(src.getBucket(),src.getKey(), getAcl(acl));
		return 0;

	}

	public TransferManager getTransferManager() {
		if( tm == null)
			tm  =  new TransferManager( mClient ,  createDefaultExecutorService() );
		return tm;
	}

	public void shutdownTransferManager() {
		if( tm != null)
			tm.shutdownNow();
	}

	
}