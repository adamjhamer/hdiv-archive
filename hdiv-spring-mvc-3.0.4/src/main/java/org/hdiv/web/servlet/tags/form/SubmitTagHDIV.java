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
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.tags.form.AbstractHtmlInputElementTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * JSP tag for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>submit</code>'.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see org.springframework.web.servlet.tags.form.AbstractHtmlInputElementTag
 */
public class SubmitTagHDIV extends AbstractHtmlInputElementTag {
	
	private String value;
	
	private String name;
	
	/**
	 * Set the value of the '<code>value</code>' attribute.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Get the value of the '<code>value</code>' attribute.
	 */
	protected String getValue() {
		return this.value;
	}	
	
	
	/**
	 * Writes the '<code>input</code>' tag to the supplied {@link TagWriter}.
	 * Uses the value returned by {@link #getType()} to determine which
	 * type of '<code>input</code>' element to render.
	 */
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		
		tagWriter.startTag("input");

		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", getType());
		writeValue(tagWriter);

		tagWriter.endTag();
		return EVAL_PAGE;
	}	
	
	/**
	 * Writes the '<code>value</code>' attribute to the supplied
	 * {@link TagWriter}. Subclasses may choose to override this implementation
	 * to control exactly when the value is written.
	 */
	protected void writeValue(TagWriter tagWriter) throws JspException {

		if (getName() != null) {
			IDataComposer dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(TagUtils.DATA_COMPOSER);
			dataComposer.compose(getName(), "", true);
		}

		String label = (getValue() != null) ? getValue() : getDefaultValue();  
		tagWriter.writeAttribute("value", label);
	}
	
	/**
	 * Gets the appropriate CSS class to use.
	 */
	protected String resolveCssClass() throws JspException {
		return ObjectUtils.getDisplayString(evaluate("cssClass", getCssClass()));
	}	

    /**
     * Return the default value.
     *
     * @return The default value if none supplied.
     */
    protected String getDefaultValue() {
        return "Submit";
    }	
	
	/**
	 * Get the value of the '<code>type</code>' attribute. Subclasses can
	 * override this to change the type of '<code>input</code>' element
	 * rendered.
	 */
	protected String getType() {
		return "submit";
	}

	/**
	 * Set the value of the '<code>name</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setName(String name) {
		this.name = name;
	}	
	
	/**
	 * Get the value of the '<code>name</code>' attribute.
	 */
	protected String getName() throws JspException {
		return this.name;
	}	
	
}
