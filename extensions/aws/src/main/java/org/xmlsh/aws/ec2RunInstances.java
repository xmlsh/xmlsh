package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSEC2Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import net.sf.saxon.s9api.SaxonApiException;

public class ec2RunInstances extends AWSEC2Command {

  /**
   * @param args
   * @throws IOException
   *
   *           ec2-run-instances ami_id [-n instance_count] [-g group [-g group
   *           ...]]
   *           [-k keypair] [-d user_data |-f user_data_file]
   *           [-instance-type instance_type] [-availability-zone zone] [-kernel
   *           kernel_id]
   *           [-ramdisk ramdisk_id] [-block-device-mapping
   *           block_device_mapping] [-monitor]
   *           [-disable-api-termination] [-instance-initiated-shutdown-behavior
   *           behavior]
   *           [-placement-group placement-group] [-tenancy tenancy] [-subnet
   *           subnet]
   *           [-private-ip-address ip_address] [-client-token token]
   *           [-additional-info info]
   *           [-dry-run]
   * 
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions(
        "ami=image:,n:,g=group:+,group-id:+,k=key:,d=user-data:,f=user-data-file:,t=instance-type:,availability-zone:,"
            +
            "kernel:,ramdisk:,block-device-mapping:+,monitor,disable-api-termination,instance-initiated-shutdown-behavior:,"
            +
            "placement-group:,tenancy:,subnet:,private-ip-address:,client-token:,additional-info:,tag:+,ebs=ebs-optimized",
        "dry-run:q,network=network-interface:+");
    parseOptions(opts, args);

    args = opts.getRemainingArgs();
    setSerializeOpts(this.getSerializeOpts(opts));

    String image = opts.getOptString("image",
        (args.isEmpty() ? null : args.get(0).toString()));

    if(image == null) {
      usage(null);
      return 1;
    }

    try {
      getEC2Client(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    int ret = runInstance(args.get(0).toString(), opts);

    return ret;
  }

  private int runInstance(String ami_id, Options opts)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {
    /*
     * ec2-run-instances ami_id [-n instance_count] [-g group [-g group ...]]
     * [-k keypair] [-d user_data |-f user_data_file]
     * [-instance-type instance_type] [-availability-zone zone] [-kernel
     * kernel_id]
     * [-ramdisk ramdisk_id] [-block-device-mapping block_device_mapping]
     * [-monitor]
     * [-disable-api-termination] [-instance-initiated-shutdown-behavior
     * behavior]
     * [-placement-group placement-group] [-tenancy tenancy] [-subnet subnet]
     * [-private-ip-address ip_address] [-client-token token]
     * [-network-interface ...]
     * 
     * 
     */

    StringPair minMax = new StringPair(opts.getOptString("n", "1"), '-');

    int min = minMax.hasDelim() ? Util.parseInt(minMax.getLeft(), 1)
        : Util.parseInt(minMax.getRight(), 1);
    int max = minMax.hasDelim() ? Util.parseInt(minMax.getRight(), min) : min;

    RunInstancesRequest request = new RunInstancesRequest(ami_id, min, max);

    // -g group
    List<XValue> xgroups = opts.getOptValues("group");
    if(xgroups != null)
      request.setSecurityGroups(Util.toStringList(xgroups));
    // -g group
    List<XValue> xgroupids = opts.getOptValues("group-id");
    if(xgroupids != null)
      request.setSecurityGroupIds(Util.toStringList(xgroupids));

    List<XValue> nifs = opts.getOptValues("network-interface");
    if(nifs != null)
      request.setNetworkInterfaces(parseNetworkInterfaces(nifs));

    // -k keypair
    String keypair = opts.getOptString("k", null);
    if(keypair != null)
      request.setKeyName(keypair);

    String userData = getUserData(opts);
    if(userData != null)
      request.setUserData(userData);

    String instanceType = opts.getOptString("instance-type", null);
    if(instanceType != null)
      request.setInstanceType(instanceType);

    Placement placement = getPlacement(opts);
    if(placement != null)

      request.setPlacement(placement);

    String kernel = opts.getOptString("kernel", null);
    if(kernel != null)
      request.setKernelId(kernel);

    String ramdisk = opts.getOptString("ramdisk", null);
    if(ramdisk != null)
      request.setRamdiskId(ramdisk);

    Collection<BlockDeviceMapping> blockDeviceMappings = getBlockDeviceMappings(
        opts);
    if(blockDeviceMappings != null)
      request.setBlockDeviceMappings(blockDeviceMappings);

    request.setMonitoring(Boolean.valueOf(opts.hasOpt("monitor")));
    request.setDisableApiTermination(
        Boolean.valueOf(opts.hasOpt("disable-api-termination")));
    String shutdown = opts.getOptString("instance-initiated-shutdown-behavior",
        null);
    if(shutdown != null)
      request.setInstanceInitiatedShutdownBehavior(shutdown);

    String subnet = opts.getOptString("subnet", null);
    if(subnet != null)
      request.setSubnetId(subnet);

    String private_ip = opts.getOptString("private-ip-address", null);
    if(private_ip != null)
      request.setPrivateIpAddress(private_ip);

    String client_token = opts.getOptString("client-token", null);
    if(client_token != null)
      request.setClientToken(client_token);
    String info = opts.getOptString("additional-info", null);
    if(info != null)
      request.setAdditionalInfo(info);

    RunInstancesResult result;

    if(isDryRun(request))
      return dryRun(request);

    try {

      result = getAWSClient().runInstances(request);

    } catch (AmazonClientException e) {
      return handleException(e);
    }

    startResult();
    writeResult(result);
    endResult();

    return 0;
  }

  private Collection<InstanceNetworkInterfaceSpecification> parseNetworkInterfaces(
      List<XValue> nifs) throws InvalidArgumentException, UnexpectedException {

    List<InstanceNetworkInterfaceSpecification> nis = new ArrayList<>();
    for(XValue xni : nifs) {
      if(xni.canConvert(InstanceNetworkInterfaceSpecification.class) < 0)
        throw new UnexpectedException(
            "InstanceNetworkInterfaceSpecification expected, got: "
                + xni.describe());

      nis.add(xni.convert(InstanceNetworkInterfaceSpecification.class));
    }
    return nis;

  }

  private void writeResult(RunInstancesResult result)
      throws IOException, XMLStreamException, SaxonApiException, CoreException {
    OutputPort stdout = this.getStdout();
    mWriter = new SafeXMLStreamWriter(
        stdout.asXMLStreamWriter(getSerializeOpts()));

    startDocument();
    startElement(this.getName());

    writeReservation(result.getReservation());

    endElement();
    endDocument();
    closeWriter();

    stdout.writeSequenceTerminator(getSerializeOpts());

  }

  private Placement getPlacement(Options opts) {
    Placement placement = new Placement();
    String availZone = opts.getOptString("availability-zone", null);
    if(availZone != null)
      placement.setAvailabilityZone(availZone);
    String pg = opts.getOptString("placement-group", null);
    if(pg != null)
      placement.setGroupName(pg);

    String tenancy = opts.getOptString("tenancy", null);
    if(tenancy != null)
      placement.setTenancy(tenancy);

    return placement;

  }

  private String getUserData(Options opts) throws IOException {
    String userData = opts.getOptString("d", null);
    if(userData != null)
      return userData;

    XValue file = opts.getOptValue("f");
    if(file != null)
      return Util.readString(mShell.getFile(file),
          getSerializeOpts().getInputTextEncoding());
    return null;

  }

}
