package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Volume;


public class ec2DescribeVolumes extends AWSEC2Command {




    /**
     * @param args
     * @throws IOException 
     */
    @Override
    public int run(List<XValue> args) throws Exception {


        Options opts = getOptions();
        parseOptions(opts, args);

        args = opts.getRemainingArgs();
        parseCommonOptions(opts);

        setSerializeOpts(this.getSerializeOpts(opts));
        try {
            getEC2Client(opts);
        } catch (UnexpectedException e) {
            usage( e.getLocalizedMessage() );
            return 1;
        }

        int ret;
        switch(args.size()){
        case	0:
            ret = describe(null);
            break;
        default:
            ret = describe(args);
            break;
        }

        return ret;
    }


    private int describe(List<XValue> args) throws IOException, XMLStreamException, SaxonApiException, CoreException, InterruptedException {
        OutputPort stdout = getStdout();
        mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));
        startDocument();
        startElement(getName());

        DescribeVolumesRequest  request =  new DescribeVolumesRequest(); ;
        if( args != null ){
            request.setVolumeIds(Util.toStringList(args));

        }
        DescribeVolumesResult result =null ;

        int retry = rateRetry ;
        int delay = retryDelay ;
        do {
            try {

                result =  mAmazon.describeVolumes(request); 
                break ;
            } catch( AmazonServiceException e ){
                mShell.printErr("AmazonServiceException" , e );
                if( retry > 0 && Util.isEqual("RequestLimitExceeded",e.getErrorCode())){
                    mShell.printErr("AWS RequestLimitExceeded - sleeping " + delay );
                    Thread.sleep( delay );
                    retry--;
                    delay *= 2 ;
                }
                else
                    throw e;
            }
        } while( retry > 0 );

        if( args != null ){
            request.setVolumeIds(Util.toStringList(args));

        }

        traceCall("describeVolumes");

        for( Volume  volume : result.getVolumes() ){
            writeVolume(volume);
            endElement();
        }

        endElement();
        endDocument();
        closeWriter();

        stdout.writeSequenceTerminator(getSerializeOpts());
        stdout.release();

        return 0;

    }


}
