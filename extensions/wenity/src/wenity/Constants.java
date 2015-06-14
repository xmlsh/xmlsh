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

public final class Constants
{
    public static final String APP_NAME = "Wenity";

    public static final String APP_VERSION = "Wenity v1.5 (c) Karoly Kalman build: Mar 6, 2013";

    public static final String APP_HOME = "http://kksw.zzl.org";

    public static final char NEW_LINE_MARKER = '|';

    public static final String ARGUMENT_SEPARATOR = ",";

    public static final String PARAM_VERBOSE = "-d";    // debug+info
    public static final String PARAM_INFO = "-i";       // info

    public static final String RESPONSE_FILE_NAME = "wenity_response.txt";

    // error statuses
    // [[ Unix is using an 8-bit value as exit/return code which value is typically interpreted
    // as an **UNSIGNED** value by the shell. ]]

    public static final int EXIT_STATUS_USER_CANCEL = 254;
    public static final String EXIT_STATUS_USER_CANCEL_STR = "254";

    public static final int EXIT_STATUS_APP_ERROR = 255;
    public static final String EXIT_STATUS_APP_ERROR_STR = "255";

    //
    public static final int EXIT_STATUS_OK = 0;
    public static final String EXIT_STATUS_OK_STR = "0";

    //
    public static final int EXIT_STATUS_TRUE = 1;
    public static final String EXIT_STATUS_TRUE_STR = "1";

    //
    public static final int EXIT_STATUS_FALSE = 2;
    public static final String EXIT_STATUS_FALSE_STR = "2";

    //
    public static final int MB_IN_BYTES = 1024 * 1024;


    private Constants ()
    {
    }
}

