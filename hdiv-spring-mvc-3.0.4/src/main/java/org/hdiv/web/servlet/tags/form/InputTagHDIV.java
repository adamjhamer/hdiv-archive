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
import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Data-binding-aware JSP tag for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>text</code>'.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see org.springframework.web.servlet.tags.form.InputTag
 */
public class InputTagHDIV extends InputTag {
	 
	/**
	 * Writes the '<code>input</code>' tag to the supplied {@link TagWriter}.
	 * Uses the value returned by {@link #getType()} to determine which
	 * type of '<code>input</code>' element to render.
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {

		IDataComposer dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);		
		dataComposer.compose(super.getName(), "", true, this.getType());
		
		return super.writeTagContent(tagWriter);
	}

}
