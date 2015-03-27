package org.xmlsh.mustache.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.annotations.Command;
import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ExternalModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.tools.mustache.cli.main.Main;
import org.xmlsh.tools.mustache.cli.main.Main.IOEnv;
import org.xmlsh.tools.mustache.cli.main.Main.UsageException;
import org.xmlsh.util.Util;

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

    @Command(name = "mustache")
    public static class mustache extends XCommand {

        @Override
        public int run(List<XValue> args) throws Exception {

            try {
                new Main(Util.toStringArray(args),
                        new IOEnv() {
                            @Override
                            public PrintWriter getOutput()
                                    throws UnsupportedEncodingException,
                                    CoreException {
                                return getShell().getEnv().getStdout()
                                        .asPrintWriter(getSerializeOpts());
                            }

                            @Override
                            public PrintWriter getErr()
                                    throws UnsupportedEncodingException,
                                    CoreException {
                                return getShell().getEnv().getStderr()
                                        .asPrintWriter(getSerializeOpts());
                            }

                            @Override
                            public Reader getInput() throws UnsupportedEncodingException, CoreException {
                                return getShell().getEnv().getStdin()
                                        .asReader(getSerializeOpts());
                            }

                            @Override
                            public File getFile(String name) throws IOException {
                                return getShell().getFile(name);
                            }

                        }).run();
            } catch (UsageException e) {
                e.write(getShell().getEnv().getStderr()
                        .asPrintStream(getSerializeOpts()));
                return 1;
            }
            return 0;

        }
    }

}
