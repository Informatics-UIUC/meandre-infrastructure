/*
 * @(#) ComponentNature.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Added to support different types of components
 * 
 * @author Amit Kumar
 * Created on Jun 10, 2008 11:40:12 AM
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface ComponentNatures {
	ComponentNature[] natures();
}
