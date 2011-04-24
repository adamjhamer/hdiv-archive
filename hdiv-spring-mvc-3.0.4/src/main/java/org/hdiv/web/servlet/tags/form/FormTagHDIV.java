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

package org.hdiv.web.servlet.tags.form;

import java.util.StringTokenizer;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.util.HDIVUtil;
import org.hdiv.web.util.TagUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.core.Conventions;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.tags.form.AbstractFormTag;
import org.springframework.web.servlet.tags.form.AbstractHtmlElementTag;
import org.springframework.web.servlet.tags.form.TagWriter;
import org.springframework.web.util.HtmlUtils;

/**
 * Databinding-aware JSP tag for rendering an HTML '<code>form</code>' whose
 * inner elements are bound to properties on a
 * {@link #setCommandName command object}.
 * 
 * <p>
 * Users should place the command object into the
 * {@link org.springframework.web.servlet.ModelAndView} when populating the data
 * for their view. The name of this command object can be configured using the
 * {@link #setCommandName commandName} property.
 * 
 * <p>
 * The default value for the {@link #setCommandName commandName} property is '<code>command</code>'
 * which corresponds to the default name when using the
 * {@link org.springframework.web.servlet.mvc.SimpleFormController}.
 * 
 * <p>
 * Inner tags can access the name of the command object via the
 * {@link javax.servlet.jsp.PageContext}. The attribute name is defined in
 * {@link #COMMAND_NAME_VARIABLE_NAME}.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.1.0
 * @see org.springframework.web.servlet.tags.form.FormTag
 */
public class FormTagHDIV extends AbstractHtmlElementTag {

	/**
	 * Commons Logging instance.
	 */
	private static Log log = LogFactory.getLog(FormTagHDIV.class);
	
	/** The default HTTP method using which form values are sent to the server: "post" */
	private static final String DEFAULT_METHOD = "post";

	/** The default attribute name: &quot;command&quot; */
	public static final String DEFAULT_COMMAND_NAME = "command";

	/** The name of the '<code>modelAttribute</code>' setting */
	private static final String MODEL_ATTRIBUTE = "modelAttribute";

	/**
	 * The name of the {@link javax.servlet.jsp.PageContext} attribute under which the
	 * form object name is exposed.
	 */
	public static final String MODEL_ATTRIBUTE_VARIABLE_NAME =
			Conventions.getQualifiedAttributeName(AbstractFormTag.class, MODEL_ATTRIBUTE);

	/** Default method parameter, i.e. <code>_method</code>. */
	private static final String DEFAULT_METHOD_PARAM = "_method";

	private static final String FORM_TAG = "form";

	private static final String INPUT_TAG = "input";

	private static final String ACTION_ATTRIBUTE = "action";

	private static final String METHOD_ATTRIBUTE = "method";

	private static final String TARGET_ATTRIBUTE = "target";

	private static final String ENCTYPE_ATTRIBUTE = "enctype";

	private static final String ACCEPT_CHARSET_ATTRIBUTE = "accept-charset";

	private static final String ONSUBMIT_ATTRIBUTE = "onsubmit";

	private static final String ONRESET_ATTRIBUTE = "onreset";

	private static final String AUTOCOMPLETE_ATTRIBUTE = "autocomplete";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String TYPE_ATTRIBUTE = "type";


	private TagWriter tagWriter;

	private String modelAttribute = DEFAULT_COMMAND_NAME;

	private String name;

	private String action;

	private String method = DEFAULT_METHOD;

	private String target;

	private String enctype;

	private String acceptCharset;

	private String onsubmit;

	private String onreset;

	private String autocomplete;

	private String methodParam = DEFAULT_METHOD_PARAM;

	/** Caching a previous nested path, so that it may be reset */
	private String previousNestedPath;

	private IDataComposer dataComposer;

	/**
	 * Set the name of the form attribute in the model.
	 * <p>May be a runtime expression.
	 */
	public void setModelAttribute(String modelAttribute) {
		this.modelAttribute = modelAttribute;
	}

	/**
	 * Get the name of the form attribute in the model.
	 */
	protected String getModelAttribute() {
		return this.modelAttribute;
	}

	/**
	 * Set the name of the form attribute in the model.
	 * <p>May be a runtime expression.
	 * @see #setModelAttribute
	 */
	public void setCommandName(String commandName) {
		this.modelAttribute = commandName;
	}

	/**
	 * Get the name of the form attribute in the model.
	 * @see #getModelAttribute
	 */
	protected String getCommandName() {
		return this.modelAttribute;
	}

	/**
	 * Set the value of the '<code>name</code>' attribute.
	 * <p>May be a runtime expression.
	 * <p>Name is not a valid attribute for form on XHTML 1.0. However,
	 * it is sometimes needed for backward compatibility.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the value of the '<code>name</code>' attribute.
	 */
	@Override
	protected String getName() throws JspException {
		return this.name;
	}

	/**
	 * Set the value of the '<code>action</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setAction(String action) {
		this.action = (action != null ? action : "");
	}

	/**
	 * Get the value of the '<code>action</code>' attribute.
	 */
	protected String getAction() {
		return this.action;
	}

	/**
	 * Set the value of the '<code>method</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Get the value of the '<code>method</code>' attribute.
	 */
	protected String getMethod() {
		return this.method;
	}

	/**
	 * Set the value of the '<code>target</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * Get the value of the '<code>target</code>' attribute.
	 */
	public String getTarget() {
		return this.target;
	}

	/**
	 * Set the value of the '<code>enctype</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setEnctype(String enctype) {
		this.enctype = enctype;
	}

	/**
	 * Get the value of the '<code>enctype</code>' attribute.
	 */
	protected String getEnctype() {
		return this.enctype;
	}

	/**
	 * Set the value of the '<code>acceptCharset</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setAcceptCharset(String acceptCharset) {
		this.acceptCharset = acceptCharset;
	}

	/**
	 * Get the value of the '<code>acceptCharset</code>' attribute.
	 */
	protected String getAcceptCharset() {
		return this.acceptCharset;
	}

	/**
	 * Set the value of the '<code>onsubmit</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setOnsubmit(String onsubmit) {
		this.onsubmit = onsubmit;
	}

	/**
	 * Get the value of the '<code>onsubmit</code>' attribute.
	 */
	protected String getOnsubmit() {
		return this.onsubmit;
	}

	/**
	 * Set the value of the '<code>onreset</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	public void setOnreset(String onreset) {
		this.onreset = onreset;
	}

	/**
	 * Get the value of the '<code>onreset</code>' attribute.
	 */
	protected String getOnreset() {
		return this.onreset;
	}

	/**
	 * Set the value of the '<code>autocomplete</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setAutocomplete(String autocomplete) {
		this.autocomplete = autocomplete;
	}

	/**
	 * Get the value of the '<code>autocomplete</code>' attribute.
	 */
	protected String getAutocomplete() {
		return this.autocomplete;
	}

	/**
	 * Set the name of the request param for non-browser supported HTTP methods.
	 */
	public void setMethodParam(String methodParam) {
		this.methodParam = methodParam;
	}

	/**
	 * Get the name of the request param for non-browser supported HTTP methods.
	 */
	protected String getMethodParameter() {
		return this.methodParam;
	}

	/**
	 * Determine if the HTTP method is supported by browsers (i.e. GET or POST).
	 */
	protected boolean isMethodBrowserSupported(String method) {
		return ("get".equalsIgnoreCase(method) || "post".equalsIgnoreCase(method));
	}



	/**
	 * Writes the opening part of the block	'<code>form</code>' tag and exposes
	 * the form object name in the {@link javax.servlet.jsp.PageContext}.
	 * @param tagWriter the {@link TagWriter} to which the form content is to be written
	 * @return {@link javax.servlet.jsp.tagext.Tag#EVAL_BODY_INCLUDE}
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		this.tagWriter = tagWriter;

		tagWriter.startTag(FORM_TAG);
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute(ACTION_ATTRIBUTE, resolveAction());
		writeOptionalAttribute(tagWriter, METHOD_ATTRIBUTE, isMethodBrowserSupported(getMethod()) ? getMethod() : DEFAULT_METHOD);
		writeOptionalAttribute(tagWriter, TARGET_ATTRIBUTE, getTarget());
		writeOptionalAttribute(tagWriter, ENCTYPE_ATTRIBUTE, getEnctype());
		writeOptionalAttribute(tagWriter, ACCEPT_CHARSET_ATTRIBUTE, getAcceptCharset());
		writeOptionalAttribute(tagWriter, ONSUBMIT_ATTRIBUTE, getOnsubmit());
		writeOptionalAttribute(tagWriter, ONRESET_ATTRIBUTE, getOnreset());
		writeOptionalAttribute(tagWriter, AUTOCOMPLETE_ATTRIBUTE, getAutocomplete());

		tagWriter.forceBlock();

		if (!isMethodBrowserSupported(getMethod())) {
			
			String composedValue = dataComposer.compose(getMethodParameter(), getMethod(), false);
			
			tagWriter.startTag(INPUT_TAG);
			writeOptionalAttribute(tagWriter, TYPE_ATTRIBUTE, "hidden");
			writeOptionalAttribute(tagWriter, NAME_ATTRIBUTE, getMethodParameter());
			writeOptionalAttribute(tagWriter, VALUE_ATTRIBUTE, composedValue);
			tagWriter.endTag();
		}

		// Expose the form object name for nested tags...
		String modelAttribute = resolveModelAttribute();
		this.pageContext.setAttribute(MODEL_ATTRIBUTE_VARIABLE_NAME, modelAttribute, PageContext.REQUEST_SCOPE);
		this.pageContext.setAttribute(COMMAND_NAME_VARIABLE_NAME, modelAttribute, PageContext.REQUEST_SCOPE);

		// Save previous nestedPath value, build and expose current nestedPath value.
		// Use request scope to expose nestedPath to included pages too.
		this.previousNestedPath =
				(String) this.pageContext.getAttribute(NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
		this.pageContext.setAttribute(NESTED_PATH_VARIABLE_NAME,
				modelAttribute + PropertyAccessor.NESTED_PROPERTY_SEPARATOR, PageContext.REQUEST_SCOPE);

		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Autogenerated IDs correspond to the form object name.
	 */
	@Override
	protected String autogenerateId() throws JspException {
		return resolveModelAttribute();
	}

	/**
	 * {@link #evaluate Resolves} and returns the name of the form object.
	 * @throws IllegalArgumentException if the form object resolves to <code>null</code>
	 */
	protected String resolveModelAttribute() throws JspException {
		Object resolvedModelAttribute = evaluate(MODEL_ATTRIBUTE, getModelAttribute());
		if (resolvedModelAttribute == null) {
			throw new IllegalArgumentException(MODEL_ATTRIBUTE + " must not be null");
		}
		return (String) resolvedModelAttribute;
	}

	/**
	 * Resolve the value of the '<code>action</code>' attribute.
	 * <p>If the user configured an '<code>action</code>' value then
	 * the result of evaluating this value is used. Otherwise, the
	 * {@link org.springframework.web.servlet.support.RequestContext#getRequestUri() originating URI}
	 * is used.
	 * @return the value that is to be used for the '<code>action</code>' attribute
	 */
	protected String resolveAction() throws JspException {
		
		dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);
		
		String action = getAction();
		if (StringUtils.hasText(action)) {
			
			if (!action.startsWith("/")) {
				String requestUri = getRequestContext().getRequestUri();
				int lastSlash = requestUri.lastIndexOf('/');			
				if (lastSlash >= 0) {
					action = requestUri.substring(0, lastSlash) + "/" + action;
				} else {
					action = "/" + action;
				}
			}			
			
			String displayString = getDisplayString(evaluate(ACTION_ATTRIBUTE, action));
			
			String beginAction = displayString;
			if (displayString.contains("?")) {
				beginAction = displayString.substring(0, displayString.indexOf("?"));
			}
			dataComposer.beginRequest(HDIVUtil.getActionMappingName(beginAction));
			
			if (displayString.contains("?")) {
				String encodedParams = this.composeQueryString(displayString.substring(displayString.indexOf("?") + 1));
				displayString = beginAction + "?" + encodedParams;
			}
			
			return displayString;

		} else { // action == null
			String requestUri = getRequestContext().getRequestUri();
			ServletResponse response = this.pageContext.getResponse();

			if (response instanceof HttpServletResponse) {

				requestUri = ((HttpServletResponse) response).encodeURL(requestUri);				
				dataComposer.beginRequest(HDIVUtil.getActionMappingName(requestUri));

				String queryString = getRequestContext().getQueryString();
				if (StringUtils.hasText(queryString)) {

					queryString = this.composeQueryString(queryString);
					requestUri += "?" + HtmlUtils.htmlEscape(queryString);
				}
			}
			if (StringUtils.hasText(requestUri)) {
				return requestUri;
			} else {
				throw new IllegalArgumentException("Attribute 'action' is required. Attempted to resolve "
						+ "against current request URI but request URI was null");
			}
		}
	}

	/**
	 * Removes HDIV parameter from <code>queryString</code> and it composes
	 * other parameters.
	 * 
	 * @param queryString query string
	 * @return queryString without HDIV's parameter
	 */
	protected String composeQueryString(String queryString) {

		String token = null;
		StringBuffer result = new StringBuffer();
		
		StringTokenizer st = new StringTokenizer(queryString, "&");
		while (st.hasMoreTokens()) {

			token = st.nextToken();
			String param = token.substring(0, token.indexOf("="));

			if (!ignoreParameter(param)) {

				String originalValue = this.pageContext.getRequest().getParameter(param);
				String val = dataComposer.compose(param, originalValue, false);
								
				if (result.length() > 0) {
					result.append("&");
				}
				result.append(param + "=" + val);
			}
		}
		return result.toString();
	}

	/**
	 * @returns Returns true if parameter <code>param</code> must be ignored.
	 *          False otherwise.
	 */
	protected boolean ignoreParameter(String param) {
		
		String hdivParameter = (String) HDIVUtil.getHttpSession().getAttribute("HDIVParameter");
		return param.equalsIgnoreCase(hdivParameter);
	}
	
	/**
	 * Closes the '<code>form</code>' block tag and removes the form object name
	 * from the {@link javax.servlet.jsp.PageContext}.
	 */
	@Override
	public int doEndTag() throws JspException {
		
		addExtraParameters();
		
		this.tagWriter.endTag();
		return EVAL_PAGE;
	}
	
	/**
	 * Adds new parameters to the form that are not been defined with HTML tags.
	 */
	public void addExtraParameters() throws JspException {
		
		if (!isMethodBrowserSupported(getMethod())) {
			
			// TODO: should not be editable. It is well for now because I know how to solve it...!!
			String hdivValue = dataComposer.compose(getMethodParameter(), getMethod(), true);
		}
		
		this.addHDIVParameter();
	}
	
	/**
	 * Adds HDIV state as parameter.
	 */
	protected void addHDIVParameter() throws JspException {
		
		String requestId = dataComposer.endRequest();

		if (requestId.length() > 0) {
			String hdivState = (String) HDIVUtil.getHttpSession().getAttribute("HDIVParameter");
			this.tagWriter.appendValue(this.generateHiddenTag(hdivState, requestId));
		}
	}
	
	/**
	 * Renders an HTML <b>&lt;input&gt;</b> element of type hidden.
	 * 
	 * @param name hidden parameter name
	 * @param requestId request identification
	 * @return HTML <b>&lt;input&gt;</b> element of type hidden
	 */
	protected String generateHiddenTag(String name, String requestId) {

		StringBuffer hdivParameter = new StringBuffer();

		hdivParameter.append("<input type=\"hidden\"");
		renderAttribute(hdivParameter, "name", name);
		renderAttribute(hdivParameter, "value", requestId);
		hdivParameter.append(">\n");

		return hdivParameter.toString();
	}
	
	/**
	 * Prepares an attribute if the <code>value</code> is not null, appending
	 * it to the the given StringBuffer <code>result</code>.
	 * 
	 * @param result The StringBuffer that output will be appended to.
	 */
	private void renderAttribute(StringBuffer result, String name, String value) {

		if (value != null) {
			result.append(" ");
			result.append(name);
			result.append("=\"");
			result.append(value);
			result.append("\"");
		}
	}

	public IDataComposer getDataComposer() {
		return dataComposer;
	}

	/**
	 * Clears the stored {@link TagWriter}.
	 */
	@Override
	public void doFinally() {
		super.doFinally();
		this.pageContext.removeAttribute(MODEL_ATTRIBUTE_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
		this.pageContext.removeAttribute(COMMAND_NAME_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
		if (this.previousNestedPath != null) {
			// Expose previous nestedPath value.
			this.pageContext.setAttribute(NESTED_PATH_VARIABLE_NAME, this.previousNestedPath, PageContext.REQUEST_SCOPE);
		}
		else {
			// Remove exposed nestedPath value.
			this.pageContext.removeAttribute(NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
		}
		this.tagWriter = null;
		this.previousNestedPath = null;
	}


	/**
	 * Override resolve CSS class since error class is not supported.
	 */
	@Override
	protected String resolveCssClass() throws JspException {
		return ObjectUtils.getDisplayString(evaluate("cssClass", getCssClass()));
	}

	/**
	 * Unsupported for forms.
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setPath(String path) {
		throw new UnsupportedOperationException("The 'path' attribute is not supported for forms");
	}

	/**
	 * Unsupported for forms.
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void setCssErrorClass(String cssErrorClass) {
		throw new UnsupportedOperationException("The 'cssErrorClass' attribute is not supported for forms");
	}

	/**
	 * @return the tag writer
	 */
	public TagWriter getTagWriter() {
		return tagWriter;
	}

}
