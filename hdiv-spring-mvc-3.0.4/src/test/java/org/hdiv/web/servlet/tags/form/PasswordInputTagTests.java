/*
 * Copyright 2002-2007 the original author or authors.
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

import java.io.StringWriter;

import javax.servlet.jsp.tagext.Tag;

import org.hdiv.beans.TestBean;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * @author Gorka Vicente
 * @since 2.0.6
 */
public final class PasswordInputTagTests extends AbstractFormTagTests {

	private PasswordInputTagHDIV tag;

	private TestBean rob;
	

	protected void onSetUp() {
		this.tag = createTag(getWriter());
		this.tag.setPageContext(getPageContext());
	}

	protected TestBean createTestBean() {
		// set up test data
		this.rob = new TestBean();
		this.rob.setName("Rob");
		this.rob.setMyFloat(new Float(12.34));

		TestBean sally = new TestBean();
		sally.setName("Sally");
		this.rob.setSpouse(sally);

		return this.rob;
	}

	/*
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2866
	 */
	public void testPasswordValueIsNotRenderedByDefault() throws Exception {
		this.tag.setPath("name");

		assertEquals(Tag.SKIP_BODY, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());
		assertPasswordValueAttribute(output, "");
	}

	/*
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2866
	 */
	public void testPasswordValueIsRenderedIfShowPasswordAttributeIsSetToTrue() throws Exception {
		this.tag.setPath("name");
		this.tag.setShowPassword(true);

		assertEquals(Tag.SKIP_BODY, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());
		assertPasswordValueAttribute(output, "Rob");
	}

	/*
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2866
	 */
	public void testPasswordValueIsNotRenderedIfShowPasswordAttributeIsSetToFalse() throws Exception {
		this.tag.setPath("name");
		this.tag.setShowPassword(false);

		assertEquals(Tag.SKIP_BODY, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());
		assertPasswordValueAttribute(output, "");
	}

	protected void assertPasswordValueAttribute(String output, String expectedValue) {
		if (this.tag.isShowPassword()) {
			assertValueAttribute(output, expectedValue);
		} else {
			assertValueAttribute(output, "");
		}
	}

	protected final void assertTagClosed(String output) {
		assertTrue("Tag not closed properly", output.endsWith("/>"));
	}

	protected final void assertTagOpened(String output) {
		assertTrue("Tag not opened properly", output.startsWith("<input "));
	}
	
	protected void assertValueAttribute(String output, String expectedValue) {
		assertContainsAttribute(output, "value", expectedValue);
	}		
	
	protected String getType() {
		return "password";
	}

	protected PasswordInputTagHDIV createTag(final StringWriter writer) {
		return new PasswordInputTagHDIV() {

			protected TagWriter createTagWriter() {
				return new TagWriter(writer);
			}
		};
	}

}
