package org.xmlsh.sh.module;

import java.util.List;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IXFunction;
import org.xmlsh.core.XFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.shell.Shell;

/*
 * 
 */
public class ScriptFunction extends XFunction {
	private ICommandExpr mBody;
	private IModule mModule; // containing module

	public ScriptFunction(String name, ICommandExpr body, IModule module) {
	    super(name);
		mBody = body;
		mModule = module;
	}

	@Override
	public XValue run(Shell shell, List<XValue> args)
			throws Exception {

		return shell.runCommandFunction(getName(), mBody, args);

	}

	@Override
	public EvalEnv argumentEnv(EvalEnv parent) {
		return parent.withFlagsSet(EvalEnv.commandArgsFlags());
	}

	@Override
	public EvalEnv returnEnv(EvalEnv parent) {
		return parent.withFlagsMasked(EvalEnv.returnValueMask());
	}

	@Override
	public IModule getModule() {
		return mModule;
	}


}