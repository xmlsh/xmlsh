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

public abstract class AWenityModule implements IWenityModule
{
    protected final String moduleName;
    protected final String lowCaseModuleName;

    protected AWenityModule (final String moduleName)
    {
        lowCaseModuleName = moduleName.toLowerCase ();
        this.moduleName = moduleName;
    }

    @Override
    public String getModuleName ()
    {
        return moduleName;
    }

    @Override
    public boolean canProcess (final String lowCaseModuleName)
    {
        return this.lowCaseModuleName.equals(lowCaseModuleName);
    }

}
