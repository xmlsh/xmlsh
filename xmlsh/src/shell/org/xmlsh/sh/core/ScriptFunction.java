package org.xmlsh.sh.core;

import java.util.List;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.ModuleHandle;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.StaticContext;

/*
 * 
 */
final class ScriptFunction implements IFunction {
	private String mName;
	private ICommandExpr mBody;
	private ModuleHandle mModule; // containing module

	public ScriptFunction(String name, ICommandExpr body, ModuleHandle module) {
		mName = name;
		mBody = body;
		mModule = module;
	}

	@Override
	public XValue run(Shell shell, SourceLocation loc, List<XValue> args)
			throws Exception {

		return shell.runCommandFunction(mName, mBody, loc, args);

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return mName;
	}

	@Override
	public EvalEnv argumentEnv(EvalEnv parent) {
		return parent.withFlagsSet(EvalEnv.commandArgsFlags());
	}

	@Override
	public EvalEnv returnEnv(EvalEnv parent) {
		return parent.withFlagsMasked(EvalEnv.returnValueMask());
	}

	public ModuleHandle getModule() {
		return mModule;
	}

	public void setModule(ModuleHandle module) {
		this.mModule = module;
	}

}