package org.xmlsh.jstache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
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
import org.xmlsh.tools.mustache.cli.api.JacksonObjectHandler;
import org.xmlsh.tools.mustache.cli.api.MustacheContext;
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
    @Command(name = "jstache",names={"mustache"})
    public static class jstache extends XCommand {
        private static Options.OptionDefs optDefs = Options.OptionDefs
                .parseDefs(
                        "R=template-dir:+",
                        "f=template-file:",
                        "t=template:",
                        "d=template-data:",
                        "p=properties-file:",
                        "j=json-data:+",
                        "J=json-file:+",
                        "o=output:",
                        "n=name:",
                        "S=delim-start:",
                        "E=delim-end:",
                        "json=json-encoding",
                        "html=html-encoding"
                        );

        private MustacheContext mContext = new MustacheContext( Shell.getCurdir() );
        private boolean bInputUsed = false ;

        private Reader getInputFromFile(XValue v) throws CoreException, IOException{
            if( v.isAtomic() && v.equals("-")){
                bInputUsed = true ;
            	return getShell().getEnv().getStdin().asReader(getSerializeOpts()); 
            }
            return mContext.getFileReader(v.toString());
            
        }
        @Override
        public int run(List<XValue> args) throws Exception {

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
                    mContext.setTemplate(getInputFromFile(ov.getValue()));
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
                    mContext.addJsonScope(getInputFromFile( ov.getValue()));
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
                    break;

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
                   throw new InvalidArgumentException("No template specified");
            }
             if (mContext.getOutput() == null){
                    mContext.setOutput(getShell().getEnv().getStdout().asPrintWriter(getSerializeOpts()));
            }

        try {
                mContext.execute();
        } finally {
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

        private Reader getReader(XValue v) throws UnsupportedEncodingException,
        CoreException, InvalidArgumentException {
            return new XValueInputPort(v).asReader(getSerializeOpts());
        }
    }

}
