/**
 * Copyright 2005-2010 hdiv.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hdiv.context;

import java.io.IOException;

import javax.faces.context.ExternalContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper of ExternalContext.
 * 
 * Generates HDIV state for the redirects and adds it as a parameter to the url
 * 
 * @author Gotzon Illarramendi
 *
 */
public class RedirectExternalContext extends ExternalContextWrapper{

	private static Log log = LogFactory.getLog(RedirectExternalContext.class);
	
	/**
	 * Helper with the redirect logic
	 */
	private RedirectHelper redirectHelper = new RedirectHelper();
	
	/**
	 * Original ExternalContext
	 */
	private ExternalContext wrapped;
	
	/**
	 * Default constructor
	 * @param wrapped original ExternalContext
	 */
	public RedirectExternalContext(ExternalContext wrapped) {
		super();
		this.wrapped = wrapped;
	}

	/*
	 * (non-Javadoc)
	 * @see org.hdiv.context.ExternalContextWrapper#getWrapped()
	 */
	public ExternalContext getWrapped() {
		
		return this.wrapped;
	}

	/**
	 * If it is an internal redirect (to the aplication itself) it generates the state, stores it in session
	 * and adds a parameter to the url
	 * @param url url to redirect
	 */
	public void redirect(String url) throws IOException {
		
		String finalUrl = this.redirectHelper.addHDIVStateToURL(url);
		if(log.isDebugEnabled()){
			log.debug("Redirecting to url:"+finalUrl);
		}
		
		this.wrapped.redirect(finalUrl);
				
	}
	
}
