/**
 * 
 */
package org.xmlsh.core;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author DLEE
 * 
 */
public class XStringSubstituter extends StrSubstitutor {
  static Logger mLogger = LogManager.getLogger();

  public XStringSubstituter() {
    super();
    mLogger.entry( /* TODO */);
    setEnableSubstitutionInVariables(true);
  }

  /**
   * @param variableResolver
   */
  public XStringSubstituter(XStringLookup resolver) {
    super(resolver);
    mLogger.entry(resolver);
    setEnableSubstitutionInVariables(true);

  }
}
