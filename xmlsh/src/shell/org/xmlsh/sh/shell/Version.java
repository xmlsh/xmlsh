/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

	private static Properties mProperties ;
	static Logger mLogger = Logger.getLogger(Version.class);
	static {
		mProperties = new Properties();
		try {
			InputStream stream = Version.class.getResourceAsStream("version.properties");
			mProperties.load( stream);
			stream.close();
		} catch (IOException e) {
			mLogger.debug("Exception loading version.properties",e);
		}
	}



	public static String getBuildDate()
	{

		if( mProperties == null )

			return "";
		else
			return mProperties.getProperty("version.build_date");
	}

	public static String getRelease()
	{
		if( mProperties == null )

			return "";
		else
			return mProperties.getProperty("version.release");
	}

	public static String getVersion()
	{
		if( mProperties == null )

			return "";
		else
			return mProperties.getProperty("version.version");
	}



}
