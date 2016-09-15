package org.xmlsh.modules.text.regex;

import java.io.IOException;
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
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnknownOption;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.modules.text.TextLineReplaceCommand;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

@Command(name="replace",names={"regex-replace"})
public class replace extends TextLineReplaceCommand
{
	protected Pattern mPattern ;
	protected List<XValue> parseOpts(List<XValue> args) throws UnexpectedException, InvalidArgumentException, UnknownOption, IOException 
	{
		args = parseOpts(args);
        mPattern = Pattern.compile(mSearch);
		return args ;

	}
	protected String replaceLine(String line) {
		return mPattern.matcher(line).replaceAll(mReplace);
	}

}
