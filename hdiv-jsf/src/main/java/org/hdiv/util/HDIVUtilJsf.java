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

package org.hdiv.util;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.dataComposer.DataComposerFactory;
import org.hdiv.events.HDIVFacesEventListener;
import org.hdiv.exception.HDIVException;
import org.hdiv.logs.Logger;

/**
 * Utility class that stores in threadlocal/servletContext/request
 * necessary objects for JSF version.
 * @author Gotzon Illarramendi
 * 
 */
public class HDIVUtilJsf {

	/**
	 * Commons Logging instance.
	 */
	private static Log log = LogFactory.getLog(HDIVUtilJsf.class);

	public static final String FACESEVENTLISTENER_SERVLETCONTEXT_KEY = "FACESEVENTLISTENER_SERVLETCONTEXT_KEY";
	public static final String LOGGER_SERVLETCONTEXT_KEY = "LOGGER_SERVLETCONTEXT_KEY";
	public static final String TARGET_REQUEST_KEY = "TARGET_REQUEST_KEY";
	public static final String DATACOMPOSERFACTORY_SERVLETCONTEXT_KEY = "DATACOMPOSERFACTORY_SERVLETCONTEXT_KEY";

	/* HDIVFacesEventListener */

	public static HDIVFacesEventListener getFacesEventListener(FacesContext facesContext) {

		HDIVFacesEventListener newFacesEventListener = (HDIVFacesEventListener) facesContext.getExternalContext()
				.getApplicationMap().get(FACESEVENTLISTENER_SERVLETCONTEXT_KEY);

		if (newFacesEventListener == null) {
			throw new HDIVException(
					"HDIVFacesEventListener object has not been initialized correctly in servletContext.");
		} else {
			return newFacesEventListener;
		}
	}

	public static void setFacesEventListener(HDIVFacesEventListener newFacesEventListener, FacesContext facesContext) {

		facesContext.getExternalContext().getApplicationMap()
				.put(FACESEVENTLISTENER_SERVLETCONTEXT_KEY, newFacesEventListener);

	}

	/* Logger */

	public static Logger getLogger(FacesContext facesContext) {

		Logger logger = (Logger) facesContext.getExternalContext().getApplicationMap().get(LOGGER_SERVLETCONTEXT_KEY);

		if (logger == null) {
			throw new HDIVException("Logger object has not been initialized correctly in servletContext.");
		} else {
			return logger;
		}
	}

	public static void setLogger(Logger logger, FacesContext facesContext) {

		facesContext.getExternalContext().getApplicationMap().put(LOGGER_SERVLETCONTEXT_KEY, logger);

	}

	/* Target */

	public static String getTarget(FacesContext facesContext) {

		String target = (String) facesContext.getExternalContext().getRequestMap().get(TARGET_REQUEST_KEY);

		if (target == null) {
			throw new HDIVException("Target object has not been initialized correctly in servletContext.");
		} else {
			return target;
		}
	}

	public static void setTarget(String target, FacesContext facesContext) {

		facesContext.getExternalContext().getRequestMap().put(TARGET_REQUEST_KEY, target);

	}

	/* DataComposerFactory */

	public static DataComposerFactory getDataComposerFactory(FacesContext facesContext) {

		DataComposerFactory factory = (DataComposerFactory) facesContext.getExternalContext().getApplicationMap()
				.get(DATACOMPOSERFACTORY_SERVLETCONTEXT_KEY);

		if (factory == null) {
			throw new HDIVException(
					"DataComposerFactory object has not been initialized correctly in servletContext..");
		} else {
			return factory;
		}
	}

	public static void setDataComposerFactory(DataComposerFactory dataComposerFactory, FacesContext facesContext) {

		facesContext.getExternalContext().getApplicationMap()
				.put(DATACOMPOSERFACTORY_SERVLETCONTEXT_KEY, dataComposerFactory);

	}

}