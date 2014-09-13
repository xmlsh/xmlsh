package org.xmlsh.sh.shell;

import org.xmlsh.core.ReferenceCountedHandle;

public class ModuleHandle extends ReferenceCountedHandle<IModule>  {

	public ModuleHandle(IModule mod) {
		super(mod);
	}

	public String getName() {
		return get().getName();
	}


}