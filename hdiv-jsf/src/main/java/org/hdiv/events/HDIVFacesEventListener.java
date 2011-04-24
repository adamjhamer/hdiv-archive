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

package org.hdiv.events;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.StateHolder;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.exception.StateValidationException;
import org.hdiv.logger.LoggerUtil;
import org.hdiv.validation.ValidationError;
import org.hdiv.validators.ComponentValidator;
import org.hdiv.validators.EditableValidator;

/**
 * <p>
 * Listener that processes a HDIV event. This class validates the component
 * tree searching for modifications in the values of the non editable data.
 * </p>
 * <p>
 * Validation logic for each type of component is stored in a separate
 * class that implements ComponentValidator.
 * </p>
 * <p>
 * Implementa el interfaz StateHolder para marcarlo como transient y que no se
 * guarde en el estado de JSF.
 * Implements StateHolder interface to set it as transient and don't store it
 * in HDIV state.
 * </p>
 * 
 * @author Gotzon Illarramendi
 */
public class HDIVFacesEventListener implements FacesListener, StateHolder {

	private static Log log = LogFactory.getLog(HDIVFacesEventListener.class);

	/**
	 * Parameter validator
	 */
	private ComponentValidator requestParamValidator;

	/**
	 * UICommand components validator
	 */
	private ComponentValidator uiCommandValidator;

	/**
	 * HtmlInputHidden components validator
	 */
	private ComponentValidator htmlInputHiddenValidator;

	/**
	 * Editable data validator
	 */
	private EditableValidator editabeValidator;

	/**
	 * HDIV config
	 */
	private HDIVConfig config;

	/**
	 * Constructor
	 */
	public HDIVFacesEventListener() {
		if (log.isDebugEnabled()) {
			log.debug("Constructor de HDIVFacesEventListener");
		}

	}

	/**
	 * Process a HDIVFacesEvent event
	 * 
	 * @param facesEvent
	 *            Evento de HDIV
	 */
	public void processListener(HDIVFacesEvent facesEvent) {

		if (log.isDebugEnabled()) {
			log.debug("Procesando evento de HDIV");
		}

		FacesContext context = FacesContext.getCurrentInstance();

		UICommand eventComp = (UICommand) facesEvent.getComponent();

		// Search form component
		UIForm form = this.findParentForm(eventComp);

		// Validate request parameters
		ValidationError error = this.requestParamValidator.validate(form);
		if (error != null) {
			LoggerUtil.log(error, context);
			forwardToErrorPage(eventComp);
		}

		// Validate component parameters
		error = this.uiCommandValidator.validate(eventComp);
		if (error != null) {
			LoggerUtil.log(error, context);
			forwardToErrorPage(eventComp);
		}

		// Validate all the hidden components in the form
		error = this.validateHiddens(form);
		if (error != null) {
			LoggerUtil.log(error, context);
			forwardToErrorPage(eventComp);
		}

		error = this.editabeValidator.validate(form);
		if (error != null) {
			LoggerUtil.log(error, context);
		}
	}
	
	/**
	 * Searches the form inside the component. Input component must be
	 * UICommand type and must be inside a form.
	 * 
	 * @param comp
	 *            Base component
	 * @return UIForm component
	 */
	
	private UIForm findParentForm(UIComponent comp) {

		UIComponent parent = comp.getParent();
		while (!(parent instanceof UIForm)) {
			parent = parent.getParent();
		}
		return (UIForm) parent;

	}

	/**
	 * Validates HtmlInputHidden components inside the form
	 * 
	 * @param component
	 *            UIForm component
	 * @return validation result
	 */
	private ValidationError validateHiddens(UIComponent component) {

		for (Iterator it = component.getChildren().iterator(); it.hasNext();) {
			UIComponent uicomponent = (UIComponent) it.next();
			if (uicomponent instanceof HtmlInputHidden) {

				HtmlInputHidden hidden = (HtmlInputHidden) uicomponent;
				ValidationError error = this.htmlInputHiddenValidator.validate(hidden);
				if (error != null) {
					return error;
				}
			} else {
				ValidationError error = validateHiddens(uicomponent);
				if (error != null) {
					return error;
				}
			}
		}
		return null;
	}

	/**
	 * Redirige la ejecucion a la pagina de error de HDIV
	 * Redirects the execution to the HDIV error page 
	 * 
	 * @param comp
	 *            component which throws the event
	 */
	private void forwardToErrorPage(UICommand comp) {
		if (!comp.isImmediate()) {
			// Redirect a la pagina de error de hdiv
			FacesContext fContext = FacesContext.getCurrentInstance();
			try {
				String contextPath = fContext.getExternalContext().getRequestContextPath();
				fContext.getExternalContext().redirect(contextPath + this.config.getErrorPage());
			} catch (IOException e) {
				throw new StateValidationException();
			}
		} else {
			// Previous strategy doesn't work with immediate components because
			// the execution of business logic continues running-
			// An exception is thrown to be catched by the ExceptionHandler (JSF2) 
			throw new StateValidationException();
		}

	}

	/**
	 * Se marca como transient para que no se almacene en el estado de JSF
	 * It is set as trasient to avoid storing in the JSF state
	 */
	public boolean isTransient() {

		return true;
	}

	/**
	 * As the listener is transient this method isn't called
	 */
	public void setTransient(boolean newTransientValue) {

	}

	/**
	 * As the listener is transient this method isn't called
	 */
	public Object saveState(FacesContext context) {
		return null;
	}

	/**
	 * As the listener is transient this method isn't called
	 */
	public void restoreState(FacesContext context, Object state) {

	}

	public void setRequestParamValidator(ComponentValidator requestParamValidator) {
		this.requestParamValidator = requestParamValidator;
	}

	public void setUiCommandValidator(ComponentValidator uiCommandValidator) {
		this.uiCommandValidator = uiCommandValidator;
	}

	public void setHtmlInputHiddenValidator(ComponentValidator htmlInputHiddenValidator) {
		this.htmlInputHiddenValidator = htmlInputHiddenValidator;
	}

	public void setConfig(HDIVConfig config) {
		this.config = config;
	}

	public EditableValidator getEditabeValidator() {
		return editabeValidator;
	}

	public void setEditabeValidator(EditableValidator editabeValidator) {
		this.editabeValidator = editabeValidator;
	}

}
