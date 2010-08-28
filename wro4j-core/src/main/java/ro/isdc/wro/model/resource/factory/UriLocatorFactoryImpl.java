/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.factory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.locator.UriLocator;

/**
 * Default implementation of UriLocator. Holds a list of uri locators. The
 * uriLocator will be created based on the first uriLocator from the supplied
 * list which will accept the url.
 *
 * @author Alex Objelean
 * @created Created on Nov 4, 2008
 */
public final class UriLocatorFactoryImpl implements UriLocatorFactory {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UriLocatorFactoryImpl.class);
  /**
   * List of resource readers.
   */
  private List<UriLocator> uriLocators = new ArrayList<UriLocator>();
  private final DuplicateResourceDetector duplicateResourceDetector = new DuplicateResourceDetector();

  /**
   * {@inheritDoc}
   */
  public UriLocator getInstance(final String uri) {
    for (final UriLocator uriLocator : uriLocators) {
      if (uriLocator.accept(uri)) {
        return uriLocator;
      }
    }
    return null;
  }

  /**
   * Add a single resource to the list of supported resource locators.
   *
   * @param uriLocator
   *          {@link UriLocator} object to add.
   */
  public final void addUriLocator(final UriLocator uriLocator) {
    if (uriLocator == null) {
      throw new IllegalArgumentException("ResourceLocator cannot be null!");
    }
    processInjectAnnotation(uriLocator);
    //inject duplicateResourceDetector
    uriLocators.add(uriLocator);
  }


  /**
   * Check for each field from the passed object if @Inject annotation is present & inject the required field if
   * supported, otherwise warns about invalid usage.
   *
   * @param locator object to check for annotation presence.
   */
  //TODO move this method to WroUtils
  private void processInjectAnnotation(final Object locator) {
    try {
      final Collection<Field> fields = getAllFields(locator);
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
          if (field.getType() != DuplicateResourceDetector.class) {
            throw new WroRuntimeException("@Inject can be applied only on fields of "
              + DuplicateResourceDetector.class.getName() + " type");
          }
          LOG.debug("Injecting " + DuplicateResourceDetector.class.getSimpleName() + " in the locator: " + locator.getClass().getSimpleName());
          field.setAccessible(true);
          field.set(locator, duplicateResourceDetector);
        }
//        //proceed with injection for inner UriLocator's.
//        if (WildcardStreamLocator.class.isAssignableFrom(field.getType())) {
//          field.setAccessible(true);
//          processInjectAnnotation(field.get(locator));
//        }
      }
    } catch (final Exception e) {
      throw new WroRuntimeException("Exception while trying to process Inject annotation", e);
    }
  }

  /**
   * Return all fields for given object, also those from super classes.
   *
   * @param object
   * @return
   */
  private Collection<Field> getAllFields(final Object object) {
    final Collection<Field> fields = new ArrayList<Field>();
    fields.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
    //inspect super classes
    Class<?> superClass = object.getClass().getSuperclass();
    do {
      fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
      superClass = superClass.getSuperclass();
    } while(superClass != null);
    return fields;
  }

  /**
   * @param uriLocators
   *          the resourceLocators to set
   */
  public final void setUriLocators(final List<UriLocator> uriLocators) {
    if (uriLocators == null) {
      throw new IllegalArgumentException("uriLocators list cannot be null!");
    }
    this.uriLocators = uriLocators;
  }

  /**
   * @return the duplicateResourceDetector
   */
  public DuplicateResourceDetector getDuplicateResourceDetector() {
    return duplicateResourceDetector;
  }
}
