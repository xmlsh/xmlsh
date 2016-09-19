package org.xmlsh.internal.commands;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class csv2xmlTest {
  static Shell shell;
  csv2xml command;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    shell = new Shell();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
   shell.close();
  }

  @Before
  public void setup() {
    command = new csv2xml();

  }
  //@Test
  public void testRun() throws Exception {

    List<XValue> args = new ArrayList<XValue>();
    args.add( XValue.newXValue("-header"));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    shell.getEnv().setInput("input", new ByteArrayInputStream(
        "Col A,Col(b),3rd Col_umn#\nA,b,C\n1,\"two and\" , \"  three\"  ".getBytes("utf8")));
    shell.getEnv().setOutput("output", out);

    command.run( shell,"csv2xml",args );

  }

}
