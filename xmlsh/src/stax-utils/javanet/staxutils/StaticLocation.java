/*
 * $Id: StaticLocation.java,v 1.1 2004/07/08 14:29:42 cniles Exp $
 */
package javanet.staxutils;

import javax.xml.stream.Location;

/**
 * Marker interface used to denote {@link Location} implementations whose state is
 * not transient or dependent on external objects/events and will remain stable
 * unless explicitly updated.
 * 
 * @author Christian Niles
 * @version $Revision: 1.1 $
 */
public interface StaticLocation extends Location {

}