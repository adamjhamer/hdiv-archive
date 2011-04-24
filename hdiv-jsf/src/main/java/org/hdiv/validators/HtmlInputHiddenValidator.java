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

package org.hdiv.validators;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.components.HtmlInputHiddenExtension;
import org.hdiv.util.HDIVErrorCodes;
import org.hdiv.util.UtilsJsf;
import org.hdiv.validation.ValidationError;

/**
 * Validates component of type HtmlInputHiddenExtension.
 * 
 * @author Gotzon Illarramendi
 */
public class HtmlInputHiddenValidator implements ComponentValidator {

	private static Log log = LogFactory.getLog(HtmlInputHiddenValidator.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hdiv.validators.ComponentValidator#validate(javax.faces.component
	 * .UIComponent)
	 */
	public ValidationError validate(UIComponent component) {

		HtmlInputHiddenExtension inputHidden = (HtmlInputHiddenExtension) component;
		ValidationError error = this.validateHiddenComponent(inputHidden);

		return error;
	}

	/**
	 * Validates Hidden component received as input
	 * 
	 * @param inputHidden
	 *            component to validar
	 * @return validation result
	 */
	private ValidationError validateHiddenComponent(HtmlInputHiddenExtension inputHidden) {

		UIData tabla = UtilsJsf.findParentUIData(inputHidden);

		int rowIndex = 0;
		if (tabla != null) {
			rowIndex = tabla.getRowIndex();
		}
		Object hiddenValue = null;
		Object hiddenRealValue = null;
		FacesContext fContext = FacesContext.getCurrentInstance();

		HttpServletRequest request = (HttpServletRequest) fContext.getExternalContext().getRequest();
		if (rowIndex >= 0) {
			// If rowIndex >= 0, actual position is a table and hidden's component
			// clientId is correct

			hiddenValue = request.getParameter(inputHidden.getClientId(fContext));
			hiddenRealValue = inputHidden.getRealValue(inputHidden.getClientId(fContext));

			if (log.isDebugEnabled()) {
				log.debug("Hidden's value received:" + hiddenValue);
				log.debug("Hidden's value sent to the client:" + hiddenRealValue);
			}

			if (hiddenValue == null) {
				ValidationError error = new ValidationError();
				error.setErrorKey(HDIVErrorCodes.REQUIRED_PARAMETERS);
				error.setErrorParam(inputHidden.getId());
				error.setErrorValue("null");
				error.setErrorComponent(inputHidden.getClientId(fContext));
				return error;
			}

			boolean correcto = hiddenValue.equals(hiddenRealValue);
			if (!correcto) {
				ValidationError error = new ValidationError();
				error.setErrorKey(HDIVErrorCodes.PARAMETER_VALUE_INCORRECT);
				error.setErrorParam(inputHidden.getId());
				error.setErrorValue(hiddenRealValue.toString());
				error.setErrorComponent(inputHidden.getClientId(fContext));
				return error;
			}
		} else {
			// else, actual position isn't a table, but hidden is in a table
			// and its clientId is incorrect
			List clientIds = inputHidden.getClientIds();
			for (int i = 0; i < clientIds.size(); i++) {
				String clientId = (String) clientIds.get(i);
				hiddenValue = request.getParameter(clientId);
				hiddenRealValue = inputHidden.getRealValue(clientId);
				if (log.isDebugEnabled()) {
					log.debug("Hidden's value received:" + hiddenValue);
					log.debug("Hidden's value sent to the client:" + hiddenRealValue);
				}

				if (hiddenValue == null) {
					ValidationError error = new ValidationError();
					error.setErrorKey(HDIVErrorCodes.REQUIRED_PARAMETERS);
					error.setErrorParam(inputHidden.getId());
					error.setErrorValue("null");
					error.setErrorComponent(inputHidden.getClientId(fContext));
					return error;
				}

				boolean correcto = hiddenValue.equals(hiddenRealValue);
				if (!correcto) {
					ValidationError error = new ValidationError();
					error.setErrorKey(HDIVErrorCodes.PARAMETER_VALUE_INCORRECT);
					error.setErrorParam(inputHidden.getId());
					error.setErrorValue(hiddenRealValue.toString());
					error.setErrorComponent(inputHidden.getClientId(fContext));
					return error;
				}
			}

		}

		return null;
	}

}
