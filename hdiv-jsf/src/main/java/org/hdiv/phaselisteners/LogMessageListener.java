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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.logger.LogMessages;
import org.hdiv.logs.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Phase Listener that detects errors thrown by components of type
 * Select (SelectOne or SelectMany) and registers them in the HDIV logger.
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class LogMessageListener implements PhaseListener {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(LogMessageListener.class);

	/**
	 * Utility class for managing validation messages
	 */
	private LogMessages logMessages;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.event.PhaseListener#getPhaseId()
	 */
	public PhaseId getPhaseId() {
		return PhaseId.PROCESS_VALIDATIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
	 */
	public void beforePhase(PhaseEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("MESSAGEVALIDATION - BEFORE");
		}

		if (this.logMessages == null) {

			WebApplicationContext wac = FacesContextUtils.getRequiredWebApplicationContext(event.getFacesContext());
			Logger logger = (Logger) wac.getBean("logger");
			this.logMessages = new LogMessages();
			this.logMessages.init(logger);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
	 */
	public void afterPhase(PhaseEvent event) {

		if (log.isDebugEnabled()) {
			log.debug("MESSAGEVALIDATION - AFTER");
		}
		this.logMessages.manageMessages(event.getFacesContext());
	}
}
