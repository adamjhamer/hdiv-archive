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
import org.springframework.web.servlet.tags.form.RadioButtonTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Databinding-aware JSP tag for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>radio</code>'.
 *
 * <p>Rendered elements are marked as 'checked' if the configured
 * {@link #setValue(Object) value} matches the {@link #getValue bound value}.
 *
 * <p>A typical usage pattern will involved multiple tag instances bound
 * to the same property but with different values.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see org.springframework.web.servlet.tags.form.RadioButtonTag
 */
public class RadioButtonTagHDIV extends RadioButtonTag {	
	
	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking the
	 * '<code>input</code>' element as 'checked' if the supplied value matches the
	 * bound value.
	 */
	@Override
	protected void renderFromValue(Object item, Object value, TagWriter tagWriter) throws JspException {
				
		IDataComposer dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);		

		String displayValue = convertToDisplayString(value);

		String hdivValue = dataComposer.compose(super.getName(), displayValue, false);		
				
		tagWriter.writeAttribute("value", hdivValue);
		if (isOptionSelected(value) || (value != item && isOptionSelected(item))) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}	
	
	/**
	 * Determines whether the supplied value matched the selected value
	 * through delegating to {@link SelectedValueComparator#isSelected}.
	 */
	private boolean isOptionSelected(Object value) throws JspException {
		return SelectedValueComparatorHDIV.isSelected(getBindStatus(), value);
	}	

}
