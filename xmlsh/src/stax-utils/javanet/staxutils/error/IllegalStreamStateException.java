/*
 * $Id: IllegalStreamStateException.java,v 1.1 2004/07/12 17:27:21 cniles Exp $
 */
package javanet.staxutils.error;

import javax.xml.stream.Location;

/**
 * {@link IllegalStateException} that includes a StAX {@link Location} identifying
 * the point where the error occured.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public class IllegalStreamStateException extends IllegalStateException {

    /** The location in the stream where the error occured. */
    private Location location;

    public IllegalStreamStateException() {

    }

    public IllegalStreamStateException(Location location) {

        this.location = location;

    }

    public IllegalStreamStateException(String s) {

        super(s);

    }

    public IllegalStreamStateException(String s, Location location) {

        super(s);
        this.location = location;

    }

    /**
     * Returns the {@link Location} where the error occured.
     * 
     * @return The {@link Location} where the error occured.
     */
    public Location getLocation() {

        return this.location;

    }

    /**
     * Sets the {@link Location} where the error occured.
     * 
     * @param location The {@link Location} where the error occured.
     */
    public void setLocation(Location location) {

        this.location = location;

    }

}