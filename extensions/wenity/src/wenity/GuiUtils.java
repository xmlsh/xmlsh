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

// Mar 4, 2013
package wenity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public final class GuiUtils
{
    public static final void showCenteredDialog (final JDialog dialog)
    {
        dialog.setLocationRelativeTo (null);
        dialog.setVisible (true);
    }


    public static final void initializeDialog (final JDialog dialog, final JPanel mainPanel)
    {
        dialog.setTitle (Constants.APP_NAME);
        dialog.setModal (true);
        dialog.setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
        dialog.add (mainPanel);
//        dialog.setSize (200, 200);
        dialog.pack ();
    }


    public static JPanel createButtonsPanel (final String[] buttonCaptions, final JDialog dialogToDispose, final ResultHolder resultHolder)
    {
        final JPanel buttonPanel = new JPanel (new GridLayout (0, buttonCaptions.length));
        int btnNumber = 1;
        for (String caption : buttonCaptions)
        {
            final JButton button = new JButton (caption.trim ());
            button.addActionListener (newDialogCloseBtnListener (dialogToDispose, resultHolder, btnNumber++));
            buttonPanel.add (button);
        }
        return buttonPanel;
    }


    private static ActionListener newDialogCloseBtnListener (final JDialog dialog, final ResultHolder resultHolder, final int buttonNumber)
    {
        return new ActionListener ()
        {
            @Override
            public void actionPerformed (final ActionEvent e)
            {
                resultHolder.result = buttonNumber;
                dialog.dispose ();
            }
        };
    }


    private GuiUtils ()
    {
    }


    public static class ResultHolder
    {
        private static final int NOT_INITIALIZED = -1;
        private int result = NOT_INITIALIZED;


        public void setResult (final int result)
        {
            this.result = result;
        }


        public int getResult ()
        {
            return result;
        }


        public boolean hasResult ()
        {
            return result != NOT_INITIALIZED;
        }
    }

}
