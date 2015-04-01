package org.xmlsh.modules.datamapping;

import java.util.List;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;

@org.xmlsh.annotations.Module(name="java")
public class DataMappingModule extends PackageModule {

	public DataMappingModule(ModuleConfig config) throws CoreException {
		super(config);
	}
}
