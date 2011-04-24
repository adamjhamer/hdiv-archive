/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * @author Gorka Vicente
 * @since 2.0.6
 */
public class RadioButtonTagTests extends AbstractFormTagTests {

	private RadioButtonTagHDIV tag;
	
	private boolean confidentiality;

	protected void onSetUp() {
		this.tag = new RadioButtonTagHDIV() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
		
		this.confidentiality = ((Boolean) getHdivContext().getBean("confidentiality")).booleanValue();
	}

	public void testWithCheckedValue() throws Exception {
		this.tag.setPath("sex");
		this.tag.setValue("M");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "sex");
		assertContainsAttribute(output, "type", "radio");
		
		String hdivValue = this.confidentiality ? "0" : "M";
		assertContainsAttribute(output, "value", hdivValue);
		assertContainsAttribute(output, "checked", "checked");
	}

	public void testWithCheckedObjectValue() throws Exception {
		this.tag.setPath("myFloat");
		this.tag.setValue(getFloat());
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		
		String hdivValue = this.confidentiality ? "0" : getFloat().toString();
		assertContainsAttribute(output, "value", hdivValue);
		assertContainsAttribute(output, "checked", "checked");
	}

	public void testWithUncheckedObjectValue() throws Exception {
		Float value = new Float("99.45");
		this.tag.setPath("myFloat");
		this.tag.setValue(value);
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		
		String hdivValue = this.confidentiality ? "0" : value.toString();
		assertContainsAttribute(output, "value", hdivValue);		
		assertAttributeNotPresent(output, "checked");
	}

	public void testWithUncheckedValue() throws Exception {
		this.tag.setPath("sex");
		this.tag.setValue("F");
		int result = this.tag.doStartTag();
		assertEquals(Tag.SKIP_BODY, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "sex");
		assertContainsAttribute(output, "type", "radio");
		
		String hdivValue = this.confidentiality ? "0" : "F";
		assertContainsAttribute(output, "value", hdivValue);		
		assertAttributeNotPresent(output, "checked");
	}

	private void assertTagOpened(String output) {
		assertTrue(output.indexOf("<input ") > -1);
	}

	private void assertTagClosed(String output) {
		assertTrue(output.indexOf("/>") > -1);
	}

	private Float getFloat() {
		return new Float("12.99");
	}

	protected TestBean createTestBean() {
		TestBean bean = new TestBean();
		bean.setSex("M");
		bean.setMyFloat(getFloat());
		return bean;
	}


}
