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

import javax.servlet.jsp.tagext.Tag;

import org.hdiv.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.tags.form.TagWriter;


public class HiddenInputTagTests extends AbstractFormTagTests {

	private HiddenInputTagHDIV tag;

	private TestBean bean;
	
	private boolean confidentiality;

	protected void onSetUp() {
		this.tag = new HiddenInputTagHDIV() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
		confidentiality = ((Boolean) getHdivContext().getBean("confidentiality")).booleanValue();
		
		MockHttpServletRequest request = (MockHttpServletRequest) super.getPageContext().getRequest();
		request.setAttribute("name", "name");
	}
	
	public void testRender() throws Exception {
		this.tag.setPath("name");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getOutput();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", "hidden");
		
		String value = this.confidentiality ? "0" : "Sally Greenwood";
		assertContainsAttribute(output, "value", value);
	}

	public void testWithCustomBinder() throws Exception {
		this.tag.setPath("myFloat");

		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		errors.getPropertyAccessor().registerCustomEditor(Float.class, new SimpleFloatEditor());
		exposeBindingResult(errors);

		assertEquals(Tag.SKIP_BODY, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", "hidden");
		
		String value = this.confidentiality ? "0" : "12.34f";
		assertContainsAttribute(output, "value", value);
	}

	private void assertTagClosed(String output) {
		assertTrue(output.endsWith("/>"));
	}

	private void assertTagOpened(String output) {
		assertTrue(output.startsWith("<input "));
	}

	protected TestBean createTestBean() {
		this.bean = new TestBean();
		bean.setName("Sally Greenwood");
		bean.setMyFloat(new Float("12.34"));
		return bean;
	}
}
