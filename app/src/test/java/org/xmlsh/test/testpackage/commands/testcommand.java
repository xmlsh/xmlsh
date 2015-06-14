package org.xmlsh.test.testpackage.commands;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import java.io.PrintWriter;
import java.util.List;



public class testcommand extends XCommand {
  /*
   * TODO: xcat should use the collection() instead of doc()
   * so that ports can be used as filenames instead of just files or URI's
   */



  @Override
  public int run( List<XValue> args ) throws Exception
  {
    
    OutputPort stdout = getStdout();
    try ( 
        PrintWriter w = stdout.asPrintWriter(getSerializeOpts()) ){
      
      for( XValue a : args )
        w.write(a.toString());
    }
    
    return 0;
  }
}