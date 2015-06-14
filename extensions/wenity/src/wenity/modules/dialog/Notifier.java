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
import wenity.Utils;
import wenity.modules.common.AWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Module name:             notifier                                            <br>
 * Function:                show notification in system tray                    <br>
 * Accepted parameters:     type    title   message   display_time_in_sec       <br>
 * .                         \- info, error, warning
 * Remarks:
 */

public class Notifier extends AWenityModule
{
    private static final String MODULE_NAME = "notifier";


    public Notifier ()
    {
        super (MODULE_NAME);
    }


    @Override
    public ModuleResponse process (final ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            if (!SystemTray.isSupported ())
            {
                throw new Exception ("Notification area is not supported on this operating system!");
            }

            // type    title   message   display_time_in_sec
            final Iterator<String> moduleParamsIter = moduleRequest.getModuleParams ();
            final String notType = Utils.getIteratorValueEx (moduleParamsIter, "type");
            final String notTitle = Utils.getIteratorValueEx (moduleParamsIter, "title");
            final String notMessage = Utils.getIteratorValueEx (moduleParamsIter, "message");
            final int notDelaySeconds = Utils.getIteratorIntValueEx (moduleParamsIter, "display_time_in_sec");

            final NotificationData notification = NotificationData.fromNotificationType (notType);

            final TrayIcon trayIcon = createTrayIcon (notification.optionPaneIconName);
            final SystemTray systemTray = addIconToTray (trayIcon);

            showNotification (notTitle, notMessage, notDelaySeconds, notification.trayIconMessageType, trayIcon, systemTray);

            return ModuleResponse.newResponse (Constants.EXIT_STATUS_TRUE_STR);
        }
        catch (Exception ex)
        {
            throw new Exception ("An error occurred while executing module: " + MODULE_NAME +
                                     ". Did you specify all the correct arguments in the form of" +
                                     " 'type title message display_time_in_sec'? Error: " + ex, ex);
        }
    }


    private void showNotification (final String notTitle, final String notMessage, final int notDelaySeconds,
        final TrayIcon.MessageType iconMsgType, final TrayIcon trayIcon, final SystemTray systemTray)
    {
        trayIcon.displayMessage (notTitle, notMessage, iconMsgType);
        // wait until time elapses even user closes notification balloon
        Utils.sleepSec (notDelaySeconds);
        systemTray.remove (trayIcon);
    }


    private SystemTray addIconToTray (final TrayIcon trayIcon) throws AWTException
    {
        final SystemTray tray = SystemTray.getSystemTray ();
        tray.add (trayIcon);
        return tray;
    }


    private TrayIcon createTrayIcon (final String iconName)
    {
        final Icon icon = UIManager.getIcon (iconName);
        final TrayIcon trayIcon = new TrayIcon (((ImageIcon) icon).getImage ());
//        final TrayIcon trayIcon = new TrayIcon (new BufferedImage (30, 30, BufferedImage.TYPE_INT_RGB)); // empty image
        trayIcon.setImageAutoSize (true);
        return trayIcon;
    }


    private static class NotificationData
    {
        private final String notificationType;
        private final String optionPaneIconName;
        private final TrayIcon.MessageType trayIconMessageType;
        private static final Map<String, NotificationData> mapping = new HashMap<String, NotificationData> ();

        private static final NotificationData INFO_NOTIFICATION = new NotificationData ("info", "OptionPane.informationIcon", TrayIcon.MessageType.INFO);
        private static final NotificationData ERROR_NOTIFICATION = new NotificationData ("error", "OptionPane.errorIcon", TrayIcon.MessageType.ERROR);
        private static final NotificationData WARNING_NOTIFICATION = new NotificationData ("warning", "OptionPane.warningIcon", TrayIcon.MessageType.WARNING);


        static
        {
            mapping.put (INFO_NOTIFICATION.notificationType, INFO_NOTIFICATION);
            mapping.put (ERROR_NOTIFICATION.notificationType, ERROR_NOTIFICATION);
            mapping.put (WARNING_NOTIFICATION.notificationType, WARNING_NOTIFICATION);
        }


        private NotificationData (final String notificationType, final String optionPaneIconName, final TrayIcon.MessageType trayIconMessageType)
        {
            this.notificationType = notificationType;
            this.optionPaneIconName = optionPaneIconName;
            this.trayIconMessageType = trayIconMessageType;
        }


        static NotificationData fromNotificationType (final String notificationType) throws Exception
        {
            final NotificationData notificationData = mapping.get (notificationType.toLowerCase ());
            if (notificationData != null)
                return notificationData;

            throw new Exception ("Unknown notification type:" + notificationType);
        }
    }
}
