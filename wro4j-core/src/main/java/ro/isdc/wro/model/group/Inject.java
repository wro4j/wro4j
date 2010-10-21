/*
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to inject configuration specific objects into pre&post processors implementations.
 *
 * @author Alex Objelean
 * @created Created on Jan 10, 2010
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Inject {
}