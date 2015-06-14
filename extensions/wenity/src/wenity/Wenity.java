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

import wenity.modules.common.IWenityModule;
import wenity.modules.common.ModuleRequest;
import wenity.modules.common.ModuleResponse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

public class Wenity
{

    // main method: returns exitCode and optionally creates answer file
    public int doIt (String[] args)
    {
        try
        {
            final ModuleRequest moduleRequest = parseArgs (args);

            final IWenityModule module = findModule (moduleRequest.getModuleName ());
            if (module == null)
            {
                Logger.error ("Can't find module " + moduleRequest.getModuleName ());
                return Constants.EXIT_STATUS_APP_ERROR;
            }

            final ModuleResponse moduleResponse = executeModule (module, moduleRequest);
            if (moduleResponse.isValid () && moduleResponse.createsResponseFile ())
            {
                // write response file only if user selected something
                writeResponseFile (moduleResponse.getResultAsString ());
                return Constants.EXIT_STATUS_OK;
            }

            return moduleResponse.getResultAsInt ();
        }
        catch (Exception ex)
        {
            Logger.exception ("Wenity - An error occurred! ", ex);
            return Constants.EXIT_STATUS_APP_ERROR;
        }
    }

    private ModuleRequest parseArgs (final String[] args)
    {
        try
        {
            // Usage: [-d] module_name module_parameters
            final int argsLength = args.length;
            final int lastArgIdx = argsLength - 1;

            final boolean hasOptionalParam = Constants.PARAM_VERBOSE.equals (args[0]) || Constants.PARAM_INFO.equals (args[0]);  // optional

            final int moduleNameIdx = hasOptionalParam ? 1 : 0;
            final String moduleName = args[moduleNameIdx];

            final int firstModuleParamIdx = moduleNameIdx + 1;
            String[] moduleParams = new String[0]; // assume module with no parameters
            if (firstModuleParamIdx <= lastArgIdx)
            {
                // copy module params, if any
                moduleParams = Arrays.copyOfRange (args, firstModuleParamIdx, argsLength);
            }

            Logger.debug ("Processed arguments - module: '%s' parameters: '%s'", moduleName, Arrays.toString (moduleParams));

            return new ModuleRequest (moduleName, moduleParams);
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException ("Can't parse program arguments! Reason: " + ex, ex);
        }
    }

    private IWenityModule findModule (String moduleName)
    {
        for (final IWenityModule module : ModuleConfig.getInstalledModules ())
        {
            if (module.canProcess (moduleName))
            {
                return module;
            }
        }
        return null;
    }

    private ModuleResponse executeModule (IWenityModule module, ModuleRequest moduleRequest) throws Exception
    {
        try
        {
            Logger.debug ("Executing '%s' with request '%s'", module.getModuleName (), moduleRequest);

            final ModuleResponse moduleResponse = module.process (moduleRequest);

            Logger.debug ("Executed '%s' with response '%s'", module.getModuleName (), moduleResponse);
            return moduleResponse;
        }
        catch (Exception ex)
        {
            Logger.error ("Failed to execute '%s' with params '%s'", module.getModuleName (), moduleRequest);
            throw ex;
        }
    }

    private void writeResponseFile (final String data) throws IOException
    {
        PrintWriter printWriter = null;
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter (Constants.RESPONSE_FILE_NAME);
            bufferedWriter = new BufferedWriter (fileWriter);
            printWriter = new PrintWriter (bufferedWriter);
            printWriter.println (data);
        }
        finally
        {
            Utils.closeNoThrow (printWriter);
            Utils.closeNoThrow (bufferedWriter);
            Utils.closeNoThrow (fileWriter);
        }
    }


}
