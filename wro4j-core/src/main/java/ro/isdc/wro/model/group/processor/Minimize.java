/*
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to identify (pre and post) processors which performs minimization. This is useful when the minimization isn't
 * desired (for debug purposes). This annotations is inherited, because classes which extends a super class having this
 * annotation, will also inherit the minimize aware characteristic.
 *
 * @author Alex Objelean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Minimize {
}
