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

package org.hdiv.logger;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.logs.Logger;
import org.hdiv.util.HDIVUtilJsf;
import org.hdiv.validation.ValidationError;

/**
 * Utility class for Logger
 * 
 * @author Gotzon Illarramendi
 */
public class LoggerUtil {

	private static Log log = LogFactory.getLog(LoggerUtil.class);

	/**
	 * Helper method to get logger's instance
	 * 
	 * @param facesContext
	 *            request context
	 * @return active logger
	 */
	public static Logger logger(FacesContext facesContext) {
		return HDIVUtilJsf.getLogger(facesContext);
	}

	/**
	 * Helper method to get target's value
	 * 
	 * @param facesContext
	 *            request context
	 * @return target
	 */
	public static String getTarget(FacesContext facesContext) {
		return HDIVUtilJsf.getTarget(facesContext);
	}

	/**
	 * Helper method to write a log
	 * 
	 * @param error
	 *            validation result
	 * @param facesContext
	 *            request context
	 */
	public static void log(ValidationError error, FacesContext facesContext) {
		Logger logger = LoggerUtil.logger(facesContext);
		logger.log(error.getErrorKey(), LoggerUtil.getTarget(FacesContext.getCurrentInstance()), error.getErrorParam(),
				error.getErrorValue());
	}
}
