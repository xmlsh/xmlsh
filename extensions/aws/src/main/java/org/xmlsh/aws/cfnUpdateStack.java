/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSCFNCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.Util;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.UpdateStackRequest;
import com.amazonaws.services.cloudformation.model.UpdateStackResult;
import net.sf.saxon.s9api.SaxonApiException;

public class cfnUpdateStack extends AWSCFNCommand {

  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions(
        "capability:+,name:,template-file=f:,template-url=url:");
    parseOptions(opts, args);

    args = opts.getRemainingArgs();

    setSerializeOpts(this.getSerializeOpts(opts));

    try {
      getCFNClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret = updateStack(args, opts);

    return ret;

  }

  private int updateStack(List<XValue> args, Options opts)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {

    OutputPort stdout = getStdout();
    mWriter = new SafeXMLStreamWriter(
        stdout.asXMLStreamWriter(getSerializeOpts()));

    startDocument();
    startElement(getName());

    UpdateStackRequest request = new UpdateStackRequest();

    // "capability:+,disable-rollback,notification-arn:+,name:,template:,timeout:,tag:+");

    if(opts.hasOpt("capability"))
      request
          .setCapabilities(Util.toStringList(opts.getOptValues("capability")));

    request.setStackName(opts.getOptStringRequired("name"));

    if(opts.hasOpt("template-file"))
      request.setTemplateBody(
          Util.readString(mShell.getFile(opts.getOptValue("template-file")),
              getSerializeOpts().getInput_text_encoding()));
    else
      request.setTemplateURL(opts.getOptStringRequired("template-url"));

    request.setParameters(getParameters(args));

    traceCall("updateStack");

    UpdateStackResult result = getAWSClient().updateStack(request);

    writeStackResult(result);

    endElement();
    endDocument();
    closeWriter();

    stdout.writeSequenceTerminator(getSerializeOpts());

    return 0;

  }

  private Collection<Parameter> getParameters(List<XValue> args) {
    int sz = args.size();
    Collection<Parameter> params = new ArrayList<Parameter>(sz);
    for(int i = 0; i < sz; i += 2) {
      String name = args.get(i).toString();
      String value = args.get(i + 1).toString();
      params.add(
          new Parameter().withParameterKey(name).withParameterValue(value));

    }
    return params;

  }

  private void writeStackResult(UpdateStackResult result)
      throws XMLStreamException {
    startElement("stack");
    attribute("stack-id", result.getStackId());
    endElement();

  }

}

/*
 * Copyright (C) 2008-2014 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */
