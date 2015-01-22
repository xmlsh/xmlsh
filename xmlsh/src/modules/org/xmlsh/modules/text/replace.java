package org.xmlsh.modules.text;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

@Command(name="replace",names={"string-replace"})
public class replace extends XCommand
{

    @Override
    public int run( List<XValue> args) throws Exception {
        Options opts = new Options("i=input:,o=output:", SerializeOpts.getOptionDefs());
        opts.parse(args);
        args = opts.getRemainingArgs();
        if( ! requires( args.size() == 2 , "[-input file] [-output file] search replace") )
            return 1;
        
        
        String search = args.get(0).toString();
        String repl = args.get(1).toString();
        
        InputPort in = opts.hasOpt("i") ? 
                getShell().getEnv().getInput(opts.getOptValue("i")) : getShell().getEnv().getStdin() ;
        OutputPort out = opts.hasOpt("o") ? 
                getShell().getEnv().getOutput(opts.getOptValue("o"),false) : getShell().getEnv().getStdout() ;

        Pattern p = Pattern.compile(search);

        try ( Reader r = in.asReader(getSerializeOpts()) ;
              PrintWriter w = out.asPrintWriter(getSerializeOpts()) ){
            
            String line;
            while( (line=Util.readLine(r)) != null) {
                line = p.matcher(line).replaceAll(repl);
                w.println(line);
                
            }
        }
        return 0;

        
    }

}
