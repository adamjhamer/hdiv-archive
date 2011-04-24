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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.exception.HDIVException;
import org.hdiv.util.HDIVErrorCodes;
import org.hdiv.util.HDIVUtil;
import org.hdiv.util.MessageFactory;
import org.hdiv.validation.ValidationError;

/**
 * Responsible for validating that the parameters coming from an editable
 * component (InputText, Textarea, Secret) are logical
 * 
 * @author Ugaitz Urien
 */
public class EditableValidator implements ComponentValidator {

	private static Log log = LogFactory.getLog(EditableValidator.class);

	/**
	 * HDIV config
	 */
	private HDIVConfig hdivConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hdiv.validators.ComponentValidator#validate(javax.faces.component
	 * .UIComponent)
	 */
	public ValidationError validate(UIComponent component) {

		UIForm form = (UIForm) component;
		ValidationError error = this.validateEditablesForm(form);
		return error;
	}

	/**
	 * Validates all the editable components of the form
	 * 
	 * @param formComponent
	 * @return
	 */
	private ValidationError validateEditablesForm(UIForm formComponent) {
		ValidationError error = null;

		List components = formComponent.getChildren();
		for (int i = 0; i < components.size(); i++) {
			UIComponent component = (UIComponent) components.get(i);
			ValidationError tempError = validateEditablesComponent(component);
			if (tempError != null) {
				error = tempError;
			}
		}
		return error;
	}

	/**
	 * Recursive method. When a component is non editable, verifies its
	 * children.
	 * 
	 * @param uiComponent
	 * @return
	 */
	private ValidationError validateEditablesComponent(UIComponent uiComponent) {
		if ((uiComponent instanceof HtmlInputText) || (uiComponent instanceof HtmlInputTextarea)
				|| (uiComponent instanceof HtmlInputSecret) || (uiComponent instanceof HtmlInputHidden)) {
			return validateInput(uiComponent);
		} else {
			List children = uiComponent.getChildren();
			ValidationError error = null;
			for (int i = 0; i < children.size(); i++) {
				UIComponent child = (UIComponent) children.get(i);
				ValidationError tempError = validateEditablesComponent(child);
				if (tempError != null) {
					error = tempError;
				}
			}
			return error;
		}
	}

	/**
	 * Configures variables to call validateContent
	 * 
	 * @param inputHiddenComponent
	 * @return
	 */
	private ValidationError validateInput(UIComponent inputComponent) {
		FacesContext fc = FacesContext.getCurrentInstance();
		Object value = null;
		String clientId = inputComponent.getClientId(fc);
		String contentType = null;
		if (inputComponent instanceof HtmlInputHidden) {
			contentType = "hidden";
			value = ((HtmlInputHidden) inputComponent).getValue();
		} else if (inputComponent instanceof HtmlInputTextarea) {
			contentType = "textarea";
			value = ((HtmlInputTextarea) inputComponent).getValue();
		} else if (inputComponent instanceof HtmlInputText) {
			contentType = "text";
			value = ((HtmlInputText) inputComponent).getValue();
		} else if (inputComponent instanceof HtmlInputSecret) {
			contentType = "password";
			value = ((HtmlInputSecret) inputComponent).getValue();
		}
		if (!validateContent(clientId, value, contentType)) {
			Object[] params = { clientId };
			FacesMessage facesMessage = MessageFactory.getMessage("hdiv.editable.error", params);
			if (facesMessage == null) {
				facesMessage = new FacesMessage("Invalid content for field");
			}
			FacesContext.getCurrentInstance().addMessage(clientId, facesMessage);
			if (inputComponent instanceof HtmlInputHidden) {
				((HtmlInputHidden) inputComponent).setValid(false);
			} else if (inputComponent instanceof HtmlInputTextarea) {
				((HtmlInputTextarea) inputComponent).setValid(false);
			} else if (inputComponent instanceof HtmlInputText) {
				((HtmlInputText) inputComponent).setValid(false);
			} else if (inputComponent instanceof HtmlInputSecret) {
				((HtmlInputSecret) inputComponent).setValid(false);
			}
			return new ValidationError(HDIVErrorCodes.EDITABLE_VALIDATION_ERROR, null, clientId, value.toString());
		}
		return null;
	}

	/**
	 * Uses HdivConfig to validate editable field content
	 * 
	 * @param clientId
	 * @param contentObj
	 * @param contentType
	 * @return
	 */
	private boolean validateContent(String clientId, Object contentObj, String contentType) {
		boolean result = true;
		if (!(contentObj instanceof String)) {
			return result;
		}
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		String target = getTarget(request);
		String targetWithoutContextPath = getTargetWithoutContextPath(request, target);

		String[] content = { (String) contentObj };
		if (hdivConfig.existValidations()) {
			result = hdivConfig.areEditableParameterValuesValid(targetWithoutContextPath, clientId, content,
					contentType);
		}
		return result;
	}

	public void setHdivConfig(HDIVConfig hdivConfig) {
		this.hdivConfig = hdivConfig;
	}

	/**
	 * Gets the part of the url that represents the action to be executed in
	 * this request.
	 * 
	 * @param request
	 *            HttpServletRequest to validate
	 * @return target Part of the url that represents the target action
	 * @throws HDIVException
	 */
	protected String getTarget(HttpServletRequest request) {
		try {
			return HDIVUtil.actionName(request);
		} catch (Exception e) {
			String errorMessage = HDIVUtil.getMessage("helper.actionName");
			throw new HDIVException(errorMessage, e);
		}
	}

	/**
	 * Removes the target's ContextPath part
	 * 
	 * @param request
	 *            HttpServletRequest to validate
	 * @param target
	 *            target to stripp the ContextPath
	 * @return target without the ContextPath
	 */
	protected String getTargetWithoutContextPath(HttpServletRequest request, String target) {
		String targetWithoutContextPath = target.substring(request.getContextPath().length());
		return targetWithoutContextPath;
	}
}
