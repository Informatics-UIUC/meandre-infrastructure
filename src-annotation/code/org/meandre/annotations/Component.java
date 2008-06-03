package org.meandre.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**This interface annotation is used by eclipse plugin to create and upload
 * component.
 * 
 * @author Amit Kumar
 * Created on May 23, 2008 6:59:04 PM
 *  //modified May 31st 2008 made name attribute essential.
 *  // the runnable and format properties are enums.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface Component {
	public enum FiringPolicy { all, any};
	public enum Licenses {UofINCSA, ASL_2, Other};
	public enum Runnable {java,python,lisp};
	//public enum Format {javaFormat,pythonFormat,lispFormat};
	//final static String javaFormat ="java/class";
	//final static String pythonFormat ="jython";
	//final static String lispFormat ="clojure";
	
	
	String name();
	String baseURL() default "http://www.seasrproject.org/components/";
	String creator();
	String description();
	String tags();
	Licenses rights() default Licenses.UofINCSA;
	String format() default "java/class";
	Runnable runnable() default Runnable.java;
	FiringPolicy firingPolicy() default FiringPolicy.all;
	String rightsOther() default "";
	/*only jar dependencies*/
	String[] dependency() default "";
	/*property files or other dependencies that are not jar libraries*/
	String[] resources() default "";
	//  public enum type {webUI,nonWebUI};
	// "http://www.meandre.org/ontology/component/type/nonwebui"
	// "http://www.meandre.org/ontology/component/type/webui"
	
}