package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.ec2.model.DeleteVolumeRequest;


public class ec2DeleteVolumes extends AWSEC2Command {




    /**
     * @param args
     * @throws IOException 
     */
    @Override
    public int run(List<XValue> args) throws Exception {


        Options opts = getOptions("");
        parseOptions(opts, args);

        setSerializeOpts(this.getSerializeOpts(opts));
        args = opts.getRemainingArgs();


        if( args.size() < 1 ){
            usage(null);
            return 1;
        }

        try {
            getEC2Client(opts);
        } catch (UnexpectedException e) {
            usage( e.getLocalizedMessage() );
            return 1;

        }

        traceCall("deleteVolumes");

        int ret = deleteVolumes( Util.toStringArray(args) );





        return ret;


    }






    private int deleteVolumes(  String[] resources ) throws InvalidArgumentException, IOException, XMLStreamException, SaxonApiException 
    {



        for( String volid : resources ){

            DeleteVolumeRequest deleteVolumeRequest  = new DeleteVolumeRequest(volid);
            mAmazon.deleteVolume(deleteVolumeRequest);

        }



        return 0;



    }





}
