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

import javax.servlet.jsp.JspException;

import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.web.util.TagUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.tags.form.CheckboxesTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Databinding-aware JSP tag for rendering multiple HTML '<code>input</code>'
 * elements with a '<code>type</code>' of '<code>checkbox</code>'.
 * 
 * <p>
 * Intended to be used with a Collection as the {@link #getItems()} bound
 * value}.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 */
public class CheckboxesTagHDIV extends CheckboxesTag {

	private IDataComposer dataComposer;

	
	/**
	 * Renders the '<code>input type="radio"</code>' element with the configured
	 * {@link #setItems(Object)} values. Marks the element as checked if the
	 * value matches the bound value.
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		
		dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);
		super.writeTagContent(tagWriter);
		
		if (!isDisabled()) {
			String hdivValue = dataComposer.compose(WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + getName(), "on", false);
			// Write out the 'field was present' marker.
			tagWriter.startTag("input");
			tagWriter.writeAttribute("type", "hidden");
			tagWriter.writeAttribute("name", WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + getName());
			tagWriter.writeAttribute("value", hdivValue);
			tagWriter.endTag();
		}
		return SKIP_BODY;
	}

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking the
	 * '<code>input</code>' element as 'checked' if the supplied value matches the
	 * bound value.
	 */
	@Override
	protected void renderFromValue(Object item, Object value, TagWriter tagWriter) throws JspException {

		String displayValue = convertToDisplayString(value);

		dataComposer.compose(getName(), displayValue, true);
		
		tagWriter.writeAttribute("value", displayValue);
		if (isOptionSelected(value) || (value != item && isOptionSelected(item))) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}
	
	/**
	 * Determines whether the supplied value matched the selected value
	 * through delegating to {@link SelectedValueComparatorHDIV#isSelected}.
	 */
	private boolean isOptionSelected(Object value) throws JspException {
		return SelectedValueComparatorHDIV.isSelected(getBindStatus(), value);
	}	

}