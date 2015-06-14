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

package wenity.modules.common;

import wenity.Constants;

final public class  ModuleResponse
{
    private enum ModuleResponseStatus
    {
        CANCELLED,
        ERROR,
        OK,
    }

    private final String result;
    private final ModuleResponseStatus status;
    private final boolean createResponseFile;

    private ModuleResponse (final String result, final ModuleResponseStatus status, final boolean createResponseFile)
    {
        this.result = result;
        this.status = status;
        this.createResponseFile = createResponseFile;
    }

    public boolean isValid ()
    {
        return status == ModuleResponseStatus.OK;
    }

    public boolean isCanceled ()
    {
        return status == ModuleResponseStatus.CANCELLED;
    }

    public boolean isError ()
    {
        return status == ModuleResponseStatus.ERROR;
    }

    public boolean createsResponseFile ()
    {
        return createResponseFile;
    }

    public String getResultAsString ()
    {
        return result;
    }

    public int getResultAsInt ()
    {
        return Integer.parseInt (result);
    }

    @Override
    public String toString ()
    {
        return "ModuleResponse{" +
            "result='" + result + '\'' +
            ", status=" + status +
            ", createResponseFile=" + createResponseFile +
            '}';
    }


    @Deprecated
    public static ModuleResponse newErrorResponse ()
    {   // use an exception instead of this
        return new ModuleResponse (Constants.EXIT_STATUS_APP_ERROR_STR, ModuleResponseStatus.ERROR, false);
    }

    public static ModuleResponse newCancelResponse ()
    {
        return new ModuleResponse (Constants.EXIT_STATUS_USER_CANCEL_STR, ModuleResponseStatus.CANCELLED, false);
    }

    public static ModuleResponse newResponse (final String result)
    {
        return new ModuleResponse (result, ModuleResponseStatus.OK, false);
    }

    public static ModuleResponse newResponse (final int result)
    {
        return new ModuleResponse (Integer.toString (result), ModuleResponseStatus.OK, false);
    }

    public static ModuleResponse newFileResponse (final String result)
    {
        assert result != null;
        return new ModuleResponse (result, ModuleResponseStatus.OK, true);
    }

    public static ModuleResponse newFileResponse (final int result)
    {
        return new ModuleResponse (Integer.toString (result), ModuleResponseStatus.OK, true);
    }

}
