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

package wenity;

import wenity.modules.common.IWenityModule;
import wenity.modules.dialog.DialogModule;
import wenity.modules.dialog.Notifier;
import wenity.modules.dialog.ProgressIndicator;
import wenity.modules.file.FileSelector;
import wenity.modules.file.FileSystem;
import wenity.modules.file.FileViewer;

public final class ModuleConfig
{
    private ModuleConfig ()
    {
    }

    private static final IWenityModule[] INSTALLED_MODULES = new IWenityModule[]
        {
            new DialogModule (),
            new FileSelector (),
            new FileViewer (),
            new FileSystem (),
            new Notifier (),
            new ProgressIndicator (),
        };

    public static IWenityModule[] getInstalledModules ()
    {
        return INSTALLED_MODULES;
    }
}

