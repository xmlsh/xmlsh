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

// Feb 26, 2013
package wenity.modules.file;

import wenity.Constants;
import wenity.Logger;
import wenity.Utils;
import wenity.modules.common.AWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import java.io.File;
import java.util.Iterator;

/**
 * Module name:             fileSystem                          <br>
 * Function:                get file system information         <br>
 * Accepted parameters:     operation_name      full_path           [req_free_space_in_mb]                          <br>
 * _                          hasFreeSpace     drive_spec                                   req_free_space_in_mb    <br>
 * _                          pathExists       full_os_specific_path (to folder or file)                            <br>
 * Remarks:                 <br>
 */
public class FileSystem extends AWenityModule
{
    private static final String MODULE_NAME = "fileSystem";

    private static final String OP_HAS_FREE_SPACE = "hasFreeSpace";
    private static final String OP_PATH_EXISTS = "pathExists";


    public FileSystem ()
    {
        super (MODULE_NAME);
    }

    @Override
    public ModuleResponse process (final ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            final Iterator<String> moduleParamsIter = moduleRequest.getModuleParams ();
            final String operationName = Utils.getIteratorValueEx (moduleParamsIter, "operation_name");
            final String path = Utils.getIteratorValueEx (moduleParamsIter, "full_path");

            final File file = new File (path);
            String exitStatus;
            if (OP_HAS_FREE_SPACE.equalsIgnoreCase (operationName))
            {
                exitStatus = hasEnoughFreeSpace (moduleParamsIter, file);
            }
            else
                if (OP_PATH_EXISTS.equalsIgnoreCase (operationName))
                {
                    exitStatus = checkPathExists (file);
                }
                else
                {
                    throw new IllegalArgumentException ("Unknown operation name: " + operationName);
                }

            return ModuleResponse.newResponse (exitStatus);
        }
        catch (Exception ex)
        {
            throw new Exception ("An error occurred while executing module: " + MODULE_NAME +
                                     ". Did you specify all the correct arguments in the form of" +
                                     " 'operation_name full_path [req_free_space_in_mb]'? Error: " + ex, ex);
        }

    }

    private String checkPathExists (final File file)
    {
        return file.exists () ? Constants.EXIT_STATUS_TRUE_STR : Constants.EXIT_STATUS_FALSE_STR;
    }

    private String hasEnoughFreeSpace (final Iterator<String> moduleParamsIter, final File file)
    {
        final String reqFreeSizeMb = Utils.getIteratorValueEx (moduleParamsIter, "req_free_space_in_mb");
        final long usableSpaceMb = file.getUsableSpace () / Constants.MB_IN_BYTES; //  usableSpaceMb == 0L if pathname does not name a partition
        if (Logger.debugOn () && usableSpaceMb == 0)
            Logger.debug ("WARNING! Free space is 0 Mb, this can be because of the invalid path specification!");

        return usableSpaceMb >= Long.parseLong (reqFreeSizeMb) ? Constants.EXIT_STATUS_TRUE_STR : Constants.EXIT_STATUS_FALSE_STR;
    }


}
