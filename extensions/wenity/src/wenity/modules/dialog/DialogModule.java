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

package wenity.modules.dialog;

import wenity.Constants;
import wenity.Utils;
import wenity.modules.common.AWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Module name:             dialog <br>
 * Function:                show a dialog and return selected button index (1..N)    <br>
 * Accepted parameters:     dialog_type    dialog_text    button_caption(s)*         <br>
 * _                            * button_caption(s) is not used for input dialog     <br>
 * _                            * if dialog_type is CUSTOM dialog_text specifies the dialog definition file     <br>
 * Remarks:                 dialog_text new line character is: '|'                   <br>
 */
public class DialogModule extends AWenityModule
{
    private static final String MODULE_NAME = "dialog";
    private static final Map<DialogType, Integer> DIALOG_TYPES = initDialogTypesMap ();

    private enum DialogType
    {
        QUESTION,
        WARNING,
        ERROR,
        INPUT,
        INFO,
        CUSTOM,
    }


    public DialogModule ()
    {
        super (MODULE_NAME);
    }


    @Override
    public ModuleResponse process (final ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            final Iterator<String> moduleParamsIter = moduleRequest.getModuleParams ();

            // dialog_type   dialog_text     button_captions (not used for input dialog)
            final DialogType dialogType = getDialogType (moduleParamsIter);
            final String dialogText = getDialogText (moduleParamsIter);

            ModuleResponse moduleResponse;
            switch ( dialogType )
            {
                case INPUT:
                    moduleResponse = showInputDlg (dialogText);
                    break;
                case CUSTOM:
                    moduleResponse = showCustomDlg (dialogText);
                    break;
                default:
                    moduleResponse = showOptionDlg (dialogType, dialogText, Utils.getButtonCaptions (moduleParamsIter));
            }

            return moduleResponse;
        }
        catch (Exception ex)
        {
            throw new Exception ("An error occurred while executing module: " + MODULE_NAME +
                                     ". Did you specify all the correct arguments in the form of" +
                                     " 'dialog_type dialog_text button_caption(s)'? Error: " + ex, ex);
        }
    }


    private String getDialogText (final Iterator<String> moduleParamsIter)
    {
        return Utils.getIteratorValueEx (moduleParamsIter, "dialog_text").replace (Constants.NEW_LINE_MARKER, '\n');
    }


    private DialogType getDialogType (final Iterator<String> moduleParamsIter)
    {
        final String dialogTypeString = Utils.getIteratorValueEx (moduleParamsIter, "dialog_type").toUpperCase ();
        return DialogType.valueOf (dialogTypeString);
    }


    private ModuleResponse showInputDlg (final String dialogText)
    {
        final int messageType = DIALOG_TYPES.get (DialogType.INPUT);
        final String userInput = processInputLoop (dialogText, messageType);
        return (userInput == null) ? ModuleResponse.newCancelResponse () : ModuleResponse.newFileResponse (userInput);
    }


    private ModuleResponse showOptionDlg (final DialogType dialogType, final String dialogText, final Object[] buttons)
    {
        assert buttons.length > 0;
        final int messageType = DIALOG_TYPES.get (dialogType);
        final int selectedButtonIdx = JOptionPane.showOptionDialog (null, dialogText, Constants.APP_NAME, JOptionPane.DEFAULT_OPTION, messageType, null, buttons, buttons[0]);
        return (selectedButtonIdx == JOptionPane.CLOSED_OPTION) ? ModuleResponse.newCancelResponse () : ModuleResponse.newResponse (selectedButtonIdx + 1);
    }

    private ModuleResponse showCustomDlg (final String dialogDefFilePath) throws Exception
    {
        return new DialogModuleCustomDialog().doCustomDialog(dialogDefFilePath);
    }


    private String processInputLoop (final String dialogText, final int messageType)
    {
        String userInput;
        do
        {
            userInput = JOptionPane.showInputDialog (null, dialogText, Constants.APP_NAME, messageType);
        } while (userInput != null && userInput.isEmpty ()); // empty input is not accepted
        return userInput;
    }


    private static EnumMap<DialogType, Integer> initDialogTypesMap ()
    {
        final EnumMap<DialogType, Integer> enumMap = new EnumMap<DialogType, Integer> (DialogType.class);
        enumMap.put (DialogType.QUESTION, JOptionPane.QUESTION_MESSAGE);
        enumMap.put (DialogType.WARNING, JOptionPane.WARNING_MESSAGE);
        enumMap.put (DialogType.ERROR, JOptionPane.ERROR_MESSAGE);
        enumMap.put (DialogType.INFO, JOptionPane.INFORMATION_MESSAGE);
        enumMap.put (DialogType.INPUT, JOptionPane.QUESTION_MESSAGE);
        enumMap.put (DialogType.CUSTOM, null /*not used*/);
        return enumMap;
    }

}
