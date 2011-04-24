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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import org.hdiv.application.IApplication;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.DataComposerFactory;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.session.ISession;
import org.hdiv.session.IStateCache;
import org.hdiv.util.Constants;
import org.hdiv.util.HDIVUtil;
import org.hdiv.web.servlet.tags.AbstractTagTests;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.JspAwareRequestContext;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 * @author Gorka Vicente
 * @since 2.0.6
 */
public abstract class AbstractHtmlElementTagTests extends AbstractTagTests {

	public static final String COMMAND_NAME = "testBean";

	private StringWriter writer;

	private MockPageContext pageContext;
	
	private ApplicationContext hdivContext;
	
	private ISession hdivSession;


	protected final void setUp() throws Exception {
		// set up a writer for the tag content to be written to
		this.writer = new StringWriter();

		// configure the page context
		this.pageContext = createAndPopulatePageContext();
		this.initDataComposer();
		
		onSetUp();
	}

	protected MockPageContext createAndPopulatePageContext() throws JspException {
		MockPageContext pageContext = createPageContext();
		MockHttpServletRequest request = (MockHttpServletRequest) pageContext.getRequest();
		RequestContext requestContext = new JspAwareRequestContext(pageContext);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, requestContext);
		extendRequest(request);
		extendPageContext(pageContext);
		return pageContext;
	}

	protected void initDataComposer() {

		String[] files = { 
				"/org/hdiv/config/hdiv-core-applicationContext.xml",
				"/hdiv-config.xml", 
				"/hdiv-validations.xml",
		};

		if (this.hdivContext == null) {
			this.hdivContext = new ClassPathXmlApplicationContext(files);
		}
		
		//API mock de Servlet
//		HttpServletRequest request = (MockHttpServletRequest) this.hdivContext.getBean("mockRequest");
		MockHttpServletRequest request = (MockHttpServletRequest) pageContext.getRequest();
		HttpSession httpSession = request.getSession();
		ServletContext servletContext = httpSession.getServletContext();
		HDIVUtil.setHttpServletRequest(request);

		//inicializar StateCache en session
		this.initStateCache(httpSession);
		
		//inicializar HDIVConfig en ServletContext
		HDIVConfig hdivConfig = (HDIVConfig) this.hdivContext.getBean("config");
		HDIVUtil.setHDIVConfig(hdivConfig, servletContext);
		
		//inicializar IApplication en ServletContext
		IApplication application = (IApplication) this.hdivContext.getBean("application");
		HDIVUtil.setApplication(application, servletContext);
		
		//inicializar MessageSource en ServletContext
		MessageSource messageSource = (MessageSource) this.hdivContext;
		HDIVUtil.setMessageSource(messageSource, servletContext);
		
		//inicializar el datacomposer
		DataComposerFactory dataComposerFactory = (DataComposerFactory) this.hdivContext
				.getBean("dataComposerFactory");
		IDataComposer dataComposer = dataComposerFactory.newInstance();
		dataComposer.beginRequest("/testFormTag.do");
		HDIVUtil.setDataComposer(dataComposer, request);		
	}
	
	private void initStateCache(HttpSession session) {
		
		IStateCache cache = (IStateCache) this.hdivContext.getBean("cache");
		String cacheName = (String) this.hdivContext.getBean("cacheName");		
		session.setAttribute((cacheName == null) ? Constants.CACHE_NAME : cacheName, cache);
	}
	
	protected void extendPageContext(MockPageContext pageContext) throws JspException {
	}

	protected void extendRequest(MockHttpServletRequest request) {
	}

	protected void onSetUp() {
	}

	protected StringWriter getWriter() {
		return this.writer;
	}
	
	protected String getOutput() {
		return this.writer.toString();
	}

	protected MockPageContext getPageContext() {
		return this.pageContext;
	}

	protected final RequestContext getRequestContext() {
		return (RequestContext) getPageContext().getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
	}


	protected void exposeBindingResult(Errors errors) {
		// wrap errors in a Model
		Map model = new HashMap();
		model.put(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, errors);

		// replace the request context with one containing the errors
		MockPageContext pageContext = getPageContext();
		RequestContext context = new RequestContext((HttpServletRequest) pageContext.getRequest(), model);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, context);
	}

	protected final void assertContainsAttribute(String output, String attributeName, String attributeValue) {
		String attributeString = attributeName + "=\"" + attributeValue + "\"";
		assertTrue("Expected to find attribute '" + attributeName +
				"' with value '" + attributeValue +
				"' in output + '" + output + "'",
				output.indexOf(attributeString) > -1);
	}

	protected final void assertAttributeNotPresent(String output, String attributeName) {
		assertTrue("Unexpected attribute '" + attributeName + "' in output '" + output + "'.",
				output.indexOf(attributeName + "=\"") < 0);
	}

	protected final void assertBlockTagContains(String output, String desiredContents) {
		String contents = output.substring(output.indexOf(">") + 1, output.lastIndexOf("<"));
		assertTrue("Expected to find '" + desiredContents + "' in the contents of block tag '" + output + "'",
				contents.indexOf(desiredContents) > -1);
	}

	/**
	 * @return the HDIV's context
	 */
	public ApplicationContext getHdivContext() {
		return hdivContext;
	}
}
