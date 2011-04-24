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
import org.springframework.web.servlet.tags.form.HiddenInputTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Data-binding aware JSP tag for rendering a hidden HTML '<code>input</code>'
 * field containing the databound value.
 * 
 * <p>
 * Example (binding to 'name' property of form backing object):
 * 
 * <pre class="code>
 * &lt;form:hidden path=&quot;name&quot;/&gt;
 * </pre>
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see org.springframework.web.servlet.tags.form.HiddenInputTag
 */
public class HiddenInputTagHDIV extends HiddenInputTag {

	/**
	 * Writes the HTML '<code>input</code>' tag to the supplied
	 * {@link TagWriter} including the databound value.
	 * 
	 * @see #writeDefaultAttributes(TagWriter)
	 * @see #getBoundValue()
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", "hidden");

		String displayValue = getDisplayString(getBoundValue(), getPropertyEditor());
		String displayName = getDisplayString(evaluate("name", getName()));

		IDataComposer dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);
		String hdivValue = dataComposer.compose(displayName, displayValue, false);

		tagWriter.writeAttribute("value", hdivValue);
		tagWriter.endTag();
		
		return SKIP_BODY;
	}

}
