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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public final class Utils
{
    private Utils ()
    {
    }


    public static final String getIteratorValueEx (final Iterator<String> stringIterator, final String valueName)
    {
        if (stringIterator.hasNext ())
            return stringIterator.next ();

        throw new NoSuchElementException ("" + valueName + " is not found!");
    }


    public static final int getIteratorIntValueEx (final Iterator<String> stringIterator, final String valueName)
    {
        if (stringIterator.hasNext ())
            return Integer.parseInt (stringIterator.next ());

        throw new NoSuchElementException ("" + valueName + " is not found!");
    }


    public static final String getIteratorOptionalValue (final Iterator<String> stringIterator) throws Exception
    {
        return stringIterator.hasNext () ? stringIterator.next () : "";
    }

//    public static final String createSimpleStackTrace (final Exception ex)
//    {
//        final StringBuilder sb = new StringBuilder (400);
//        sb.append (ex);
//
//        Throwable throwable = ex;
//        while (throwable.getCause () != null)
//        {
//            throwable = throwable.getCause ();
//            sb.append ("<--");
//            sb.append (throwable);
//        }
//
//        return sb.toString ();
//    }

//    public static final void createSimpleStackTraceTest ()
//    {
//        System.out.println (createSimpleStackTrace (new Exception ("1:One level")));
//        System.out.println (createSimpleStackTrace (new Exception ("2:One level", new Exception ("2nd level"))));
//        System.out.println (createSimpleStackTrace (new Exception ("3:One level", new Exception ("2nd level", new Exception ("3rd level")))));
//    }


    /**
     * Returns button captions separated by comma,
     */
    public static final String[] getButtonCaptions (final Iterator<String> moduleParamsIter)
    {
        final String buttonNames = Utils.getIteratorValueEx (moduleParamsIter, "button_caption");
        return buttonNames.split (",");
    }


    public static final void closeNoThrow (final Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close ();
            }
            catch (IOException ignored)
            {
                ignored.printStackTrace ();
            }
        }
    }


    public static final void sleepMs (final int millis)
    {
        try
        {
            Thread.sleep (millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace ();
        }
    }


    public static final void sleepSec (final int seconds)
    {
        sleepMs (seconds * 1000);
    }


    public static List<String> readFile (final String path) throws Exception
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader (new FileReader (path));

            final ArrayList<String> lines = new ArrayList<String> ();
            String line;
            while ((line = reader.readLine ()) != null)
            {
                lines.add (line);
            }
            return lines;
        }
        catch (Exception e)
        {
            throw new Exception ("Can't read file: " + path, e);
        }
        finally
        {
            closeNoThrow (reader);
        }
    }


}
