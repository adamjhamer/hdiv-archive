/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.hdiv;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.hdiv.web.servlet.tags.form.CheckboxTagTests;
import org.hdiv.web.servlet.tags.form.FormTagTests;
import org.hdiv.web.servlet.tags.form.HiddenInputTagTests;
import org.hdiv.web.servlet.tags.form.InputTagTests;
import org.hdiv.web.servlet.tags.form.OptionTagTests;
import org.hdiv.web.servlet.tags.form.OptionsTagTests;
import org.hdiv.web.servlet.tags.form.PasswordInputTagTests;
import org.hdiv.web.servlet.tags.form.RadioButtonTagTests;
import org.hdiv.web.servlet.tags.form.SelectTagTests;
import org.hdiv.web.servlet.tags.form.TextareaTagTests;

public class AllTagsTests {

	public static Test suite() {
		
		TestSuite suite = new TestSuite("Test for org.hdiv");
		
		//$JUnit-BEGIN$
		suite.addTestSuite(CheckboxTagTests.class);
		suite.addTestSuite(FormTagTests.class);
		suite.addTestSuite(HiddenInputTagTests.class);
		suite.addTestSuite(InputTagTests.class);
		suite.addTestSuite(OptionsTagTests.class);
		suite.addTestSuite(OptionTagTests.class);
		suite.addTestSuite(PasswordInputTagTests.class);
		suite.addTestSuite(RadioButtonTagTests.class);
		suite.addTestSuite(SelectTagTests.class);
		suite.addTestSuite(TextareaTagTests.class);
		
		return suite;
	}

}
