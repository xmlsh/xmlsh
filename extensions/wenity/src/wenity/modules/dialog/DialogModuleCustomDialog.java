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

// March 5, 2013
package wenity.modules.dialog;

import wenity.Constants;
import wenity.GuiUtils;
import wenity.Logger;
import wenity.Utils;
import wenity.modules.common.ModuleResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/*package*/ class DialogModuleCustomDialog
{
    private final GuiUtils.ResultHolder selectedButtonResult = new GuiUtils.ResultHolder ();


    /*package*/ ModuleResponse doCustomDialog (final String dialogDefFilePath) throws Exception
    {
        try
        {
            Logger.debug ("Reading dialog definition file: " + dialogDefFilePath);
            final List<String> dialogSpec = readDefFile (dialogDefFilePath);

            final List<JTextField> inputFieldsList = new ArrayList<JTextField> (dialogSpec.size ());
            final JPanel fieldsPanel = createInputFieldsPanel (dialogSpec, inputFieldsList);

            final String[] buttonCaptions = extractButtonCaptions (dialogSpec);
            final int cancelButtonNumber = buttonCaptions.length; // last button is cancel, counted from 1

            final JDialog dialog = new JDialog ();
            final String headerText = dialogSpec.get (0);
            final JPanel mainPanel = createMainPanel (headerText, fieldsPanel, dialog, buttonCaptions);

            GuiUtils.initializeDialog (dialog, mainPanel);
            GuiUtils.showCenteredDialog (dialog);

            return processUserInput (inputFieldsList, cancelButtonNumber);
        }
        catch (Exception e)
        {
            throw new Exception ("Can't create custom dialog from file: " + dialogDefFilePath, e);
        }
    }


    private String[] extractButtonCaptions (final List<String> dialogSpec)
    {
        final String buttonSpec = dialogSpec.get (dialogSpec.size () - 1);
        final String[] captions = buttonSpec.split (Constants.ARGUMENT_SEPARATOR);
        Logger.debug ("Cancel button is: " + captions[captions.length - 1]);
        return captions;
    }


    private ModuleResponse processUserInput (final List<JTextField> inputFieldsList, final int cancelButtonNumber)
    {
        final ModuleResponse moduleResponse;
        if (selectedButtonResult.hasResult () && selectedButtonResult.getResult () != cancelButtonNumber)
        {
            final StringBuilder mergedUserInput = new StringBuilder (80);
            mergedUserInput.append (selectedButtonResult.getResult ()).append ('\n'); // selected button index
            for (final JTextField textField : inputFieldsList)
            {
                mergedUserInput.append (textField.getText ().trim ());
                mergedUserInput.append ('\n');
            }
            moduleResponse = ModuleResponse.newFileResponse (mergedUserInput.toString ());
        }
        else
        {
            moduleResponse = ModuleResponse.newCancelResponse ();
        }
        return moduleResponse;
    }


    private JPanel createMainPanel (final String headerText, final JPanel fieldsPanel, final JDialog dialog, final String[] buttonCaptions)
    {
        final JPanel mainPanel = new JPanel (new BorderLayout ());
        mainPanel.add (new JLabel (headerText, JLabel.CENTER), BorderLayout.NORTH);
        mainPanel.add (fieldsPanel, BorderLayout.CENTER); // larger area
        mainPanel.add (GuiUtils.createButtonsPanel (buttonCaptions, dialog, selectedButtonResult), BorderLayout.SOUTH);
        return mainPanel;
    }


    private JPanel createInputFieldsPanel (final List<String> dialogSpec, final List<JTextField> textFieldList)
    {
        final JPanel fieldsPanel = new JPanel (new GridLayout (0, 2));
        for (int idx = 1; idx < dialogSpec.size () - 1; idx++)
        {
            final JTextField textField = new JTextField ("");
            textFieldList.add (textField);
            fieldsPanel.add (new JLabel ("   " + dialogSpec.get (idx) + "   "));
            fieldsPanel.add (textField);
        }
        return fieldsPanel;
    }


    private List<String> readDefFile (final String dialogDefFilePath) throws Exception
    {
        final List<String> dialogDef = Utils.readFile (dialogDefFilePath);
        if (dialogDef.size () <= 3)
            throw new Exception ("Dialog definition must contain at least 3 lines!");
        return dialogDef;
    }

}
