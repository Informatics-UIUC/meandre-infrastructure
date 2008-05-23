package org.meandre.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface Component {
	public enum FiringPolicy { all, any};
	public enum Licenses {UofINCSA, ASL_2, Other};
	String name() default "My new component";
	String baseURL() default "http://www.seasrproject.org/components/";
	String creator();
	String description();
	String tags();
	Licenses rights() default Licenses.UofINCSA;
	String format() default "java/class";
	String runnable() default "java";
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