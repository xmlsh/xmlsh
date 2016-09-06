/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

public class ShellURIResolver implements URIResolver {

  private URIResolver mDelegate;

  /*
   * (non-Javadoc)
   * 
   * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
   * java.lang.String)
   */
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    // TODO Auto-generated method stub
    return mDelegate.resolve(href, base);
  }

  public ShellURIResolver(URIResolver delegate) {
    mDelegate = delegate;
  }

}
