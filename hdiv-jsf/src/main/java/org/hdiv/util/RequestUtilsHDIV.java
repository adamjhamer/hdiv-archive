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

package org.hdiv.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.IDataComposer;

/**
 * General purpose utility methods related to processing a servlet request.
 * 
 * @author Gorka Vicente
 */
public class RequestUtilsHDIV {

	private static Log log = LogFactory.getLog(RequestUtilsHDIV.class);

	/**
	 * Devuelve true solo si la url que se le pasa es externa, esto es, no hay
	 * que a�adirle el estado de HDIV
	 * Returns true only if it is an external url, that is, HDIV state must not be added.
	 * 
	 * @param request
	 *            request object
	 * @param url
	 *            the url
	 * @return boolean value
	 */
	public static boolean isInternalUrl(HttpServletRequest request, String url) {

		if (RequestUtilsHDIV.isPathUrl(url)) {

			if (url.startsWith(request.getContextPath() + "/") || url.equals(request.getContextPath())) {
				// url of type  /hdiv-jsf-1.0/... or /hdiv-jsf-1.0
				return true;
			} else if (url.startsWith("/")) {
				// url of type /anotherApplication/...
				return false;
			} else {
				// url of type section/action...
				return true;
			}
		} else { // URL is absolute: http://...
			String urlWithoutServer = removeServerFromUrl(url);
			if (urlWithoutServer.startsWith(request.getContextPath() + "/")
					|| urlWithoutServer.equals(request.getContextPath())) {
				// http://localhost:8080/hdiv-jsf-1.0/... or
				// http://localhost:8080/hdiv-jsf-1.0
				return true;
			}
			// http://localhost:8080/anotherApplication... or http://www.google.com
			return false;
		}
	}

	/**
	 * Returns true only if url points to a resource that does not require
	 * a HDIV state, as an image or pdf file. 
	 * Before calling this method verify that it is an internal url
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isResourceUrl(HDIVConfig hdivConfig, String url) {

		// Remove parameters
		if (url.indexOf("?") > 0) {
			url = url.substring(0, url.indexOf("?"));
		}

		// Remove anchor
		if (url.indexOf("#") > 0) {
			url = url.substring(0, url.indexOf("#"));
		}

		if (url.endsWith("/")) {
			return false;
		}

		Hashtable protectedExtensions = hdivConfig.getProtectedURLPatterns();

		Iterator it = protectedExtensions.entrySet().iterator();
		while (it.hasNext()) {
			Entry valor = (Entry) it.next();
			Pattern allowedExtension = (Pattern) valor.getValue();
			if (allowedExtension.matcher(url).matches()) {
				return false;
			}
		}

		// If url ends with .../module verify that has no dot (.) on it.
		// If contains a dot it may be a type of file extension .../mod.gif
		String suffix = "";
		if (url.indexOf("/") > 0) {
			suffix = url.substring(url.lastIndexOf("/") + 1);
		} else {
			suffix = url;
		}
		if (suffix.indexOf(".") < 0) {
			return false;
		}

		return true;

	}

	/**
	 * Checks if <code>url</code> contains any scheme.
	 * 
	 * @param url
	 *            URL
	 * @return if the url <code>url</code> contains a scheme. False otherwise.
	 */
	private static boolean isPathUrl(String url) {
		return (url.indexOf(':') == -1);// Si contiene http://.... devuelve
										// false
	}

	/**
	 * It creates a new state to store all the parameters and values of the
	 * <code>request</code> and it generates a new encoded values for the
	 * <code>request</code> parameters and adds the HDIV parameter.
	 * 
	 * @param request
	 *            HTTP request
	 * @param finalLocation
	 *            the location to redirect to
	 * @return URL with encoded parameters and HDIV parameter
	 */
	public static String composeURL(HttpServletRequest request, IDataComposer dataComposer, String url) {

		String encodedURL = url;

		// IDataComposer dataComposer = (IDataComposer)
		// request.getAttribute("dataComposer");
		// IDataComposer dataComposer = HDIVUtil.getDataComposer();
		String actionMappingName = RequestUtilsHDIV.getActionMappingName(request, url);
		dataComposer.beginRequest(actionMappingName);

		int question = url.indexOf("?");
		if (question > 0) {

			// generate a new encoded values for the url parameters
			encodedURL = RequestUtilsHDIV.composeAction(url, question, Constants.ENCODING_UTF_8, dataComposer);
		}

		return encodedURL;
	}

	/**
	 * Gets action identifier from url. Action identifier is composed by 
	 * application and action names <code>/application/action</code>
	 * 
	 * @param request
	 * @param url
	 * @return
	 */
	private static String getActionMappingName(HttpServletRequest request, String url) {

		String actionMappingName = null;

		if (!isPathUrl(url)) {
			// http://....
			// Remove the part related with the server side
			String urlSimple = removeServerFromUrl(url);
			actionMappingName = HDIVUtil.getActionMappingName(urlSimple);
		} else if (url.startsWith("/")) {
			// url= /aplicacion/action
			// Already has the context path
			actionMappingName = HDIVUtil.getActionMappingName(url);
		} else {
			String uri = request.getRequestURI();
			String contextAndFolder = uri.substring(0, uri.lastIndexOf("/"));

			// url= action
			actionMappingName = HDIVUtil.getActionMappingName(contextAndFolder + "/" + url);
		}

		return actionMappingName;

	}

	/**
	 * <p>
	 * It generates a new encoded values for the <code>url</code> parameters.
	 * </p>
	 * <p>
	 * The returned values guarantees the confidentiality in the encoded and
	 * memory strategies if confidentiality indicator defined by user is true.
	 * </p>
	 * 
	 * @param url
	 *            request url
	 * @param questionIndex
	 *            index of the first question occurrence in <code>url</code>
	 *            string
	 * @param charEncoding
	 *            character encoding
	 * @return url with encoded values
	 */
	public static String composeAction(String url, int questionIndex, String charEncoding, IDataComposer dataComposer) {

		String value = url;

		value = value.substring(questionIndex + 1);
		value = value.replaceAll("&amp;", "&");

		String token = null;
		String urlAction = HDIVUtil.getActionMappingName(url);

		StringTokenizer st = new StringTokenizer(value, "&");
		while (st.hasMoreTokens()) {

			token = st.nextToken();
			String param = token.substring(0, token.indexOf("="));
			String val = token.substring(token.indexOf("=") + 1);

			String encodedValue = dataComposer.compose(urlAction, param, val, false, true, charEncoding);
			value = value.replaceFirst(HDIVUtil.protectCharacters(token), param + "=" + encodedValue);
		}
		return url.substring(0, questionIndex + 1) + value;
	}

	/**
	 * Add Hdiv state to the url
	 * 
	 * @param hdivParameter
	 *            hdiv parameter name
	 * @param hdivRequestId
	 *            hdiv parameter value
	 * @param encodedURL
	 *            url to appent the hdiv state
	 * @param anchor
	 *            url anchor
	 * @return url with state
	 */
	public static String addHDIVState(String hdivParameter, String hdivRequestId, String encodedURL, String anchor) {

		String separator = "";

		if ((hdivRequestId.length() <= 0) || (encodedURL.startsWith("javascript:"))) {
			return encodedURL;
		}

		// we ask if the link has an anchor. If so, we must remove it to be
		// added
		// later on, once the hdivParameter has been added. this way we get a
		// link
		// like this: ../action.do?parameters&hdivParameter=2-5#anchor
		boolean isAnchor = (anchor != null) && (!anchor.equals(""));
		if (isAnchor) {
			encodedURL = encodedURL.replaceFirst("#" + anchor, "");
		}

		// we check if the url contains parameters
		separator = (encodedURL.indexOf("?") > 0) ? "&" : "?";

		hdivParameter = separator + hdivParameter + "=" + hdivRequestId;

		return encodedURL + hdivParameter + ((isAnchor) ? "#" + anchor : "");
	}

	/**
	 * Removes from url the part related with the server side
	 * 
	 * @param url
	 *            url a modificar
	 * @return url modificada
	 */
	public static String removeServerFromUrl(String url) {

		String urlSimple = url.replaceFirst("://", "");
		int posicion = urlSimple.indexOf("/");
		if (posicion > 0) {
			urlSimple = urlSimple.substring(posicion);
		} else {
			urlSimple = "";
		}
		return urlSimple;

	}

}
