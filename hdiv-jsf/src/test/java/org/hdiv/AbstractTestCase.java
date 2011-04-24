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

package org.hdiv;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public abstract class AbstractTestCase extends TestCase{

	private static Log log = LogFactory.getLog(AbstractTestCase.class);
	
	protected HDIVConfig config;
	
	protected void setUp() throws Exception {
		
		XmlBeanFactory context = new XmlBeanFactory(new ClassPathResource("/org/hdiv/config/hdiv-core-applicationContext.xml"));
		context = new XmlBeanFactory(new ClassPathResource("/hdiv-validations.xml"), context);
		context = new XmlBeanFactory(new ClassPathResource("/hdiv-config.xml"), context);
		
		this.config = (HDIVConfig) context.getBean("config");
		
	}
	
	protected void tearDown() throws Exception {
		
	}
	
}
