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

package org.hdiv.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIData;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.util.ConstantsJsf;
import org.hdiv.util.UtilsJsf;

/**
 * HtmlInputHidden component extension
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class HtmlInputHiddenExtension extends HtmlInputHidden {

	private static Log log = LogFactory.getLog(HtmlInputHiddenExtension.class);

	/**
	 * Default constructor
	 */
	public HtmlInputHiddenExtension() {
		log.debug("Creado nuevo HtmlInputHiddenExtension");
	}

	/**
	 * Obtains hidden's real value which has been stored in the JSF state
	 * 
	 * @return
	 */
	public Object getRealValue(String clientId) {
		Map values = (Map) this.getAttributes().get(ConstantsJsf.HDIV_ATTRIBUTE_KEY);
		return values.get(clientId);
	}

	/**
	 * Returns component's client id for the row passed as a parameter
	 * 
	 * @param rowIndex
	 *            row index in UIData
	 * @return component id
	 */
	public String getRequestId(int rowIndex) {
		List clientIds = (List) this.getAttributes().get(ConstantsJsf.HDIV_ATTRIBUTE_CLIENTIDS_KEY);
		return (String) clientIds.get(rowIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.
	 * FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {

		Map values = null;
		List clientIds = null;
		UIData tablaComp = UtilsJsf.findParentUIData(this);
		String clientId = this.getClientId(context);
		if (tablaComp != null) {
			values = (Map) this.getAttributes().get(ConstantsJsf.HDIV_ATTRIBUTE_KEY);
			clientIds = (List) this.getAttributes().get(ConstantsJsf.HDIV_ATTRIBUTE_CLIENTIDS_KEY);
		}
		if (values == null) {
			values = new HashMap();
		}
		if (clientIds == null) {
			clientIds = new ArrayList();
		}
		values.put(clientId, super.getValue());
		clientIds.add(clientId);
		this.getAttributes().put(ConstantsJsf.HDIV_ATTRIBUTE_KEY, values);
		this.getAttributes().put(ConstantsJsf.HDIV_ATTRIBUTE_CLIENTIDS_KEY, clientIds);

		if (log.isDebugEnabled()) {
			log.debug("HDIV_value" + "->" + values.get(clientId));
		}

		super.encodeBegin(context);
	}

	/**
	 * Returns list of component's client id
	 * 
	 * @return list of ids
	 */
	public List getClientIds() {
		return (List) this.getAttributes().get(ConstantsJsf.HDIV_ATTRIBUTE_CLIENTIDS_KEY);
	}

}
