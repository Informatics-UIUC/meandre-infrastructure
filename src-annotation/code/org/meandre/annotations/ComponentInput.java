package org.meandre.annotations;


import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Amit Kumar
 * Created on Nov 23, 2007 6:42:55 PM
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ComponentInput {
	String name();
	String description();

}
