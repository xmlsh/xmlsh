/*
 * $Id: UnknownLocation.java,v 1.1 2004/07/08 14:30:23 cniles Exp $
 */
package javanet.staxutils.helpers;

import javanet.staxutils.StaticLocation;

import javax.xml.stream.Location;

/**
 * {@link Location} used to represent unknown locations.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public final class UnknownLocation implements Location, StaticLocation {

    public static final UnknownLocation INSTANCE = new UnknownLocation();
    
    public int getLineNumber() {

        return -1;

    }

    public int getColumnNumber() {

        return -1;

    }

    public int getCharacterOffset() {

        return -1;

    }

    public String getPublicId() {

        return null;

    }

    public String getSystemId() {

        return null;

    }

}