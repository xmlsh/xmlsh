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

//
package wenity.modules.file;

import wenity.Constants;
import wenity.Logger;
import wenity.Utils;
import wenity.modules.common.AWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Iterator;

/**
 * Module name:             fileSelector                                                   <br>
 * Function:                show a file selector dialog and return selected file name      <br>
 * Accepted parameters:     dialog_caption    filter    [no_file_error_message]            <br>
 * _                          * if no_file_error_message is specified it's displayed when selected file is not valid   <br>
 * _                          * filter values:                                                           <br>
 * _                                * all       -> show all files                                         <br>
 * _                                * dir       -> show directories only                                  <br>
 * _                                * ext1,ext2 -> show files having ext1, ext2 only (still accepts if a valid file is typed manually)  <br>
 * Remarks:                 <br>
 */
public class FileSelector extends AWenityModule
{
    private static final String MODULE_NAME = "fileSelector";
    private static final String NO_FILTER = "all";  // show all files
    private static final String DIR_FILTER = "dir";

    public FileSelector ()
    {
        super (MODULE_NAME);
    }

    @Override
    public ModuleResponse process (final ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            final Iterator<String> moduleParamsIter = moduleRequest.getModuleParams ();
            final String dialogCaption = Utils.getIteratorValueEx (moduleParamsIter, "dialog_caption");
            final String filter = Utils.getIteratorValueEx (moduleParamsIter, "filter");
            final String noFileErrorMsg = Utils.getIteratorOptionalValue (moduleParamsIter);

            ModuleResponse moduleResponse = null;
            while (moduleResponse == null)
            {
                final JFileChooser fileChooser = createFileChooser (dialogCaption, filter); // always recreate
                if (fileChooser.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
                    moduleResponse = checkModuleResponse (fileChooser, noFileErrorMsg);
                else
                    moduleResponse = ModuleResponse.newCancelResponse ();
            }

            return moduleResponse;
        }
        catch (Exception ex)
        {
            throw new Exception ("An error occurred while executing module: " + MODULE_NAME +
                                     ". Did you specify all the correct arguments in the form of" +
                                     " 'dialog_caption filter [no_file_error_message]'? Error: " + ex, ex);
        }

    }

    private JFileChooser createFileChooser (final String dialogCaption, final String filter)
    {
        final JFileChooser fileChooser = new JFileChooser ();
        fileChooser.setMultiSelectionEnabled (false);
        fileChooser.setDialogTitle (dialogCaption);

        final boolean dirOnly = DIR_FILTER.equalsIgnoreCase (filter); // TODO precompile filter into enum or sthing
        fileChooser.setFileSelectionMode (dirOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);

        if (!dirOnly && !NO_FILTER.equalsIgnoreCase (filter))
        {
            final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter (filter, filter.split (Constants.ARGUMENT_SEPARATOR));
            fileChooser.setAcceptAllFileFilterUsed (false);
            fileChooser.setFileFilter (extensionFilter);
        }

        return fileChooser;
    }

    private ModuleResponse checkModuleResponse (final JFileChooser fileChooser, final String noFileErrorMsg)
    {
        final File selFile = fileChooser.getSelectedFile ();
        Logger.debug ("Selected file is %s. Exists: %s", selFile.getPath (), selFile.exists ());

        final boolean fileMustExist = !noFileErrorMsg.isEmpty (); // got error message->file must exist
        if (fileMustExist && !selFile.exists ())
        {
            JOptionPane.showMessageDialog (null, noFileErrorMsg);
            return null;
        }
        return ModuleResponse.newFileResponse (selFile.getPath ());
    }

}
