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

// Jan 30, 2013; Feb 26, 2013
package wenity.modules.file;

import wenity.GuiUtils;
import wenity.Utils;
import wenity.modules.common.AWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;


/**
 * Module name:             fileViewer  <br>
 * Function:                show a text file and return selected button index (1..N)       <br>
 * Accepted parameters:     full_file_path     header_text     button_caption(s)           <br>
 * Remarks:                                                                                <br>
 */
public class FileViewer extends AWenityModule
{
    private static final String MODULE_NAME = "fileViewer";


    public FileViewer ()
    {
        super (MODULE_NAME);
    }


    @Override
    public ModuleResponse process (final ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            final GuiUtils.ResultHolder resultHolder = new GuiUtils.ResultHolder ();

            final Iterator<String> moduleParamsIter = moduleRequest.getModuleParams ();

            //  full_file_path   header_text   button_caption(s)
            final String filePath = Utils.getIteratorValueEx (moduleParamsIter, "full_file_path"); // CASE SENSITIVE!
            final String headerText = Utils.getIteratorValueEx (moduleParamsIter, "header_text");
            final String[] buttonCaptions = Utils.getButtonCaptions (moduleParamsIter);

            final boolean htmlFile = isHtmlFile (filePath);
            final JTextComponent textComponent = htmlFile ? createEditorPane () : createTextArea ();

            final JDialog dialog = new JDialog ();
            final JPanel buttonsPanel = createButtonsPanel (createButtons (dialog, resultHolder, buttonCaptions));
            final JPanel mainPanel = createMainPanel (headerText, textComponent, buttonsPanel);

            GuiUtils.initializeDialog (dialog, mainPanel);

            if (!htmlFile)
                showFileInDialogThrowing (textComponent, filePath);
            else
            {
                ((JEditorPane) textComponent).setPage ("file://" + filePath);
                dialog.setSize (800, 600);
            }

            GuiUtils.showCenteredDialog (dialog);

            return createModuleResponse (resultHolder);
        }
        catch (Exception ex)
        {
            throw new Exception ("An error occurred while executing module: " + MODULE_NAME +
                                     ". Did you specify all the correct arguments in the form of" +
                                     " 'full_file_path header_text button_caption(s)'? Error: " + ex, ex);
        }
    }


    private void showFileInDialogThrowing (final JTextComponent textArea, final String filePath) throws Exception
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader (new FileReader (filePath));
            // In theory JEditorPane extends JTextComponent so a single read can be used for both HTML and text, but
            // JEditorPane.read just shows the HTML file as text unless contentType and document is specified, but
            // dunno how to set it correctly.
            textArea.read (reader, "");
        }
        catch (IOException ex)
        {
            throw new Exception ("Failed to load from file '" + filePath + "'", ex);
        }
        finally
        {
            Utils.closeNoThrow (reader);
        }
    }


    private JPanel createMainPanel (final String headerText, final JTextComponent textComponent, final JPanel buttonsPanel)
    {
        final BorderLayout mainLayout = new BorderLayout ();
        final JPanel mainPanel = new JPanel (mainLayout);

        mainPanel.add (new JLabel (headerText, JLabel.CENTER), BorderLayout.NORTH);
        final JScrollPane comp = new JScrollPane (textComponent);
//        comp.setSize(1200,600);
        mainPanel.add (comp, BorderLayout.CENTER); // larger area
        mainPanel.add (buttonsPanel, BorderLayout.SOUTH);
//            mainPanel.setSize(200,300);
        return mainPanel;
    }


    private JTextArea createTextArea ()
    {
        final JTextArea textArea = new JTextArea (30, 60);
//            textArea.setLineWrap (true);
        textArea.setEditable (false);
        return textArea;
    }


    private JTextComponent createEditorPane () throws IOException
    {
        final JEditorPane editorPane = new JEditorPane ();
//        editorPane.setSize (800, 1000);
        editorPane.setEditable (false);
        return editorPane;
    }


    private JPanel createButtonsPanel (final JButton[] buttons)
    {
        final GridLayout buttonGrid = new GridLayout (0, buttons.length);
        final JPanel buttonPanel = new JPanel (buttonGrid);
//            buttonPanel.setSize(10,800);
        for (JButton btn : buttons)
        {
            buttonPanel.add (btn);
        }
        return buttonPanel;
    }


    private JButton[] createButtons (final JDialog dialog, final GuiUtils.ResultHolder resultHolder, final String[] buttonCaptions)
    {
        final JButton[] buttons = new JButton[buttonCaptions.length];

        int idx = 0;
        for (String caption : buttonCaptions)
        {
            final int btnIndex = idx + 1;
            final JButton button = new JButton (caption);
            button.addActionListener (createButtonListener (dialog, resultHolder, btnIndex));
            buttons[idx++] = button;
        }
        return buttons;
    }


    private ActionListener createButtonListener (final JDialog dialog, final GuiUtils.ResultHolder resultHolder, final int btnIndex)
    {
        return new ActionListener ()
        {
            @Override
            public void actionPerformed (final ActionEvent e)
            {
                resultHolder.setResult (btnIndex);
                dialog.dispose ();
            }
        };
    }


    private boolean isHtmlFile (final String filePath)
    {
        boolean htmlFile = false;
        final int dotIndex = filePath.lastIndexOf ('.');
        if (dotIndex >= 0)
        {
            final String extension = filePath.substring (dotIndex).toLowerCase ();
            htmlFile = extension.endsWith ("html") || extension.endsWith ("htm");
        }
        return htmlFile;
    }


    private ModuleResponse createModuleResponse (final GuiUtils.ResultHolder resultHolder)
    {
        return resultHolder.hasResult () ?
            ModuleResponse.newResponse (resultHolder.getResult ()) :
            ModuleResponse.newCancelResponse ();
    }
}
