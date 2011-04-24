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

import java.util.Collection;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.web.util.TagUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.tags.form.OptionTag;
import org.springframework.web.servlet.tags.form.OptionsTag;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Databinding-aware JSP tag that renders an HTML '<code>select</code>'
 * element.
 *
 * <p>Inner '<code>option</code>' tags can be rendered using one of the
 * approaches supported by the OptionWriter class.
 *
 * <p>Also supports the use of nested {@link OptionTag OptionTags} or
 * (typically one) nested {@link OptionsTag}.
 *
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see org.springframework.web.servlet.tags.form.SelectTag
 */
public class SelectTagHDIV extends SelectTag {

	private IDataComposer dataComposer;
	 
	/**
	 * Marker object for items that have been specified but resolve to null.
	 * Allows to differentiate between 'set but null' and 'not set at all'.
	 */
	private static final Object EMPTY = new Object();
	
	/**
	 * The {@link TagWriter} instance that the output is being written.
	 * <p>Only used in conjunction with nested {@link OptionTag OptionTags}.
	 */
	private TagWriter tagWriter;
	
	/**
	 * Renders the HTML '<code>select</code>' tag to the supplied
	 * {@link TagWriter}.
	 * <p>Renders nested '<code>option</code>' tags if the
	 * {@link #setItems items} property is set, otherwise exposes the
	 * bound value for the nested {@link OptionTag OptionTags}.
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		
		dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);		
		dataComposer.compose(this.getName(), "", false);		
		
		tagWriter.startTag("select");
		writeDefaultAttributes(tagWriter);
		if (isMultiple()) {
			tagWriter.writeAttribute("multiple", "multiple");
		}
		tagWriter.writeOptionalAttributeValue("size", getDisplayString(evaluate("size", getSize())));

		Object items = getItems();
		if (items != null) {
			// Items specified, but might still be empty...
			if (items != EMPTY) {
				Object itemsObject = evaluate("items", items);
				if (itemsObject != null) {
					
					String valueProperty = (getItemValue() != null ? ObjectUtils.getDisplayString(evaluate("itemValue", getItemValue())) : null);
					String labelProperty = (getItemLabel() != null ? ObjectUtils.getDisplayString(evaluate("itemLabel", getItemLabel())) : null);
					
					OptionWriterHDIV optionWriter =	new OptionWriterHDIV(dataComposer, this.getName(), itemsObject, getBindStatus(), valueProperty, labelProperty, isHtmlEscape());
					optionWriter.writeOptions(tagWriter);
				}
			}
			tagWriter.endTag(true);
			writeHiddenTagIfNecessary(tagWriter);
			return SKIP_BODY;
		}
		else {
			// Using nested <form:option/> tags, so just expose the value in the PageContext...
			tagWriter.forceBlock();
			this.tagWriter = tagWriter;
			this.pageContext.setAttribute(LIST_VALUE_PAGE_ATTRIBUTE, getBindStatus());
			return EVAL_BODY_INCLUDE;
		}
	}

	/**
	 * If using a multi-select, a hidden element is needed to make sure all
	 * items are correctly unselected on the server-side in response to a
	 * <code>null</code> post.
	 */
	private void writeHiddenTagIfNecessary(TagWriter tagWriter) throws JspException {
		
		if (isMultiple()) {
			
			String hdivValue = dataComposer.compose(WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + getName(), "1", false);
			tagWriter.startTag("input");
			tagWriter.writeAttribute("type", "hidden");
			tagWriter.writeAttribute("name", WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + getName());
			tagWriter.writeAttribute("value", hdivValue);
			tagWriter.endTag();
		}
	}

	private boolean isMultiple() throws JspException {
		
		Object multiple = getMultiple();
		if (Boolean.TRUE.equals(multiple) || "multiple".equals(multiple)) {
			return true;
		}
		else if (super.getMultiple() instanceof String) {
			return evaluateBoolean("multiple", (String) multiple);
		}
		return forceMultiple();
	}

	/**
	 * Returns '<code>true</code>' if the bound value requires the
	 * resultant '<code>select</code>' tag to be multi-select.
	 */
	private boolean forceMultiple() throws JspException {
		
		BindStatus bindStatus = getBindStatus();
		Class valueType = bindStatus.getValueType();
		if (valueType != null && typeRequiresMultiple(valueType)) {
			return true;
		
		} else if (bindStatus.getEditor() != null) {
			
			Object editorValue = bindStatus.getEditor().getValue();
			if (editorValue != null && typeRequiresMultiple(editorValue.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns '<code>true</code>' for arrays, {@link Collection Collections}
	 * and {@link Map Maps}.
	 */
	private static boolean typeRequiresMultiple(Class type) {
		return (type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));
	}

	/**
	 * Get the {@link BindStatus} for this tag.
	 */
	@Override
	protected BindStatus getBindStatus() throws JspException {
		return super.getBindStatus();
	}
	
	/**
	 * Get the value for the HTML '<code>name</code>' attribute.
	 * <p>The default implementation simply delegates to
	 * {@link #getCompletePath()} to use the property path as the name.
	 * For the most part this is desirable as it links with the server-side
	 * expectation for databinding. However, some subclasses may wish to change
	 * the value of the '<code>name</code>' attribute without changing the bind path.
	 * @return the value for the HTML '<code>name</code>' attribute
	 */
	@Override
	protected String getName() throws JspException {		
		return super.getName();
	}

	/**
	 * Set the {@link Collection}, {@link Map} or array of objects used to
	 * generate the inner '<code>option</code>' tags.
	 * <p>Required when wishing to render '<code>option</code>' tags from
	 * an array, {@link Collection} or {@link Map}.
	 * <p>Typically a runtime expression.
	 * @param items the items that comprise the options of this selection
	 */
	@Override
	public void setItems(Object items) {
		super.setItems(items != null ? items : EMPTY);
	}

	/**
	 * Closes any block tag that might have been opened when using
	 * nested {@link OptionTag options}.
	 */
	@Override
	public int doEndTag() throws JspException {
		if (this.tagWriter != null) {
			this.tagWriter.endTag();
			writeHiddenTagIfNecessary(tagWriter);
		}
		return EVAL_PAGE;
	}

	/**
	 * Clears the {@link TagWriter} that might have been left over when using
	 * nested {@link OptionTag options}.
	 */
	@Override
	public void doFinally() {
		super.doFinally();
		this.tagWriter = null;
		this.pageContext.removeAttribute(LIST_VALUE_PAGE_ATTRIBUTE);
	}	
	
}
