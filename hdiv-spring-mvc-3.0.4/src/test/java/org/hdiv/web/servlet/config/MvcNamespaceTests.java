/*
 * Copyright 2002-2005 the original author or authors.
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
package org.hdiv.web.servlet.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Locale;

import javax.validation.Valid;

import org.hdiv.beans.TestBean;
import org.hdiv.web.validator.EditableParameterValidator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

/**
 * @author Keith Donald
 * @author Arjen Poutsma 
 */
public class MvcNamespaceTests {

	private GenericWebApplicationContext appContext;

	@Before
	public void setUp() {
		appContext = new GenericWebApplicationContext();
		appContext.setServletContext(new MockServletContext());
		LocaleContextHolder.setLocale(Locale.US);
	}

	@Test
	public void testCustomValidator() throws Exception {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
		reader.loadBeanDefinitions(new ClassPathResource("/org/hdiv/web/context/WEB-INF/editable-custom-validator.xml", getClass()));
		assertEquals(5, appContext.getBeanDefinitionCount());
		appContext.refresh();

		AnnotationMethodHandlerAdapter adapter = appContext.getBean(AnnotationMethodHandlerAdapter.class);
		assertNotNull(adapter);

		TestController handler = new TestController();

		// default web binding initializer behavior test
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("date", "2009-10-31");
		MockHttpServletResponse response = new MockHttpServletResponse();
		adapter.handle(request, response, handler);

		appContext.getBean(EditableParameterValidator.class).validate(null, null);
		assertFalse(handler.recordedValidationError);
	}
	
	@Controller
	public static class TestController {
		
		private boolean recordedValidationError;
		
		@RequestMapping
		public void testBind(@RequestParam @DateTimeFormat(iso=ISO.DATE) Date date, @Valid TestBean bean, BindingResult result) {
			if (result.getErrorCount() == 1) {
				this.recordedValidationError = true;
			} else {
				this.recordedValidationError = false;
			}
		}
	}
	
	public static class TestValidator implements Validator {

		boolean validatorInvoked;
		
		public boolean supports(Class<?> clazz) {
			return true;
		}

		public void validate(Object target, Errors errors) {
			this.validatorInvoked = true;
		}	
	}

}
