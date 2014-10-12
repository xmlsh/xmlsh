package org.xmlsh.aws.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.xmlsh.aws.clients.ASClient;
import org.xmlsh.aws.clients.CFNClient;
import org.xmlsh.aws.clients.CloudWatchClient;
import org.xmlsh.aws.clients.DDBClient;
import org.xmlsh.aws.clients.EC2Client;
import org.xmlsh.aws.clients.ELBClient;
import org.xmlsh.aws.clients.GlacierClient;
import org.xmlsh.aws.clients.S3Client;
import org.xmlsh.aws.clients.SDBClient;
import org.xmlsh.aws.clients.SNSClient;
import org.xmlsh.aws.clients.SQSClient;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.Shell;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class AWSClientFactory {

	public static ASClient newASClient(Shell shell, Options opts) throws InvalidArgumentException {
		return new ASClient(  shell , opts );

	}
	public static CFNClient newCFNClient(Shell shell, Options opts) throws InvalidArgumentException {
		return new CFNClient(  shell , opts );

	}
	public static DDBClient newDDBClient(Shell shell, Options opts) throws InvalidArgumentException {
		return new DDBClient(shell,opts);
	}
	
	public static SDBClient newSDBClient(Shell shell, Options opts) throws InvalidArgumentException {
		return new SDBClient(shell,opts);
	}
	
	public static EC2Client newEC2lient(Shell shell, Options opts) throws InvalidArgumentException {
		return new EC2Client(shell,opts);
	}
	public static AWSClient<AmazonElasticLoadBalancingClient> newELBClient(
			Shell shell, Options opts) throws InvalidArgumentException {
		return new ELBClient( shell , opts );
	}
	public static AWSClient<AmazonGlacierClient> newGlacierClient(Shell shell,
			Options opts) throws InvalidArgumentException {
		 return new GlacierClient( shell , opts );
	}
	public static AWSClient<AmazonCloudWatchClient > newCloudWatchClient(Shell shell,
			Options opts) throws InvalidArgumentException {
		 return new CloudWatchClient( shell , opts );
	}
	public static AWSClient<AmazonS3Client>  newS3Client(Shell shell, Options opts) throws UnsupportedEncodingException, IOException, CoreException {
		return new S3Client( shell , opts );
	}
	
	
	public static AWSClient<AmazonSNSClient> newSNSClient(Shell shell,
			Options opts) throws InvalidArgumentException {
		 return new SNSClient( shell , opts );
	}
	
	public static AWSClient<AmazonSQSClient> newSQSClient(Shell shell,
			Options opts) throws InvalidArgumentException {
		 return new SQSClient( shell , opts );
	}
	
}
