package org.xmlsh.jstache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.xmlsh.tools.mustache.cli.api.MustacheContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionDef;
import org.xmlsh.core.Options.OptionDefs;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.FileInputPort;
import org.xmlsh.core.io.XValueInputPort;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.module.ExternalModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.XTypeFamily;
import org.xmlsh.types.xtypes.XValueProperty;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.util.HtmlEscaper;

@org.xmlsh.annotations.Module
public class Module extends ExternalModule {

    static Logger mLogger = LogManager.getLogger();

    public Module(ModuleConfig config, XClassLoader loader)
            throws CoreException {
        super(config, loader);
        mLogger.entry(config, loader);

    }

    @Override
    public void onInit(Shell shell, List<XValue> args) throws Exception {
        super.onInit(shell, args);
        mLogger.entry(shell, args);
    }

    @Override
    public void onLoad(Shell shell) {
        super.onLoad(shell);
        mLogger.entry(shell);

    }

    /*
     * 
     * public void run() throws IOException {
     * try {
     * getMustacheFactory().setObjectHandler(new JacksonObjectHandler());
     * com.github.mustachejava.Mustache mustache = mf.compile(template,
     * template_name == null ? "main" : template_name , delimStart , delimEnd );
     * mustache.execute(output, getScope());
     * } finally {
     * template.close();
     * output.close();
     * }
     * }
     * 
     * 
     * Opt.option ( "--root" , "-R" ,"--template-dir" ).with("Template directory root"),
     * Opt.option ( "-f", "--template-file").with("Template file (or '-') "),
     * Opt.option ( "-t", "--template","--template-data").with("Template data (inline) "),
     * Opt.option ( "-p", "--properties-file").with("Context read from Java properties file" ),
     * Opt.option ( "-j", "--json-data").with("Context  (inline) as JSON" ),
     * Opt.option ( "-J", "--json-file").with("Context  read from JSON file" ),
     * Opt.option ( "-o", "--output","--output-file").with("Write to output file (or '-')" ),
     * Opt.option ( "-n", "--name").with("Template name"),
     * Opt.option ( "-S", "--delim-start").with("Delmitar start string [default '{{' ]"),
     * Opt.option ( "-E", "--delim-end").with("Delmitar end string [default '}}' ]"),
     * Opt.option ( "--json").with("Use JSON encoded data for variable expansion"),
     * Opt.option ( "--html").with("Use HTML encoded data for variable expansion"),
     * Opt.option ( "-h","--help").with("Help")
     * );f
     */
    
    /*
     * Experimental option defs
     */

    static class Opt { 
      OptionDefs mDef;
      String description;
      Opt( String s,String l){
        mDef = OptionDefs.parseDefs( s+"="+l );
      }
      Opt with( String desc ){
        description = desc ;
        return this ;
      }
      static Opt option( String l,String r ) {
        return new Opt(l,r);
      } 
      OptionDefs def() { return mDef ; }
      public String toString() {
        StringBuilder sb = new StringBuilder();
        for( OptionDef d : mDef ){
          sb.append("-").append(d.getName());
          if( d.getLongname() != null )
            sb.append(" | -").append( d.getLongname() );
          sb.append("\t").append(description);
        }
        return sb.toString();
      }
    };


   static List<Opt>  opts = Arrays.asList(
       Opt.option("R","template-dir:+").with("Template directory root"),
       Opt.option("f","template-file:").with("Template file (or '-') "),
       Opt.option("t","template:").with("Template data (inline)"),
       Opt.option("d","template-data:").with("Template data (inline)"),
       Opt.option("p","properties-file:").with("Context read from Java properties file"),
       Opt.option("j","json-data:+").with("Context  (inline) as JSON"),
       Opt.option("J","json-file:+").with("Context read from JSON file"),
       Opt.option("o","output:").with("Write to output file (or '-')"),
       Opt.option("n","name:").with("Template Name"),
       Opt.option("S","delim-start:").with("Delmitar start string [default '{{' ]"),
       Opt.option("E","delim-end:").with("Delmitar end string [default '}}' ]"),
       Opt.option("json","json-encoding").with("Use JSON encoded data for variable expansion"),
       Opt.option("html","html-encoding").with("Use HTML encoded data for variable expansion"),
       Opt.option("h","help").with("Help")
      );        
   public static class Usage {
     public List<String> message;
     public String header = "Usage: jstache [options] [template] [context]";
     
     Usage( String... msg ){
         message = Arrays.asList(msg);
     }
     
     public void getOptions( Consumer<String> out ) {
        for( Opt o : opts  ){
            out.accept( o.toString() );
        }
      } 
     public void write(Shell sh) {
       message.forEach( sh::printOut );
       sh.printOut(header);
       getOptions( sh::printOut );
   }
   };

 

    @Command(name = "jstache",names={"mustache"})
    public static class jstache extends XCommand {
        @SuppressWarnings("serial")
        private static Options.OptionDefs optDefs = new OptionDefs() { 
          {
            for( Opt o : opts)
              addOptionDefs( o.def() );
          }
        };

        @Override
        public 
        void usage() {
          Usage u = new Usage();
          u.write(getShell());
       }


        private MustacheContext mContext = new MustacheContext( Shell.getCurdir() );
        private boolean bInputUsed = false ;

        private Reader getInputFromFile(XValue v) throws CoreException, IOException{
            	return getShell().getEnv().getInput(v).asReader(getSerializeOpts()); 
        }
        

        // Get a Template file using the context paths
        private Reader getTemplateFromFile(XValue v) throws CoreException, IOException{
            if( v.isAtomic() && v.equals("-")){
                bInputUsed = true ;
            	return getShell().getEnv().getStdin().asReader(getSerializeOpts()); 
            }
            return mContext.getFileReader(v.toString());
            
        }
        
        
        @Override
        public int run(List<XValue> args) throws Exception {

          try {
            Options opts =  new Options( optDefs );
            opts.parse( args );
            args = opts.getRemainingArgs();
            mContext.addTemplateRoot( Shell.getCurdir() );

            for( OptionValue ov : opts.getOpts() ){
 
                String name = ov.getOptionDef().getName() ;
                switch( name   ){
                case "R" : 
                    mContext.addTemplateRoot( getShell().getExplicitFile(  ov.getValue().toString() , true , false )) ;
                    break;

                case "f":
                case "template-file": {
                    mContext.setTemplate(getTemplateFromFile(ov.getValue()));
                    mContext.setTemplate_name(ov.getValue().toString()) ;
                    break;
                }
                case "t":
                case "template":
                    mContext.setTemplate( getReader( ov.getValue()));

                    break;
                case "p":
                case "properties-file":
                    mContext.addPropertiesScope(getInputFromFile(ov.getValue()));
                    
                    break;
                case "j":      
                case "json-data":
                    addJsonScope( ov.getValue() );
                    break;
                case "J":
                case "json-file": 
                  try {
                    mContext.addJsonScope(getInputFromFile(ov.getValue()));
                  } catch (Exception e) {
                    throw new CoreException("Exception parsing JSON from:" + ov.getValue().toString());
                  }
                  break;
                case "n":
                case "name":
                    mContext.setTemplate_name(ov.toStringValue());
                    break;
                case "ov":
                case "output": 
                    mContext.setOutput(getShell().getEnv().getOutput( ov.getValue(), false).asPrintWriter(getSerializeOpts()));
                    break;
                case "S" :
                case "delim-start" :
                    mContext.setDelimStart(ov.toStringValue()); 
                    break;
                case "E" :
                case "delim-end" :
                    mContext.setDelimEnd(ov.toStringValue()); 
                    break ;
                case "json" :
                    break;
                case "html" :
                    mContext.setEncoder((v,w) -> HtmlEscaper.escape(v,w,true));
                    break;
                case "h" : case "help"  :
                    usage();
                    return 1;

                }
            }
            
            // Read name=value pairs 
            for( XValue a : args){
                
                if( a.isAtomic() ){
                    mContext.addStringScope( a.toString());
                }
                else 
                if( a.isJson()){
                    mContext.addJsonScope(a.asJson());
                }
                else
                    mContext.addObjectScope( a.asObject());
                
            }

 
            if (mContext.getTemplate() == null) {
                   usage();
                   return 1;
            }
             if (mContext.getOutput() == null){
                    mContext.setOutput(getShell().getEnv().getStdout().asPrintWriter(getSerializeOpts()));
            }
          }  catch( Exception e ){
            throw mLogger.throwing(e);
            
          }
        try {
                mContext.execute();
        } finally {
          if( mContext.getOutput() != null )
            mContext.getOutput().flush();
        }
            return 0;
        }

        private void addJsonScope(XValue v) throws InvalidArgumentException, JsonProcessingException, IOException {
            if( v.isJson()) 
                mContext.addJsonScope(v.asJson());
            else
              // TODO - other json convertable types
            mContext.addJsonScope(v.toString());            
        }

        private Reader getReader(XValue v) throws CoreException, InvalidArgumentException, IOException {
            return new XValueInputPort(v).asReader(getSerializeOpts());
        }
    }
}
