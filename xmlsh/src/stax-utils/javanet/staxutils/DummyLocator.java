package javanet.staxutils;

import org.xml.sax.Locator;

/**
 * A dummy locator that returns -1 and null from all methods to
 * indicate that location info is not available.
 * 
 * @author Ryan.Shoemaker@Sun.COM
 */
public class DummyLocator implements Locator {

    /**
     * Return the column number where the current document event ends.
     * <p/>
     * <p><strong>Warning:</strong> The return value from the method
     * is intended only as an approximation for the sake of error
     * reporting; it is not intended to provide sufficient information
     * to edit the character content of the original XML document.</p>
     * <p/>
     * <p>The return value is an approximation of the column number
     * in the document entity or external parsed entity where the
     * markup triggering the event appears.</p>
     * <p/>
     * <p>If possible, the SAX driver should provide the line position
     * of the first character after the text associated with the document
     * event.</p>
     * <p/>
     * <p>If possible, the SAX driver should provide the line position
     * of the first character after the text associated with the document
     * event.  The first column in each line is column 1.</p>
     *
     * @return The column number, or -1 if none is available.
     * @see #getLineNumber
     */
    public int getColumnNumber() {
        return -1;
    }

    /**
     * Return the line number where the current document event ends.
     * <p/>
     * <p><strong>Warning:</strong> The return value from the method
     * is intended only as an approximation for the sake of error
     * reporting; it is not intended to provide sufficient information
     * to edit the character content of the original XML document.</p>
     * <p/>
     * <p>The return value is an approximation of the line number
     * in the document entity or external parsed entity where the
     * markup triggering the event appears.</p>
     * <p/>
     * <p>If possible, the SAX driver should provide the line position
     * of the first character after the text associated with the document
     * event.  The first line in the document is line 1.</p>
     *
     * @return The line number, or -1 if none is available.
     * @see #getColumnNumber
     */
    public int getLineNumber() {
        return -1;
    }

    /**
     * Return the public identifier for the current document event.
     * <p/>
     * <p>The return value is the public identifier of the document
     * entity or of the external parsed entity in which the markup
     * triggering the event appears.</p>
     *
     * @return A string containing the public identifier, or
     *         null if none is available.
     * @see #getSystemId
     */
    public String getPublicId() {
        return null;
    }

    /**
     * Return the system identifier for the current document event.
     * <p/>
     * <p>The return value is the system identifier of the document
     * entity or of the external parsed entity in which the markup
     * triggering the event appears.</p>
     * <p/>
     * <p>If the system identifier is a URL, the parser must resolve it
     * fully before passing it to the application.</p>
     *
     * @return A string containing the system identifier, or null
     *         if none is available.
     * @see #getPublicId
     */
    public String getSystemId() {
        return null;
    }
}
