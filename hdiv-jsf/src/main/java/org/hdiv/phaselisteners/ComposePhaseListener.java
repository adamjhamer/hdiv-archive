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

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.dataComposer.DataComposerFactory;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.util.HDIVUtil;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Phaselistener that creates, initializes and finalizes DataComposer.
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class ComposePhaseListener implements PhaseListener {

	private static final long serialVersionUID = -1238560694931127680L;

	private static Log log = LogFactory.getLog(ComposePhaseListener.class);

	/**
	 * Factory that creates IDataComposer instances
	 */
	private DataComposerFactory factory;

	/**
	 * Default constructor
	 */
	public ComposePhaseListener() {
		if (log.isDebugEnabled()) {
			log.debug("New ComposePhaseListener instance created");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.event.PhaseListener#getPhaseId()
	 */
	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
	 */
	public void beforePhase(PhaseEvent event) {

		if (this.factory == null) {
			WebApplicationContext wac = FacesContextUtils.getRequiredWebApplicationContext(event.getFacesContext());
			this.factory = (DataComposerFactory) wac.getBean("dataComposerFactory");
		}

		HttpServletRequest request = (HttpServletRequest) event.getFacesContext().getExternalContext().getRequest();

		IDataComposer composer = this.factory.newInstance();
		composer.startPage();
		HDIVUtil.setDataComposer(composer, request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
	 */
	public void afterPhase(PhaseEvent event) {

		HttpServletRequest request = (HttpServletRequest) event.getFacesContext().getExternalContext().getRequest();
		
		// End of page in the DataComposer
		// Caution!!! This method is not called when a redirect is executed.
		// In this case method logic is executed in the class 
		// RedirectExternalContext, after performing the redirect.
		IDataComposer dataComposer = (IDataComposer) HDIVUtil.getDataComposer(request);
		dataComposer.endPage();

		// It is executed here and not in the ConfigPhaseListener because Webflow
		// doesn't maintain the correct execution order of phase listeners.
		// See https://jira.springsource.org/browse/FACES-53
		HDIVUtil.resetLocalData();

	}

}
