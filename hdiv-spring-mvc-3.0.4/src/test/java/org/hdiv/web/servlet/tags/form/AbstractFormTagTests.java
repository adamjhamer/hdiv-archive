/*
 * Copyright 2005-2008 hdiv.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hdiv.web.servlet.tags.form;

import javax.servlet.jsp.JspException;

import org.hdiv.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;
import org.springframework.web.servlet.tags.form.FormTag;

/**
 * @author Gorka Vicente
 * @since 2.0.6
 */
public abstract class AbstractFormTagTests extends AbstractHtmlElementTagTests {

	private FormTag formTag = new FormTag();
	
	protected void extendRequest(MockHttpServletRequest request) {
		request.setAttribute(AbstractFormTagTests.COMMAND_NAME, createTestBean());
	}

	protected abstract TestBean createTestBean();
	
	protected void extendPageContext(MockPageContext pageContext) throws JspException {
		this.formTag.setCommandName(COMMAND_NAME);
		this.formTag.setAction("myAction");
		this.formTag.setPageContext(pageContext);
		this.formTag.doStartTag();
	}

	protected final FormTag getFormTag() {
		return this.formTag;
	}

}
