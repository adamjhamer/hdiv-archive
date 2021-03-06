/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.taglibs.standard.examples.taglib;

import java.util.Locale;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

/**
 * <p>Tag handler for &lt;locales&gt;
 *
 * @author Felipe Leme <jstl@felipeal.net>
 * @version $Revision: 602107 $ $Date: 2007-12-07 15:27:35 +0100 (vie, 07 dic 2007) $
 */

public class DefaultLocaleTag extends ConditionalTagSupport {

  private static final Locale defaultLocale = Locale.getDefault();

  public boolean condition() throws JspTagException {   
    LocalesTag localesTag = (LocalesTag) findAncestorWithClass( this, LocalesTag.class );
    if ( localesTag == null ) {
      throw new JspTagException( "defaultLocale bust be inside locales");
    }
    return localesTag.getCurrent().equals( defaultLocale );
   }
}
