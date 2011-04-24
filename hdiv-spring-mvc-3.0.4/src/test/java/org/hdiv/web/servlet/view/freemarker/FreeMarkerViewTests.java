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

package org.hdiv.web.servlet.view.freemarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.easymock.MockControl;
import org.junit.Test;
import org.springframework.context.ApplicationContextException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

/**
 * @author Gorka Vicente
 */
public class FreeMarkerViewTests {

	@Test
	public void testNoFreeMarkerConfig() throws Exception {
		FreeMarkerView fv = new FreeMarkerView();

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeansOfType(FreeMarkerConfig.class, true, false);
		wmc.setReturnValue(new HashMap());
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wac.getServletContext();
		wmc.setReturnValue(new MockServletContext());
		wmc.replay();

		fv.setUrl("anythingButNull");
		try {
			fv.setApplicationContext(wac);
			fv.afterPropertiesSet();
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (ApplicationContextException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("FreeMarkerConfig") != -1);
		}

		wmc.verify();
	}

	@Test
	public void testNoTemplateName() throws Exception {
		FreeMarkerView fv = new FreeMarkerView();
		try {
			fv.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("url") != -1);
		}
	}

	@Test
	public void testValidTemplateName() throws Exception {
		FreeMarkerView fv = new FreeMarkerView();

		MockControl wmc = MockControl.createNiceControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		MockServletContext sc = new MockServletContext();

		wac.getBeansOfType(FreeMarkerConfig.class, true, false);
		Map configs = new HashMap();
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setConfiguration(new FreeMarkerTestConfiguration());
		configs.put("configurer", configurer);
		wmc.setReturnValue(configs);
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wac.getServletContext();
		wmc.setReturnValue(sc, 5);
		wmc.replay();

		fv.setUrl("templateName");
		fv.setApplicationContext(wac);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addPreferredLocale(Locale.US);
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		HttpServletResponse response = new MockHttpServletResponse();

		Map model = new HashMap();
		model.put("myattr", "myvalue");
		fv.render(model, request, response);

		wmc.verify();
		assertEquals(AbstractView.DEFAULT_CONTENT_TYPE, response.getContentType());
	}

	@Test
	public void testKeepExistingContentType() throws Exception {
		FreeMarkerView fv = new FreeMarkerView();

		MockControl wmc = MockControl.createNiceControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		MockServletContext sc = new MockServletContext();

		wac.getBeansOfType(FreeMarkerConfig.class, true, false);
		Map configs = new HashMap();
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setConfiguration(new FreeMarkerTestConfiguration());
		configs.put("configurer", configurer);
		wmc.setReturnValue(configs);
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wac.getServletContext();
		wmc.setReturnValue(sc, 5);
		wmc.replay();

		fv.setUrl("templateName");
		fv.setApplicationContext(wac);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addPreferredLocale(Locale.US);
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		HttpServletResponse response = new MockHttpServletResponse();
		response.setContentType("myContentType");

		Map model = new HashMap();
		model.put("myattr", "myvalue");
		fv.render(model, request, response);

		wmc.verify();
		assertEquals("myContentType", response.getContentType());
	}

	@Test
	public void testFreeMarkerViewResolver() throws Exception {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setConfiguration(new FreeMarkerTestConfiguration());

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.getBeanFactory().registerSingleton("configurer", configurer);
		wac.refresh();

		FreeMarkerViewResolver vr = new FreeMarkerViewResolver();
		vr.setPrefix("prefix_");
		vr.setSuffix("_suffix");
		vr.setApplicationContext(wac);

		View view = vr.resolveViewName("test", Locale.CANADA);
		assertEquals("Correct view class", FreeMarkerView.class, view.getClass());
		assertEquals("Correct URL", "prefix_test_suffix", ((FreeMarkerView) view).getUrl());

		view = vr.resolveViewName("non-existing", Locale.CANADA);
		assertNull(view);

		view = vr.resolveViewName("redirect:myUrl", Locale.getDefault());
		assertEquals("Correct view class", RedirectView.class, view.getClass());
		assertEquals("Correct URL", "myUrl", ((RedirectView) view).getUrl());

		view = vr.resolveViewName("forward:myUrl", Locale.getDefault());
		assertEquals("Correct view class", InternalResourceView.class, view.getClass());
		assertEquals("Correct URL", "myUrl", ((InternalResourceView) view).getUrl());
	}

}

