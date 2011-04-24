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

package org.hdiv.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.util.Constants;
import org.hdiv.util.HDIVRequestUtils;
import org.hdiv.util.HDIVUtil;
import org.hdiv.util.RequestUtilsHDIV;

/**
 * HtmlOutputLink component extension
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class HtmlOutputLinkExtension extends HtmlOutputLink {

	private static Log log = LogFactory.getLog(HtmlOutputLinkExtension.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.
	 * FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {

		if (log.isDebugEnabled()) {
			log.debug("encodeBegin");
		}

		try {
			ExternalContext externalContext = context.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			String url = this.getValue().toString();

			// Check if it is necessary to add the state
			if (!RequestUtilsHDIV.isInternalUrl(request, url)) {
				if (log.isDebugEnabled()) {
					log.debug("is external url");
				}
				
				super.encodeBegin(context);
				return;
			}

			HDIVConfig hdivConfig = HDIVUtil.getHDIVConfig(request.getSession().getServletContext());

			// if url hasn't got parameters, we do not have to include HDIV's
			// state
			if (!hdivConfig.isValidationInUrlsWithoutParamsActivated() && !url.contains("?") && !hasUIParamChilds()) {

				super.encodeBegin(context);
				return;
			}

			// Check if url points to a resource that does not need to be securized, as
			// an image
			if (RequestUtilsHDIV.isResourceUrl(hdivConfig, url)) {
				if (log.isDebugEnabled()) {
					log.debug("is resource url");
				}
				super.encodeBegin(context);
				return;
			}

			String anchor = HDIVRequestUtils.getAnchorFromUrl(url);
			url = HDIVRequestUtils.removeAnchorFromUrl(url);

			IDataComposer dataComposer = HDIVUtil.getDataComposer(request);

			// Confidentiality is disabled, so the url doesn't change
			String encodedUrl = RequestUtilsHDIV.composeURL(request, dataComposer, url);

			boolean hasUIParams = false;

			Iterator it = this.getChildren().iterator();
			while (it.hasNext()) {
				UIComponent comp = (UIComponent) it.next();
				if (comp instanceof UIParameter) {
					hasUIParams = true;
					break;
				}
			}

			String requestId = dataComposer.endRequest();

			String hdivParameter = (String) externalContext.getSessionMap().get(Constants.HDIV_PARAMETER);

			if (hasUIParams) {
				this.setValue(encodedUrl);

				// Add a children UIParam component with Hdiv's state
				UIParameter paramComponent = (UIParameter) context.getApplication().createComponent(
						UIParameter.COMPONENT_TYPE);
				paramComponent.setName(hdivParameter);
				paramComponent.setValue(requestId);
				this.getChildren().add(paramComponent);
			} else {
			
				// Add state directly in the outputLink's value
				String finalUrl = RequestUtilsHDIV.addHDIVState(hdivParameter, requestId, encodedUrl, anchor);
				this.setValue(finalUrl);
			}

		} catch (FacesException e) {
			log.error("Error en HtmlOutputLinkExtension: " + e.getMessage());
			throw e;
		}

		super.encodeBegin(context);
	}

	public void encodeEnd(FacesContext context) throws IOException {

		super.encodeEnd(context);

		// Remove Hdiv's state component, we don't want to store it in the state
		String hdivParameter = (String) context.getExternalContext().getSessionMap().get(Constants.HDIV_PARAMETER);

		Iterator it = this.getChildren().iterator();
		
		// First we add to a list the components to remove
		// The list used by MyFaces has a problem with the iterator
	
		List toRemoveList = new ArrayList();
		while (it.hasNext()) {
			UIComponent comp = (UIComponent) it.next();
			if (comp instanceof UIParameter) {
				UIParameter param = (UIParameter) comp;
				String name = param.getName();
				if (name != null && name.equals(hdivParameter)) {
					toRemoveList.add(new Integer(this.getChildren().indexOf(param)));
				}
			}
		}
		// Remove the ones founded before
		Iterator iter = toRemoveList.iterator();
		while (iter.hasNext()) {
			Integer removeIndex = (Integer) iter.next();
			this.getChildren().remove(removeIndex.intValue());
		}
		// Deprecated method in 1.2, but necessary to work in 1.1
		if (this.getValueBinding("value") != null) {
			this.setValue(null);
		}
	}

	/**
	 * Search children component for UIParam components.
	 * 
	 * @return
	 */
	private boolean hasUIParamChilds() {

		boolean hasParams = false;

		Iterator it = this.getChildren().iterator();
		while (it.hasNext()) {
			UIComponent comp = (UIComponent) it.next();
			if (comp instanceof UIParameter) {
				hasParams = true;
				break;
			}
		}

		return hasParams;
	}
}
