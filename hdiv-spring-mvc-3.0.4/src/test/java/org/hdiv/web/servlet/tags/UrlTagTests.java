/*
 * Copyright 2008 the original author or authors.
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

package org.hdiv.web.servlet.tags;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.hdiv.application.IApplication;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.DataComposerFactory;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.session.ISession;
import org.hdiv.session.IStateCache;
import org.hdiv.util.Constants;
import org.hdiv.util.HDIVUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.tags.Param;
import org.springframework.web.servlet.tags.ParamAware;

/**
 * Unit tests for UrlTag
 * 
 * @author Gorka Vicente
 */
public class UrlTagTests extends AbstractTagTests {

	private UrlTagHDIV tag;

	private MockPageContext context;
	
	private ApplicationContext hdivContext;
	
	private ISession hdivSession;
	
	private String hdivParameter;

	@Override
	protected void setUp() throws Exception {
		context = createPageContext();
		tag = new UrlTagHDIV();
		tag.setPageContext(context);
		
		this.initDataComposer();
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
		MockHttpServletRequest request = (MockHttpServletRequest) context.getRequest();
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

	public void testParamSupport() {
		assertTrue(tag instanceof ParamAware);
	}

	public void testDoStartTag() throws JspException {
		int action = tag.doStartTag();

		assertEquals(Tag.EVAL_BODY_INCLUDE, action);
	}

	public void testDoEndTag() throws JspException {
		tag.setValue("url/path");

		tag.doStartTag();
		int action = tag.doEndTag();

		assertEquals(Tag.EVAL_PAGE, action);
	}

	public void testVarDefaultScope() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");

		tag.doStartTag();
		tag.doEndTag();
		
		String expectedValue =  "/" + "url/path?" + this.hdivParameter;
		String actual = (String)context.getAttribute("var", PageContext.PAGE_SCOPE);
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testVarExplicitScope() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");
		tag.setScope("request");

		tag.doStartTag();
		tag.doEndTag();
		
		String expectedValue =  "/" + "url/path?" + this.hdivParameter;
		String actual = (String)context.getAttribute("var", PageContext.REQUEST_SCOPE);
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testSetHtmlEscapeDefault() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");

		tag.doStartTag();

		Param param = new Param();
		param.setName("n me");
		param.setValue("v&l=e");
		tag.addParam(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		tag.addParam(param);

		tag.doEndTag();

		String expectedValue =  "/" + "url/path?n%20me=v%26l%3De&name=value2&" + this.hdivParameter;
		String actual = (String)context.getAttribute("var");
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testSetHtmlEscapeFalse() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");
		tag.setHtmlEscape("false");

		tag.doStartTag();

		Param param = new Param();
		param.setName("n me");
		param.setValue("v&l=e");
		tag.addParam(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		tag.addParam(param);

		tag.doEndTag();

		String expectedValue =  "/" + "url/path?n%20me=v%26l%3De&name=value2&" + this.hdivParameter;
		String actual = (String)context.getAttribute("var");
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testSetHtmlEscapeTrue() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");
		tag.setHtmlEscape("true");

		tag.doStartTag();

		Param param = new Param();
		param.setName("n me");
		param.setValue("v&l=e");
		tag.addParam(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		tag.addParam(param);

		tag.doEndTag();

		String expectedValue =  "/" + "url/path?n%20me=v%26l%3De&amp;name=value2" + "&amp;" + this.hdivParameter;
		String actual = (String)context.getAttribute("var");
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testSetJavaScriptEscapeTrue() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");
		tag.setJavaScriptEscape("true");

		tag.doStartTag();

		Param param = new Param();
		param.setName("n me");
		param.setValue("v&l=e");
		tag.addParam(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		tag.addParam(param);

		tag.doEndTag();

		String expectedValue =  "\\/" + "url\\/path?n%20me=v%26l%3De&name=value2&" + this.hdivParameter;
		String actual = (String)context.getAttribute("var");
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testSetHtmlAndJavaScriptEscapeTrue() throws JspException {
		tag.setValue("url/path");
		tag.setVar("var");
		tag.setHtmlEscape("true");
		tag.setJavaScriptEscape("true");

		tag.doStartTag();

		Param param = new Param();
		param.setName("n me");
		param.setValue("v&l=e");
		tag.addParam(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		tag.addParam(param);

		tag.doEndTag();

		String expectedValue =  "\\/" + "url\\/path?n%20me=v%26l%3De&amp;name=value2" + "&amp;" + this.hdivParameter;
		String actual = (String)context.getAttribute("var");
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testCreateQueryStringNoParams() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("", queryString);
	}

	public void testCreateQueryStringOneParam() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("?name=value", queryString);
	}

	public void testCreateQueryStringOneParamForExsistingQueryString()
			throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, false);

		assertEquals("&name=value", queryString);
	}

	public void testCreateQueryStringOneParamEmptyValue() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("?name=", queryString);
	}

	public void testCreateQueryStringOneParamNullValue() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue(null);
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("?name", queryString);
	}

	public void testCreateQueryStringOneParamAlreadyUsed() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		params.add(param);

		usedParams.add("name");

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("", queryString);
	}

	public void testCreateQueryStringTwoParams() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		params.add(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("?name=value&name=value2", queryString);
	}

	public void testCreateQueryStringUrlEncoding() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("n me");
		param.setValue("v&l=e");
		params.add(param);

		param = new Param();
		param.setName("name");
		param.setValue("value2");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("?n%20me=v%26l%3De&name=value2", queryString);
	}

	public void testCreateQueryStringParamNullName() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName(null);
		param.setValue("value");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("", queryString);
	}

	public void testCreateQueryStringParamEmptyName() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("");
		param.setValue("value");
		params.add(param);

		String queryString = tag.createQueryString(params, usedParams, true);

		assertEquals("", queryString);
	}

	public void testReplaceUriTemplateParamsNoParams() throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		String uri = tag.replaceUriTemplateParams("url/path", params,
				usedParams);

		assertEquals("url/path", uri);
		assertEquals(0, usedParams.size());
	}

	public void testReplaceUriTemplateParamsTemplateWithoutParamMatch()
			throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		String uri = tag.replaceUriTemplateParams("url/{path}", params,
				usedParams);

		assertEquals("url/{path}", uri);
		assertEquals(0, usedParams.size());
	}

	public void testReplaceUriTemplateParamsTemplateWithParamMatch()
			throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		params.add(param);

		String uri = tag.replaceUriTemplateParams("url/{name}", params,
				usedParams);

		assertEquals("url/value", uri);
		assertEquals(1, usedParams.size());
		assertTrue(usedParams.contains("name"));
	}

	public void testReplaceUriTemplateParamsTemplateWithParamMatchNamePreEncoding()
			throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("n me");
		param.setValue("value");
		params.add(param);

		String uri = tag.replaceUriTemplateParams("url/{n me}", params,
				usedParams);

		assertEquals("url/value", uri);
		assertEquals(1, usedParams.size());
		assertTrue(usedParams.contains("n me"));
	}

	public void testReplaceUriTemplateParamsTemplateWithParamMatchValueEncoded()
			throws JspException {
		List<Param> params = new LinkedList<Param>();
		Set<String> usedParams = new HashSet<String>();

		Param param = new Param();
		param.setName("name");
		param.setValue("v lue");
		params.add(param);

		String uri = tag.replaceUriTemplateParams("url/{name}", params,
				usedParams);

		assertEquals("url/v%20lue", uri);
		assertEquals(1, usedParams.size());
		assertTrue(usedParams.contains("name"));
	}

	public void testCreateUrlRemoteServer() throws JspException {
		tag.setValue("http://www.springframework.org/");

		tag.doStartTag();

		String uri = invokeCreateUrl(tag);

		assertEquals("http://www.springframework.org/", uri);
	}

	public void testCreateUrlRelative() throws JspException {
		tag.setValue("url/path");

		tag.doStartTag();

		String expectedValue =  "/" + "url/path?" + this.hdivParameter;
		String actual = invokeCreateUrl(tag);
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testCreateUrlLocalContext() throws JspException {
		((MockHttpServletRequest) context.getRequest()).setContextPath("/app-context");

		tag.setValue("/url/path");

		tag.doStartTag();

		String uri = invokeCreateUrl(tag);

		assertEquals("/app-context/url/path", uri);
	}

	public void testCreateUrlRemoteContext() throws JspException {
		((MockHttpServletRequest) context.getRequest())
				.setContextPath("/app-context");

		tag.setValue("/url/path");
		tag.setContext("some-other-context");

		tag.doStartTag();

		String uri = invokeCreateUrl(tag);

		assertEquals("/some-other-context/url/path", uri);
	}

	public void testCreateUrlRemoteContextWithSlash() throws JspException {
		((MockHttpServletRequest) context.getRequest())
				.setContextPath("/app-context");

		tag.setValue("/url/path");
		tag.setContext("/some-other-context");

		tag.doStartTag();

		String uri = invokeCreateUrl(tag);

		assertEquals("/some-other-context/url/path", uri);
	}

	public void testCreateUrlWithParams() throws JspException {
		tag.setValue("url/path");

		tag.doStartTag();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		tag.addParam(param);

		param = new Param();
		param.setName("n me");
		param.setValue("v lue");
		tag.addParam(param);

		String uri = invokeCreateUrl(tag);
		
		String expectedValue =  "/" + "url/path?name=value&n%20me=v%20lue&" + this.hdivParameter;
		String actual = invokeCreateUrl(tag);
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testCreateUrlWithTemplateParams() throws JspException {
		tag.setValue("url/{name}");

		tag.doStartTag();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		tag.addParam(param);

		param = new Param();
		param.setName("n me");
		param.setValue("v lue");
		tag.addParam(param);
		
		String expectedValue =  "/" + "url/value?n%20me=v%20lue&" + this.hdivParameter;
		String actual = invokeCreateUrl(tag);
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testCreateUrlWithParamAndExsistingQueryString()
			throws JspException {
		tag.setValue("url/path?foo=bar");

		tag.doStartTag();

		Param param = new Param();
		param.setName("name");
		param.setValue("value");
		tag.addParam(param);

		String expectedValue =  "/" + "url/path?foo=bar&name=value&" + this.hdivParameter;
		String actual = invokeCreateUrl(tag);
		assertTrue(actual.startsWith(expectedValue));
	}

	public void testJspWriterOutput() {
		// TODO assert that the output to the JspWriter is the expected output
	}

	public void testServletRepsonseEncodeUrl() {
		// TODO assert that HttpServletResponse.encodeURL(String) is invoked for
		// non absolute urls
	}

	// support methods

	private String invokeCreateUrl(UrlTagHDIV tag) {
		Method createUrl = ReflectionUtils.findMethod(tag.getClass(), "createUrl");
		ReflectionUtils.makeAccessible(createUrl);
		return (String) ReflectionUtils.invokeMethod(createUrl, tag);
	}

}
