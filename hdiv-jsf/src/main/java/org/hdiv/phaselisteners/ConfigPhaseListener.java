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

package org.hdiv.phaselisteners;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hdiv.application.IApplication;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.DataComposerFactory;
import org.hdiv.events.HDIVFacesEventListener;
import org.hdiv.exception.HDIVException;
import org.hdiv.logs.Logger;
import org.hdiv.util.HDIVUtil;
import org.hdiv.util.HDIVUtilJsf;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * PhaseListener that takes care of HDIV config, mostly objects that are stored
 * in ThreadLocal
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class ConfigPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -3803869221110488120L;

	/**
	 * Name of the attribute that contains the user token
	 */
	private static final String HDIV_USER_TOKEN_ATRR_NAME = "HDIV_USER_TOKEN";

	/**
	 * HDIV logger
	 */
	private Logger logger;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.event.PhaseListener#getPhaseId()
	 */
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
	 */
	public void beforePhase(PhaseEvent event) {

		if (event.getPhaseId().equals(PhaseId.RESTORE_VIEW)) {

			FacesContext context = event.getFacesContext();
			HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

			if (this.logger == null) {

				ServletContext servletContext = request.getSession().getServletContext();

				WebApplicationContext wac = FacesContextUtils.getRequiredWebApplicationContext(event.getFacesContext());
				HDIVConfig hdivConfig = (HDIVConfig) wac.getBean("config");
				// Validate the configuration
				this.validateConfig(hdivConfig);

				this.logger = (Logger) wac.getBean("logger");
				HDIVFacesEventListener facesEventListener = (HDIVFacesEventListener) wac
						.getBean("HDIVFacesEventListener");
				IApplication application = (IApplication) wac.getBean("application");
				DataComposerFactory dataComposerFactory = (DataComposerFactory) wac.getBean("dataComposerFactory");
				ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
				messageSource.setBeanClassLoader(wac.getClassLoader());
				String messageSourcePath = (String) wac.getBean("messageSourcePath");
				messageSource.setBasename(messageSourcePath);

				HDIVUtil.setHDIVConfig(hdivConfig, servletContext);
				HDIVUtil.setApplication(application, servletContext);
				HDIVUtil.setMessageSource(messageSource, servletContext);

				// It is added to the servletContext to be able to consume it from
				// components
				HDIVUtilJsf.setFacesEventListener(facesEventListener, context);
				HDIVUtilJsf.setLogger(this.logger, context);
				HDIVUtilJsf.setDataComposerFactory(dataComposerFactory, context);
			}
			HDIVUtil.setHttpServletRequest(request);

			// Init the target value
			String target = null;
			try {
				target = HDIVUtil.actionName(request);
			} catch (Exception e) {
				String errorMessage = HDIVUtil.getMessage("helper.actionName");
				throw new HDIVException(errorMessage, e);
			}
			HDIVUtilJsf.setTarget(target, context);

		}

		if (event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {

			FacesContext context = event.getFacesContext();
			// Add user's unique id to state
			this.addUserUniqueTokenToState(context);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
	 */
	public void afterPhase(PhaseEvent event) {
		
		// The reset of local data is executed in the ComposePhaseListener and not here due to
		// an error in Spring Faces
		// See https://jira.springsource.org/browse/FACES-53
		// if(event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)){
		// HDIVUtil.resetLocalData();
		// }
	}

	/**
	 * Not all the functionality of HDIV is available for JSF version.
	 * This method verifies that the configuration is correct.
	 * 
	 * @param hdivConfig
	 */
	private void validateConfig(HDIVConfig hdivConfig) {

		// Confidentiality is not available for JSF
		Boolean confidentiality = hdivConfig.getConfidentiality();
		if (confidentiality != null && confidentiality.booleanValue()) {
			throw new HDIVException("Confidentiality is not implemented in HDIV for JSF, disable it in hdiv-config.xml");
		}

		// Cookie integrity is not available for JSF
		boolean cookieIntegrity = hdivConfig.isCookiesIntegrityActivated();
		if (cookieIntegrity) {
			throw new HDIVException(
					"CookiesIntegrity is not implemented in HDIV for JSF, disable it in hdiv-config.xml");
		}

		// Cookie confidentiality is not available for JSF
		boolean cookieConfidentiality = hdivConfig.isCookiesConfidentialityActivated();
		if (cookieConfidentiality) {
			throw new HDIVException(
					"CookiesConfidentiality is not implemented in HDIV for JSF, disable it in hdiv-config.xml");
		}
	}

	/**
	 * Adds to the state a data unique for the user. This way state
	 * becomes something unique for each user. Session id is used as this
	 * unique data.
	 * 
	 * @param facesContext
	 *            request context
	 */
	private void addUserUniqueTokenToState(FacesContext facesContext) {

		UIViewRoot viewRoot = facesContext.getViewRoot();
		if (viewRoot != null) {

			String userToken = (String) viewRoot.getAttributes().get(HDIV_USER_TOKEN_ATRR_NAME);
			if (userToken == null) {

				HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
				if (session != null) {
					viewRoot.getAttributes().put(HDIV_USER_TOKEN_ATRR_NAME, session.getId());
				}

			}
		}
	}

}
