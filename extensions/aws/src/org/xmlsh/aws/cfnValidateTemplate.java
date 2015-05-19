/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSCFNCommand;
import org.xmlsh.aws.util.SafeXMLStreamWriter;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;

public class cfnValidateTemplate extends AWSCFNCommand {



    @Override
    public int run(List<XValue> args) throws Exception {



        Options opts = getOptions("template-file=f:,template-url=url:");
        parseOptions(opts, args);

        args = opts.getRemainingArgs();

        setSerializeOpts(this.getSerializeOpts(opts));



        try {
            getCFNClient(opts);
        } catch (UnexpectedException e) {
            usage( e.getLocalizedMessage() );
            return 1;

        }


        int ret = validate(opts );



        return ret;


    }



    private int validate(Options opts) throws IOException, XMLStreamException, SaxonApiException, CoreException {




        ValidateTemplateRequest request = new ValidateTemplateRequest();
        if( opts.hasOpt("template-file"))
            request.setTemplateBody( Util.readString( mShell.getFile(opts.getOptValue("template-file")), getSerializeOpts().getInput_text_encoding()));
        else
            request.setTemplateURL( opts.getOptStringRequired("template-url"));


        traceCall("validateTemplate");

        ValidateTemplateResult result = mAmazon.validateTemplate(request);


        OutputPort stdout = getStdout();
        mWriter = new SafeXMLStreamWriter(stdout.asXMLStreamWriter(getSerializeOpts()));


        startDocument();
        startElement(getName());



        writeResult( result );


        endElement();
        endDocument();
        closeWriter();

        stdout.writeSequenceTerminator(getSerializeOpts());
        stdout.release();

        return 0;

    }


    private void writeResult(ValidateTemplateResult result) throws XMLStreamException {
        startElement("template");

        attribute("capabilities-reason" , result.getCapabilitiesReason());
        attribute("description" , result.getDescription());

        writeTemplateParameters( result.getParameters() );

        writeCapibilities(result.getCapabilities());
        endElement();

    }	

}



/*
 * Copyright (C) 2008-2014   David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */