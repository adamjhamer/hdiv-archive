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

package org.hdiv.web.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.multipart.IMultipartConfig;
import org.hdiv.filter.RequestWrapper;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

/**
 * Servlet-based MultipartResolver implementation for
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>
 * 1.2 or higher and HDIV 2.06 or higher.
 *
 * <p>Provides maxUploadSize, maxInMemorySize, and defaultEncoding settings as
 * bean properties (inherited from CommonsFileUploadSupport). See respective
 * ServletFileUpload / DiskFileItemFactory properties (sizeMax, sizeThreshold,
 * headerEncoding) for details in terms of defaults and accepted values.
 *
 * <p>Saves temporary files to the servlet container's temporary directory.
 * Needs to be initialized <i>either</i> by an application context <i>or</i>
 * via the constructor that takes a ServletContext (for standalone usage).
 *
 * <p><b>NOTE:</b> As of Spring 2.0, this multipart resolver requires
 * Commons FileUpload 1.1 or higher. The implementation does not use
 * any deprecated FileUpload 1.0 API anymore, to be compatible with future
 * Commons FileUpload releases.
 *
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 * @see #CommonsMultipartResolver(ServletContext)
 * @see CommonsMultipartFile
 * @see org.springframework.web.portlet.multipart.PortletMultipartResolver
 * @see org.apache.commons.fileupload.servlet.ServletFileUpload
 * @see org.apache.commons.fileupload.disk.DiskFileItemFactory
 */
public class HDIVMultipartResolver extends CommonsMultipartResolver {
	
	private static Log log = LogFactory.getLog(HDIVMultipartResolver.class);
	
	private boolean resolveLazily = false;
	
	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		
		Assert.notNull(request, "Request must not be null");
		if (request instanceof RequestWrapper) {
			
			final RequestWrapper requestWrapper = (RequestWrapper) request;
			
			Exception multipartException = (Exception) request.getAttribute(IMultipartConfig.FILEUPLOAD_EXCEPTION);
			if (multipartException != null) {
				
				if (multipartException instanceof MaxUploadSizeExceededException) {
					throw (MaxUploadSizeExceededException) multipartException;
				} else {
					throw new MultipartException("Could not parse multipart servlet request", multipartException);					
				} 			
			}
			
			if (this.resolveLazily) {
				
				return new DefaultMultipartHttpServletRequest(requestWrapper) {
					@Override
					protected void initializeMultipart() {
						setMultipartFiles(getMultipartFileElements(requestWrapper.getFileElements()));
						setMultipartParameters(getMultipartTextElements(requestWrapper.getTextElements()));
					}
				};
			} else {
				return new DefaultMultipartHttpServletRequest(request, getMultipartFileElements(requestWrapper.getFileElements()), 
						getMultipartTextElements(requestWrapper.getTextElements()));
			}
		} else {
			//The normal behaviour
			return super.resolveMultipart(request);
		}
	}
	
	/**
	 * Return the multipart files as Map of field name to MultipartFile instance.
	 */
	public MultiValueMap<String, MultipartFile> getMultipartFileElements(Hashtable fileElements) {
		
		MultiValueMap<String, MultipartFile> multipartFiles = new LinkedMultiValueMap<String, MultipartFile>();
		for(Object key: fileElements.keySet()){
			multipartFiles.add((String)key, (MultipartFile)fileElements.get(key));
		}
		
		return multipartFiles;
	}

	/**
	 * Return the multipart parameters as Map of field name to form field String value.
	 */
	public Map<String, String[]> getMultipartTextElements(Hashtable textElements) {
		
		Map<String, String[]> multipartParameters = new HashMap<String, String[]>();
		multipartParameters.putAll(textElements);
		return multipartParameters;
	}
	
	/**
	 * Return the multipart files as Map of field name to MultipartFile instance.
	 */
	public Collection<MultipartFile> getMultipartFileValues(Hashtable fileElements) {
		
		Collection<MultipartFile> multipartFileValues = new ArrayList<MultipartFile>();
		multipartFileValues.addAll(fileElements.values());
		
		return multipartFileValues;
	}
	
	@Override
	public void cleanupMultipart(MultipartHttpServletRequest request) {
		
		if (request != null) {
			
			DefaultMultipartHttpServletRequest defaultRequest = (DefaultMultipartHttpServletRequest) request;
			RequestWrapper requestWrapper = (RequestWrapper)defaultRequest.getRequest();
			
			//Call to the original method
			this.cleanupFileItems(getMultipartFileElements(requestWrapper.getFileElements()));
		}
	}

	/**
	 * Set whether to resolve the multipart request lazily at the time of
	 * file or parameter access.
	 * <p>Default is "false", resolving the multipart elements immediately, throwing
	 * corresponding exceptions at the time of the {@link #resolveMultipart} call.
	 * Switch this to "true" for lazy multipart parsing, throwing parse exceptions
	 * once the application attempts to obtain multipart files or parameters.
	 */
	public void setResolveLazily(boolean resolveLazily) {
		super.setResolveLazily(resolveLazily);
		this.resolveLazily = resolveLazily;
	}	
}
