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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.core.Conventions;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.AssertThrows;
import org.springframework.web.servlet.tags.form.AbstractFormTag;
import org.springframework.web.servlet.tags.form.TagWriter;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Gorka Vicente
 * @since 2.0.6
 */
public class FormTagTests extends AbstractHtmlElementTagTests {
	
	/**
	 * The name of the {@link javax.servlet.jsp.PageContext} attribute under which the
	 * form object name is exposed.
	 */
	static final String MODEL_ATTRIBUTE_VARIABLE_NAME =
			Conventions.getQualifiedAttributeName(AbstractFormTag.class, "modelAttribute");	
	
	private static final String REQUEST_URI = "/my/form";

	private FormTagHDIV tag;
	
	private MockHttpServletRequest request;
	
	private boolean confidentiality;
	
	private String queryString;


	protected void onSetUp() {
		
		String hdivParameter = (String) super.getHdivContext().getBean("hdivParameter");
		getPageContext().getSession().setAttribute("HDIVParameter", hdivParameter);
		
		this.tag = new FormTagHDIV() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
		
		this.confidentiality = ((Boolean) getHdivContext().getBean("confidentiality")).booleanValue();
		
		String fooValue = this.confidentiality ? "0" : "bar"; 
		queryString = "foo=" + fooValue;
	}

	protected void extendRequest(MockHttpServletRequest request) {
		request.setRequestURI(REQUEST_URI);			
		request.setQueryString("foo=bar");
		this.request = request;
	}

	public void testWriteForm() throws Exception {
		String action = "/form.html";
		String commandName = "myCommand";
		String name = "formName";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";
		String cssClass = "myClass";
		String cssStyle = "myStyle";

		this.tag.setName(name);
		this.tag.setCssClass(cssClass);
		this.tag.setCssStyle(cssStyle);
		this.tag.setAction(action);
		this.tag.setCommandName(commandName);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);
		this.tag.setOnsubmit(onsubmit);
		this.tag.setOnreset(onreset);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getRequest().getAttribute(MODEL_ATTRIBUTE_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getRequest().getAttribute(MODEL_ATTRIBUTE_VARIABLE_NAME));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "class", cssClass);
		assertContainsAttribute(output, "style", cssStyle);
		assertContainsAttribute(output, "action", action);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
		assertContainsAttribute(output, "id", commandName);
		assertContainsAttribute(output, "name", name);
	}

	public void testWithActionFromRequest() throws Exception {
		
		String fooValue = this.confidentiality ? "0" : "bar"; 
		queryString = "foo=" + fooValue;
		request.setQueryString(queryString);
		request.addParameter("foo", "bar");
		
		String commandName = "myCommand";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";

		this.tag.setCommandName(commandName);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);
		this.tag.setOnsubmit(onsubmit);
		this.tag.setOnreset(onreset);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(MODEL_ATTRIBUTE_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(MODEL_ATTRIBUTE_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);
		
		assertContainsAttribute(output, "action", REQUEST_URI + "?" + queryString);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
	}

	public void testWithNullResolvedCommand() throws Exception {
		new AssertThrows(IllegalArgumentException.class,
				"Must not be able to have a command name that resolves to null") {
			public void test() throws Exception {
				tag.setCommandName("${null}");
				tag.doStartTag();
			}
		}.runTest();
	}

	/*
	 * See http://opensource.atlassian.com/projects/spring/browse/SPR-2645
	 */
	public void testXSSScriptingExploitWhenActionIsResolvedFromQueryString() throws Exception {
				
		String xssQueryString = queryString + "&stuff=\"><script>alert('XSS!')</script>";
		request.setQueryString(xssQueryString);
		request.addParameter("foo", "bar");
		request.addParameter("stuff", "&quot;&gt;&lt;script&gt;alert('XSS!')&lt;/script&gt;");		
		tag.doStartTag();
			
		String foo = this.confidentiality ? "0" : "bar";
		String stuff = this.confidentiality ? "0" : HtmlUtils.htmlEscape("&quot;&gt;&lt;script&gt;alert('XSS!')&lt;/script&gt;");		
				
		assertEquals("<form id=\"command\" action=\"/my/form?foo=" + foo + "&amp;stuff=" + stuff + "\" method=\"post\">",
				getWriter().toString());
	}


	private static void assertFormTagOpened(String output) {
		assertTrue(output.startsWith("<form "));
	}

	private static void assertFormTagClosed(String output) {
		assertTrue(output.endsWith("</form>"));
	}

}
