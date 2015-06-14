/*
Wenity v1.5 - a Zenity clone written in Java

Copyright (c) 2012, 2013  Karoly Kalman  http://kksw.zzl.org/

This file is part of Wenity v1.5.

Wenity v1.5 is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Wenity v1.5 is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wenity v1.5.  If not, see <http://www.gnu.org/licenses/>.

*/

package wenity.modules.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

final public class ModuleRequest
{
    private final String moduleName;
    private final List<String> moduleParams;

    public ModuleRequest (String moduleName, List<String> moduleParams)
    {
        this.moduleName = moduleName.toLowerCase ();
        this.moduleParams = moduleParams;
    }

    public ModuleRequest (String moduleName, String[] moduleParams)
    {
        this (moduleName, Arrays.asList (moduleParams));
    }

    public Iterator<String> getModuleParams ()
    {
        return moduleParams.iterator ();
    }

    public String getModuleName ()
    {
        return moduleName;
    }

    @Override
    public String toString ()
    {
        return "ModuleRequest{" +
            "moduleName='" + moduleName + '\'' +
            ", moduleParams=" + moduleParams +
            '}';
    }
}