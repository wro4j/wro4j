/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.support;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;


/**
 * Contains divers utility methods applied on processors.
 * 
 * @author Alex Objelean
 * @created 21 Nov 2010
 */
public class ProcessorsUtils {
  /**
   * This method is visible for testing only.
   * 
   * @param <T>
   *          processor type. Can be {@link ResourcePreProcessor}, {@link ResourcePostProcessor}.
   * @param type
   *          {@link ResourceType} to apply for searching on available processors. This value cannot be null.
   * @param availableProcessors
   *          a list where to perform the search.
   * @return a list of found processors which satisfy the search criteria. There are 3 possibilities:
   *         <ul>
   *         <li>If you search by JS type - you'll get processors which can be applied on JS resources & any (null)
   *         resources</li>
   *         <li>If you search by CSS type - you'll get processors which can be applied on CSS resources & any (null)
   *         resources</li>
   *         </ul>
   */
  public static <T> Collection<T> filterProcessorsToApply(final boolean minimize, final ResourceType type,
      final Collection<T> availableProcessors) {
    Validate.notNull(availableProcessors);
    Validate.notNull(type);
    final Collection<T> found = new ArrayList<T>();
    for (final T processor : availableProcessors) {
      if (new ProcessorDecorator(processor).isEligible(minimize, type)) {
        found.add(processor);
      }
    }
    return found;
  }
}
