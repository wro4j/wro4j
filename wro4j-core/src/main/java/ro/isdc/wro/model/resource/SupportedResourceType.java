/*
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * Specify for which type of resources the pre or post processor should apply. Absence of annotation means that the
 * processor will be applied for any type of resource.
 *
 * @author Alex Objelean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface SupportedResourceType {
  /**
   * @return supported {@link ResourceType}.
   */
  ResourceType value();
}
