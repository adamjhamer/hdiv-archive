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

package org.hdiv.web.util;

import javax.servlet.jsp.tagext.Tag;

import org.springframework.util.Assert;

/**
 * Utility class for tag library.
 * 
 * @author Gorka Vicente
 * @since HDIV 2.0.6
 */
public abstract class TagUtils {

	public static final String DATA_COMPOSER = "dataComposer";
	
	/**
	 * Determine whether the supplied {@link Tag} has any ancestor tag of the
	 * supplied type.
	 * 
	 * @param tag the tag whose ancestors are to be checked
	 * @param ancestorTagClass the ancestor {@link Class} being searched for
	 * @return <code>true</code> if the supplied {@link Tag} has any ancestor
	 *         tag of the supplied type
	 * @throws IllegalArgumentException if either of the supplied arguments is
	 *             <code>null</code>; or if the supplied
	 *             <code>ancestorTagClass</code> is not type-assignable to the
	 *             {@link Tag} class
	 */
	public static Tag getAncestorOfType(Tag tag, Class ancestorTagClass) {
		
		Assert.notNull(tag, "Tag cannot be null");
		Assert.notNull(ancestorTagClass, "Ancestor tag class cannot be null");
		if (!Tag.class.isAssignableFrom(ancestorTagClass)) {
			throw new IllegalArgumentException("Class '" + ancestorTagClass.getName() + "' is not a valid Tag type");
		}
		Tag ancestor = tag.getParent();
		while (ancestor != null) {
			if (ancestorTagClass.isAssignableFrom(ancestor.getClass())) {
				return ancestor;
			}
			ancestor = ancestor.getParent();
		}
		return null;
	}
}
