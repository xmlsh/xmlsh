package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSS3Command;
import org.xmlsh.aws.util.S3Path;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class s3ls extends AWSS3Command {

	

	private boolean bLongListing;
	private String mDelim;


	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		
		Options opts = getOptions("delim:,r=recurse,l=long");
		opts.parse(args);

		args = opts.getRemainingArgs();
		

		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		
		
		bLongListing = opts.hasOpt("l");
		
		
		try {
			 getS3Client(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		mDelim = opts.getOptString("delim", "/" );
		if( opts.hasOpt("r") )
			mDelim = null ;


		int ret;
		switch(args.size()){
		case	0:
			ret = listBuckets();
			break;
		case	1:
			ret = list( args.get(0).toString());
			break;
			
		default :
				usage();
				return 1;
		}


		
		
		return ret;
		
		
	}


	private int listBuckets() throws IOException, XMLStreamException, SaxonApiException, CoreException {
		

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		startDocument();
		startElement("s3ls");
		
		List<Bucket> buckets = mAmazon.listBuckets();
		for( Bucket  bucket : buckets ){
			startElement("bucket");
			attribute("name", bucket.getName());
			if( bLongListing ) {
				attribute("create-date",Util.formatXSDateTime(bucket.getCreationDate()));
				attribute("owner", bucket.getOwner().getDisplayName());
				
			}
			
			endElement();
			
		}
		
		
		
		
		
		endElement();
		endDocument();
		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		

		
		
		return 0;
		
		
		
		
	}


	private int list(String s) throws IOException, XMLStreamException,
			SaxonApiException, AmazonClientException, AmazonServiceException, CoreException {
		
		
		S3Path path = new S3Path(s);
		if( ! path.hasBucket()){
			usage();
			return 1;
			
		}
		
			
			
		
		
		OutputPort stdout = this.getStdout();
		XMLStreamWriter writer = stdout.asXMLStreamWriter(mSerializeOpts);
		
		
		writer.writeStartDocument();
		writer.writeStartElement(getName());
		writer.writeAttribute("bucket" , path.getBucket() );
		writer.writeAttribute("prefix", Util.notNull(path.getPrefix()));
		writer.writeAttribute("delim", Util.notNull(mDelim) );
		
		
		
		
		ListObjectsRequest request = getListRequest( path ,mDelim );
		ObjectListing list = mAmazon.listObjects(request);
		
		
		
		do {
			
			List<String> prefixes = list.getCommonPrefixes();
			if( prefixes != null && prefixes.size() > 0 ){
				for( String p : prefixes ){
					writer.writeStartElement("directory");
					writer.writeAttribute("name",p);
					writer.writeEndElement();
				}
				
			}
			
			List<S3ObjectSummary>  objs = list.getObjectSummaries();
			for ( S3ObjectSummary obj : objs ){
				writer.writeStartElement("file");
				writer.writeAttribute("key",obj.getKey());
				if( bLongListing ){
					
					writer.writeAttribute("size", Long.toString(obj.getSize() ));
					writer.writeAttribute("md5" , obj.getETag() );
					writer.writeAttribute("mod-date", Util.formatXSDateTime(obj.getLastModified()) );
					writer.writeAttribute("owner", obj.getOwner().getDisplayName());
					writer.writeAttribute("storage-class" , obj.getStorageClass() );
				}
				writer.writeEndElement();
				
			}
			if( list.isTruncated()){
				// String marker = list.getNextMarker();
				list = mAmazon.listNextBatchOfObjects(list);
			}
			else
				break;
		} while( true );
		
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		return 0;
	}


	public void usage() {
		super.usage("Usage: s3ls [options] [bucket/prefix]");
	}



	

}
