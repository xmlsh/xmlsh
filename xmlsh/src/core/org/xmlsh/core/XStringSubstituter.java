/**
 * 
 */
package org.xmlsh.core;

import java.util.Map;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrMatcher;
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
        // TODO Auto-generated constructor stub
        mLogger.entry( /* TODO */);
    }

    /**
     * @param variableResolver
     */
    public XStringSubstituter(XStringLookup resolver) {
        super(resolver);
        mLogger.entry( resolver);
    }
}
