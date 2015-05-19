package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;


public class ec2DeleteTags extends AWSEC2Command {




    /**
     * @param args
     * @throws IOException 
     */
    @Override
    public int run(List<XValue> args) throws Exception {


        Options opts = getOptions("t=tag:+");
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

        traceCall("deleteTags");

        int ret = deleteTags( parseTags(opts.getOptValuesRequired("tag")) , Util.toStringArray(args) );





        return ret;


    }





}
