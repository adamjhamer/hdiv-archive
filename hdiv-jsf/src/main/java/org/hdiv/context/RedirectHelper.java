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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.DataComposerFactory;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.util.Constants;
import org.hdiv.util.HDIVRequestUtils;
import org.hdiv.util.HDIVUtil;
import org.hdiv.util.HDIVUtilJsf;
import org.hdiv.util.RequestUtilsHDIV;

/**
 * Helper class for redirect operations
 * This class is independent from the JSF version. It is valid for 1.x and 2.0.
 * 
 * @author Gotzon Illarramendi
 *
 */
public class RedirectHelper {

	private static Log log = LogFactory.getLog(RedirectHelper.class);
	
	/**
	
	 * Checks that url needs to be securized (points to the application itself) and if so creates
	 * HDIV state and adds the identifier as a parameter to the url.
	 * 
	 * @param url Url to secure
	 * @return
	 */
	public String addHDIVStateToURL(String url){
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		ExternalContext externalContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest)externalContext.getRequest();
					
		//Check if it is necessary to insert the state
		if(!RequestUtilsHDIV.isInternalUrl(request, url)){
			if(log.isDebugEnabled()){
				log.debug("is external url");
			}
			
			return url;
		}
		
		HDIVConfig hdivConfig = HDIVUtil.getHDIVConfig(request.getSession().getServletContext());
		
		// Check if url points to a resource that does not need to be securized, as
		// an image
		if(RequestUtilsHDIV.isResourceUrl(hdivConfig, url)){
			if(log.isDebugEnabled()){
				log.debug("is resource url");
			}
			
			return url;
		}

		// if url has not got parameters, we do not have to include HDIV's state
		if (!hdivConfig.isValidationInUrlsWithoutParamsActivated() && !(url.indexOf("?")>0)) {
			return url;
		}
		
		String anchor = HDIVRequestUtils.getAnchorFromUrl(url);
		url = HDIVRequestUtils.removeAnchorFromUrl(url);
		
		//Create IDataComposer instance
		DataComposerFactory factory = HDIVUtilJsf.getDataComposerFactory(context);
		IDataComposer dataComposer = factory.newInstance();
		dataComposer.startPage();
		
		// Confidentiality is disabled, so the url doesn't change
		String encodedUrl = RequestUtilsHDIV.composeURL(request, dataComposer, url);
				
		String requestId = dataComposer.endRequest();
		
		String hdivParameter = (String) externalContext.getSessionMap().get(Constants.HDIV_PARAMETER);
		
		//Add state to the url
		String finalUrl = RequestUtilsHDIV.addHDIVState(hdivParameter, requestId, encodedUrl, anchor);
		
		//End Page. This makes the state to be stored in session.
		//CAUTION!!! This is made here instead of in ComposePhaselistener because after
		//the redirect no PhaseListener is executed.
		dataComposer.endPage();
		HDIVUtil.resetLocalData();
		
		return finalUrl;
	}
}
