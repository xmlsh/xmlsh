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

// March 1, 2013
package wenity.modules.dialog;

import wenity.Constants;
import wenity.GuiUtils;
import wenity.Logger;
import wenity.Utils;
import wenity.modules.common.AWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;


/**
 * Module name:             progressIndicator  <br>
 * Function:                show progress indicator dialog until the specified file is created    <br>
 * Accepted parameters:     status_file_full_path    header_text                           <br>
 * Remarks:                 poll period for status file is 1 second.                       <br>
 */
public class ProgressIndicator extends AWenityModule
{
    private static final String MODULE_NAME = "progressIndicator";
    private static final int FILE_CHECKER_SLEEP_TIME_SEC = 1;

    private boolean cancelPressed = false;


    public ProgressIndicator ()
    {
        super (MODULE_NAME);
    }


    @Override
    public ModuleResponse process (final ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            // status_file_full_path    header_text
            final Iterator<String> moduleParamsIter = moduleRequest.getModuleParams ();
            final String statusFilePath = Utils.getIteratorValueEx (moduleParamsIter, "status_file_full_path");
            final String headerText = Utils.getIteratorValueEx (moduleParamsIter, "header_text");

            final JDialog dialog = createDialog (headerText);

            startFileCheckerThread (dialog, statusFilePath);

            GuiUtils.showCenteredDialog (dialog);
            Logger.debug ("Progress bar finished. User cancelled: " + cancelPressed);

            return cancelPressed ? ModuleResponse.newCancelResponse () : ModuleResponse.newResponse (Constants.EXIT_STATUS_TRUE);
        }
        catch (Exception ex)
        {
            throw new Exception ("An error occurred while executing module: " + MODULE_NAME +
                                     ". Did you specify all the correct arguments in the form of" +
                                     " 'status_file_full_path header_text'? Error: " + ex, ex);
        }
    }


    private void startFileCheckerThread (final JDialog dialog, final String statusFilePath)
    {
        final Runnable fileChecker = new Runnable ()
        {
            @Override
            public void run ()
            {
                // if file is already there then we skip the while() and dispose the
                // (yet) not shown dialog. Later the dialog will be shown forever
                // as the checker thread (this) has already been finished.
                //
                Utils.sleepMs (500); // allow dialog to be shown

                final File f = new File (statusFilePath);
                while (!f.exists ())
                {
                    Logger.debug ("File " + f.getAbsolutePath () + " does not exist. Waiting.");
                    Utils.sleepSec(FILE_CHECKER_SLEEP_TIME_SEC);
                }
                dialog.dispose (); // TODO: check concurrency ? dialog is disposable multiple times, but Swing is not thread safe
            }
        };

        new Thread (fileChecker).start ();
    }


    private JDialog createDialog (final String headerText)
    {
        final JDialog dialog = new JDialog();
        final JPanel mainPanel = createMainPanel (headerText);
        GuiUtils.initializeDialog(dialog, mainPanel);
        dialog.addWindowListener (new WindowAdapter ()
        {
            @Override
            public void windowClosing (final WindowEvent e)
            {
                cancelPressed = true;
            }
        });
        return dialog;
    }


    private JPanel createMainPanel (final String text)
    {
        final GridLayout mainLayout = new GridLayout (2, 1);
        final JProgressBar progressBar = new JProgressBar (0);
        progressBar.setIndeterminate (true);

        final JPanel mainPanel = new JPanel (mainLayout);
        mainPanel.add (new JLabel (text, JLabel.CENTER));
        mainPanel.add (progressBar);
//            mainPanel.setSize(200,300);
        return mainPanel;
    }
}
