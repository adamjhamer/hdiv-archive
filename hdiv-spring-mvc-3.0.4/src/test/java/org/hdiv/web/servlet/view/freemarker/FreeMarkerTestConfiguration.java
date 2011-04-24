package org.hdiv.web.servlet.view.freemarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Locale;

import freemarker.ext.servlet.AllHttpScopesHashModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


public class FreeMarkerTestConfiguration extends Configuration {

	public Template getTemplate(String name, final Locale locale) throws IOException {
		if (name.equals("templateName") || name.equals("prefix_test_suffix")) {
			return new Template(name, new StringReader("test")) {
				public void process(Object model, Writer writer) throws TemplateException, IOException {
					assertEquals(Locale.US, locale);
					assertTrue(model instanceof AllHttpScopesHashModel);
					AllHttpScopesHashModel fmModel = (AllHttpScopesHashModel) model;
					assertEquals("myvalue", fmModel.get("myattr").toString());
				}
			};
		}
		else {
			throw new FileNotFoundException();
		}
	}
}
