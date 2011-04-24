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

package org.hdiv.web.servlet.tags.form;

import javax.servlet.jsp.JspException;

import org.hdiv.dataComposer.IDataComposer;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.tags.form.OptionsTag;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TagWriter;
import org.springframework.web.util.TagUtils;

/**
 * Convenient tag that allows one to supply a collection of objects
 * that are to be rendered as '<code>option</code>' tags within a
 * '<code>select</code>' tag.
 * 
 * <p><i>Must</i> be used within a {@link SelectTag 'select' tag}.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see org.springframework.web.servlet.tags.form.OptionsTag
 */
public class OptionsTagHDIV extends OptionsTag {
	
	@Override
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		
		// make sure we are under a '<code>select</code>' tag before proceeding.
		assertUnderSelectTag();
		 
		IDataComposer dataComposer = (IDataComposer) this.pageContext.getRequest().getAttribute(org.hdiv.web.util.TagUtils.DATA_COMPOSER);
		SelectTagHDIV selectTag = (SelectTagHDIV) org.hdiv.web.util.TagUtils.getAncestorOfType(this, SelectTagHDIV.class);
		
		Object items = getItems();
		Object itemsObject = null;
		if (items != null) {
			itemsObject = (items instanceof String ? evaluate("items", (String) items) : items);
		} else {
			Class<?> selectTagBoundType = ((SelectTagHDIV) findAncestorWithClass(this, SelectTagHDIV.class))
				.getBindStatus().getValueType();
			if (selectTagBoundType != null && selectTagBoundType.isEnum()) {
				itemsObject = selectTagBoundType.getEnumConstants();
			}
		}

		if (itemsObject != null) {
			String itemValue = getItemValue();
			String itemLabel = getItemLabel();

			String valueProperty = (itemValue != null ? ObjectUtils.getDisplayString(evaluate("itemValue", itemValue)) : null);
			String labelProperty = (itemLabel != null ? ObjectUtils.getDisplayString(evaluate("itemLabel", itemLabel)) : null);

			OptionsWriter optionWriter = new OptionsWriter(dataComposer, selectTag.getName(), itemsObject, valueProperty, labelProperty);
			optionWriter.writeOptions(tagWriter);
		}

		return SKIP_BODY;
	}
	
	
	private void assertUnderSelectTag() {
		TagUtils.assertHasAncestorOfType(this, SelectTagHDIV.class, "options", "select");
	}
	
	private class OptionsWriter extends OptionWriterHDIV {

		public OptionsWriter(IDataComposer dataComposer, String selectName, Object optionSource, String valueProperty, String labelProperty) {
			super(dataComposer, selectName, optionSource, getBindStatus(), valueProperty, labelProperty, isHtmlEscape());
		}

		@Override
		protected boolean isOptionDisabled() throws JspException {
			return isDisabled();
		}

		@Override
		protected void writeCommonAttributes(TagWriter tagWriter) throws JspException {
			writeOptionalAttribute(tagWriter, "id", resolveId());
			writeOptionalAttributes(tagWriter);
		}
	}	

}
