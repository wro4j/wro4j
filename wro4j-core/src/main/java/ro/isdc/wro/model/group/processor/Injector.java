/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.DuplicateResourceDetector;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

/**
 * Injector scans fields of an object instance and checks if a value can be provided to a field; Injector will ignore
 * all non-null fields.
 *
 * @author Alex Objelean
 */
public abstract class Injector {
  private static final Logger LOG = LoggerFactory.getLogger(Injector.class);
  private final DuplicateResourceDetector duplicateResourceDetector = new DuplicateResourceDetector();
  private UriLocatorFactory uriLocatorFactory;
  private PreProcessorExecutor preProcessorExecutor;
  public Injector(final boolean ignoreMissingResources) {
    uriLocatorFactory = new UriLocatorFactory(this);
    //TODO refactor
    preProcessorExecutor = new PreProcessorExecutor(this) {
      @Override
      protected boolean ignoreMissingResources() {
        return ignoreMissingResources;
      };
      @Override
      protected Collection<ResourcePreProcessor> getPreProcessorsByType(final ResourceType resourceType) {
        return Injector.this.getPreProcessorsByType(resourceType);
      }
    };
  }

  protected abstract Collection<ResourcePreProcessor> getPreProcessorsByType(final ResourceType type);

  public void inject(final Object object) {
    processInjectAnnotation(object);
  }


  /**
   * Check for each field from the passed object if @Inject annotation is present & inject the required field if
   * supported, otherwise warns about invalid usage.
   *
   * @param processor object to check for annotation presence.
   */
  private void processInjectAnnotation(final Object processor) {
    try {
      final Collection<Field> fields = getAllFields(processor);
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
          if (!acceptAnnotatedField(processor, field)) {
            throw new WroRuntimeException("@Inject can be applied only on fields of "
              + UriLocatorFactory.class.getName() + " type");
          }
        }
      }
    } catch (final Exception e) {
      throw new WroRuntimeException("Exception while trying to process Inject annotation", e);
    }
  }

  /**
   * Return all fields for given object, also those from the super classes.
   */
  private Collection<Field> getAllFields(final Object object) {
    final Collection<Field> fields = new ArrayList<Field>();
    fields.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
    // inspect super classes
    Class<?> superClass = object.getClass().getSuperclass();
    do {
      fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
      superClass = superClass.getSuperclass();
    } while (superClass != null);
    return fields;
  }

  /**
   * Analyze the field containing {@link Inject} annotation and set its value to appropriate value. Override this method
   * if you want to inject something else but uriLocatorFactory.
   *
   * @param object an object containing @Inject annotation.
   * @param field {@link Field} object containing {@link Inject} annotation.
   * @return true if field was injected with some not null value.
   * @throws IllegalAccessException
   */
  private boolean acceptAnnotatedField(final Object object, final Field field)
    throws IllegalAccessException {
    field.setAccessible(true);
    if (field.getType().equals(UriLocatorFactory.class)) {
      // accept even private modifiers
      field.set(object, uriLocatorFactory);
      LOG.debug("Successfully injected field: " + field.getName());
      return true;
    }
    if (field.getType().equals(PreProcessorExecutor.class)) {
      field.set(object, preProcessorExecutor);
      LOG.debug("Successfully injected field: " + field.getName());
      return true;
    }
    if (field.getType().equals(DuplicateResourceDetector.class)) {
      field.set(object, duplicateResourceDetector);
      LOG.debug("Successfully injected duplicateResourceDetector: " + field.getName());
      return true;
    }
    return false;
  }
}
