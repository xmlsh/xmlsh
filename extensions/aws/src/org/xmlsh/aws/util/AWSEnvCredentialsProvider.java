/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

/*
 * Copyright 2012-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import org.xmlsh.sh.shell.Shell;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

/**
 * 
 * DAL: Backwards compatibility with old variables AWS_ACCESS_KEY
 */
public class AWSEnvCredentialsProvider implements AWSCredentialsProvider {

    /** Environment variable name for the AWS access key ID */
    private static final String ACCESS_KEY_ENV_VAR = "AWS_ACCESS_KEY";

    /** Environment variable name for the AWS secret key */
    private static final String SECRET_KEY_ENV_VAR = "AWS_SECRET_KEY";
    
    private String access_key;
    private String secret_key ;

    public AWSEnvCredentialsProvider(Shell shell) {
    	// Dont hold onto shell in case getCredentials() is called in the future on a different thread
	
    	access_key = shell.getEnv().getVarString(ACCESS_KEY_ENV_VAR);
    	secret_key = shell.getEnv().getVarString(SECRET_KEY_ENV_VAR);
	}

	public AWSCredentials getCredentials() {
        if ( access_key != null &&
             secret_key != null) {

            return new BasicAWSCredentials(
            		access_key , secret_key );
        }

        throw new AmazonClientException(
                "Unable to load AWS credentials from environment variables " +
                "(" + ACCESS_KEY_ENV_VAR + " and " + SECRET_KEY_ENV_VAR + ")");
    }

    public void refresh() {}

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}


/*
 * Copyright (C) 2008-2013 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */