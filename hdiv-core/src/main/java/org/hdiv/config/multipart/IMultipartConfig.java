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

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.hdiv.filter.RequestWrapper;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Class containing multipart request configuration and methods initialized from
 * Spring Factory.
 * 
 * @author Gorka Vicente
 */
public interface IMultipartConfig {

	/**
	 * The default value for the maximum allowable size, in bytes, of an uploaded
	 * file. The value is equivalent to 2MB.
	 */
	public static final long DEFAULT_SIZE_MAX = 2 * 1024 * 1024;

	/**
	 * The default value for the threshold which determines whether an uploaded file
	 * will be written to disk or cached in memory. The value is equivalent to 250KB.
	 */
	public static final int DEFAULT_SIZE_THRESHOLD = 256 * 1024;

	/**
	 * This is the ServletRequest attribute that should be set when a multipart
	 * request is being read and the maximum length is exceeded. The value is a
	 * Boolean. If the maximum length isn't exceeded, this attribute shouldn't be put
	 * in the ServletRequest. It's the job of the implementation to put this
	 * attribute in the request if the maximum length is exceeded; in the
	 * handleRequest(HttpServletRequest) method.
	 */
	public static final String ATTRIBUTE_MAX_LENGTH_EXCEEDED = "org.apache.struts.upload.MaxLengthExceeded";
		
	/**
	 * This is the ServletRequest attribute that should be set when a multipart
	 * request is being read and failed. It's the job of the implementation to
	 * put this attribute in the request if multipart process failed; in the
	 * handleRequest(HttpServletRequest) method.
	 * @since HDIV 2.0.1
	 */
	public static final String FILEUPLOAD_EXCEPTION = "org.hdiv.exception.HDIVMultipartException";


	/**
	 * Parses the input stream and partitions the parsed items into a set of form
	 * fields and a set of file items.
	 * 
	 * @param request The multipart request wrapper.
	 * @param servletContext Our ServletContext object
	 * @throws FileUploadException if an unrecoverable error occurs.
	 * @throws DiskFileUpload.SizeLimitExceededException if size limit exceeded
	 */
	public void handleMultipartRequest(RequestWrapper request, ServletContext servletContext)
			throws FileUploadException, DiskFileUpload.SizeLimitExceededException, MaxUploadSizeExceededException;

	/**
	 * Returns the path to the temporary directory to be used for uploaded files
	 * which are written to disk. The directory used is determined from the first of
	 * the following to be non-empty.
	 * <ol>
	 * <li>A temp dir explicitly defined using the <code>saveDir</code> attribute
	 * of the &lt;multipartConfig&gt; element in the Spring config file.</li>
	 * <li>The temp dir specified by the <code>javax.servlet.context.tempdir</code>
	 * attribute.</li>
	 * </ol>
	 * 
	 * @param servletContext servlet context
	 * @return The path to the directory to be used to store uploaded files.
	 */
	public String getRepositoryPath(ServletContext servletContext);

	/**
	 * Adds a file parameter to the set of file parameters for this request and also
	 * to the list of all parameters.
	 * 
	 * @param request The request in which the parameter was specified.
	 * @param item The file item for the parameter to add.
	 */
	public void addFileParameter(RequestWrapper request, FileItem item);

	/**
	 * Adds a regular text parameter to the set of text parameters for this request.
	 * Handles the case of multiple values for the same parameter by using an array
	 * for the parameter value.
	 * 
	 * @param request The request in which the parameter was specified.
	 * @param item The file item for the parameter to add.
	 */
	public void addTextParameter(RequestWrapper request, FileItem item);

}
