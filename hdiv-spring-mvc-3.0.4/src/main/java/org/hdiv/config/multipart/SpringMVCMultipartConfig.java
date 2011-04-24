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

package org.hdiv.config.multipart;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.filter.RequestWrapper;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.util.WebUtils;

/**
 * Class containing multipart request configuration.
 * 
 * <p>
 * <b>NOTE:</b> This multipart resolver requires Commons FileUpload 1.1 or
 * higher.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.1.0
 */
public class SpringMVCMultipartConfig implements IMultipartConfig {

	private static Log log = LogFactory.getLog(SpringMVCMultipartConfig.class);

	private CommonsMultipartResolver multipartResolver;

	/**
	 * Parses the input stream and partitions the parsed items into a set of
	 * form fields and a set of file items.
	 * 
	 * @param request
	 *            The multipart request wrapper.
	 * @param servletContext
	 *            Our ServletContext object
	 * @throws FileUploadException
	 *             if an unrecoverable error occurs.
	 * @throws DiskFileUpload.SizeLimitExceededException
	 *             if size limit exceeded
	 */
	public void handleMultipartRequest(RequestWrapper request,
			ServletContext servletContext) throws FileUploadException,
			FileUploadBase.SizeLimitExceededException,
			MaxUploadSizeExceededException {

		String encoding = determineEncoding(request);
		FileUpload fileUpload = prepareFileUpload(encoding);
		try {
			List fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
			parseFileItems(request, fileItems);

		} catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(),
					ex);
		} catch (FileUploadException ex) {
			throw ex;
		}
	}

	/**
	 * Parse the given List of Commons FileItems into a Spring
	 * MultipartParsingResult, containing Spring MultipartFile instances and a
	 * Map of multipart parameter.
	 * 
	 * @param request
	 *            the request to parse
	 * @param fileItems
	 *            the Commons FileIterms to parse
	 * @see CommonsMultipartFile#CommonsMultipartFile(org.apache.commons.fileupload.FileItem)
	 */
	protected void parseFileItems(RequestWrapper request, List fileItems) {

		// Extract multipart files and multipart parameters.
		for (Iterator it = fileItems.iterator(); it.hasNext();) {

			FileItem fileItem = (FileItem) it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found item " + fileItem.getFieldName());
			}
			if (fileItem.isFormField()) {

				if (log.isDebugEnabled()) {
					log.debug("Item is a normal form field");
				}
				this.addTextParameter(request, fileItem);
			} else {

				if (log.isDebugEnabled()) {
					log.debug("Item is a file upload");
				}
				this.addFileParameter(request, fileItem);
			}
		}
	}

	/**
	 * Adds a regular text parameter to the set of text parameters for this
	 * request. Handles the case of multiple values for the same parameter by
	 * using an array for the parameter value.
	 * 
	 * @param request
	 *            The request in which the parameter was specified
	 * @param fileItem
	 *            The file item for the parameter to add
	 * @param encoding
	 *            the character encoding to use
	 */
	public void addTextParameter(RequestWrapper request, FileItem fileItem) {

		String encoding = determineEncoding(request);
		String name = fileItem.getFieldName();
		String value = null;

		String partEncoding = determineEncoding(fileItem.getContentType(),
				encoding);
		if (partEncoding != null) {
			try {
				value = fileItem.getString(partEncoding);
			} catch (UnsupportedEncodingException ex) {
				if (log.isWarnEnabled()) {
					log.warn("Could not decode multipart item '"
							+ fileItem.getFieldName() + "' with encoding '"
							+ partEncoding + "': using platform default");
				}
				value = fileItem.getString();
			}
		} else {
			value = fileItem.getString();
		}

		String[] oldArray = (String[]) request.getParameterValues(name);
		String[] newArray;

		if (oldArray != null) {
			newArray = new String[oldArray.length + 1];
			System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
			newArray[oldArray.length] = value;
		} else {
			newArray = new String[] { value };
		}

		request.addParameter(name, newArray);
	}

	/**
	 * Adds a file parameter to the set of file parameters for this request and
	 * also to the list of all parameters.
	 * 
	 * @param request
	 *            The request in which the parameter was specified.
	 * @param item
	 *            The file item for the parameter to add.
	 */
	public void addFileParameter(RequestWrapper request, FileItem item) {

		CommonsMultipartFile file = new CommonsMultipartFile(item);

		if (request.getFileElements().put(file.getName(), file) != null) {
			throw new MultipartException("Multiple files for field name ["
					+ file.getName()
					+ "] found - not supported by MultipartResolver");
		}

		if (log.isDebugEnabled()) {
			log.debug("Found multipart file [" + file.getName() + "] of size "
					+ file.getSize() + " bytes with original filename ["
					+ file.getOriginalFilename() + "], stored "
					+ file.getStorageDescription());
		}
	}

	public String getRepositoryPath(ServletContext servletContext) {
		return null;
	}

	/**
	 * @param multipartResolver
	 *            the multipart resolver to set
	 */
	public void setMultipartResolver(CommonsMultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

	protected String getDefaultEncoding() {

		String encoding = this.multipartResolver.getFileUpload()
				.getHeaderEncoding();
		if (encoding == null) {
			encoding = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		return encoding;
	}

	private String determineEncoding(String contentTypeHeader,
			String defaultEncoding) {

		if (!StringUtils.hasText(contentTypeHeader)) {
			return defaultEncoding;
		}
		MediaType contentType = MediaType.parseMediaType(contentTypeHeader);
		Charset charset = contentType.getCharSet();
		return charset != null ? charset.name() : defaultEncoding;
	}

	/**
	 * Determine the encoding for the given request. Can be overridden in
	 * subclasses.
	 * <p>
	 * The default implementation checks the request encoding, falling back to
	 * the default encoding specified for this resolver.
	 * 
	 * @param request
	 *            current HTTP request
	 * @return the encoding for the request (never <code>null</code>)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see #setDefaultEncoding
	 */
	protected String determineEncoding(HttpServletRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = getDefaultEncoding();
		}
		return encoding;
	}

	/**
	 * Determine an appropriate FileUpload instance for the given encoding.
	 * <p>
	 * Default implementation returns the shared FileUpload instance if the
	 * encoding matches, else creates a new FileUpload instance with the same
	 * configuration other than the desired encoding.
	 * 
	 * @param encoding
	 *            the character encoding to use
	 * @return an appropriate FileUpload instance.
	 */
	protected FileUpload prepareFileUpload(String encoding) {

		FileUpload fileUpload = this.multipartResolver.getFileUpload();
		FileUpload actualFileUpload = fileUpload;

		// Use new temporary FileUpload instance if the request specifies
		// its own encoding that does not match the default encoding.
		if (encoding != null
				&& !encoding.equals(fileUpload.getHeaderEncoding())) {
			actualFileUpload = newFileUpload(this.multipartResolver
					.getFileItemFactory());
			actualFileUpload.setSizeMax(fileUpload.getSizeMax());
			actualFileUpload.setHeaderEncoding(encoding);
		}

		return actualFileUpload;
	}

	/**
	 * Factory method for a Commons DiskFileItemFactory instance.
	 * <p>
	 * Default implementation returns a standard DiskFileItemFactory. Can be
	 * overridden to use a custom subclass, e.g. for testing purposes.
	 * 
	 * @return the new DiskFileItemFactory instance
	 */
	protected DiskFileItemFactory newFileItemFactory() {
		return new DiskFileItemFactory();
	}

	/**
	 * Initialize the underlying
	 * <code>org.apache.commons.fileupload.servlet.ServletFileUpload</code>
	 * instance. Can be overridden to use a custom subclass, e.g. for testing
	 * purposes.
	 * 
	 * @param fileItemFactory
	 *            the Commons FileItemFactory to use
	 * @return the new ServletFileUpload instance
	 */
	protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
		return new ServletFileUpload(fileItemFactory);
	}
}
