/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.InjectorResourceLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.util.NamingStrategy;


/**
 * Injector scans some object fields and checks if a value can be provided to a field; Injector will ignore
 * all non-null fields.
 *
 * @author Alex Objelean
 * @created 20 Nov 2010
 */
public final class Injector {
  private static final Logger LOG = LoggerFactory.getLogger(Injector.class);
  private final WroManager wroManager;
  private final ResourceLocatorFactory resourceLocatorFactory;
  private PreProcessorExecutor preProcessorExecutor;
  private final ProcessorsFactory processorsFactory;
  private final GroupsProcessor groupsProcessor;
  /**
   * Creates the Injector and initialize the provided manager.
   */
  public Injector(final WroManager wroManager) {
    Validate.notNull(wroManager);
    this.wroManager = wroManager;
    this.groupsProcessor = new GroupsProcessor();

    this.resourceLocatorFactory = new InjectorResourceLocatorFactoryDecorator(wroManager.getResourceLocatorFactory(), this);
    wroManager.setResourceLocatorFactory(this.resourceLocatorFactory);

    this.processorsFactory = new InjectorProcessorsFactoryDecorator(wroManager.getProcessorsFactory(), this);
    wroManager.setProcessorsFactory(this.processorsFactory);

    inject(wroManager);
  }

  private PreProcessorExecutor getPreProcessorExecutor() {
    if (preProcessorExecutor == null) {
      preProcessorExecutor = new PreProcessorExecutor();
      inject(preProcessorExecutor);
    }
    return preProcessorExecutor;
  }

  /**
   * Scans the object and inject the supported values into the fields having @Inject annotation present.
   *
   * @param object {@link Object} which will be scanned for @Inject annotation presence.
   */
  public void inject(final Object object) {
    Validate.notNull(object);
    processInjectAnnotation(object);
  }


  /**
   * Check for each field from the passed object if @Inject annotation is present & inject the required field if
   * supported, otherwise warns about invalid usage.
   *
   * @param processor object to check for annotation presence.
   */
  private void processInjectAnnotation(final Object processor) {
    LOG.debug("processInjectAnnotation for: {}", processor.getClass().getSimpleName());
    try {
      final Collection<Field> fields = getAllFields(processor);
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
          if (!acceptAnnotatedField(processor, field)) {
            throw new WroRuntimeException("@Inject cannot be applied field of type: "
              + field.getType());
          }
        }
      }
    } catch (final Exception e) {
      LOG.error("Error while scanning @Inject annotation", e);
      throw new WroRuntimeException("Exception while trying to process @Inject annotation", e);
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
    boolean accept = false;
    try {
      // accept even private modifiers
      field.setAccessible(true);
      if (ResourceLocatorFactory.class.isAssignableFrom(field.getType())) {
        field.set(object, resourceLocatorFactory);
        return accept = true;
      }
      if (ProcessorsFactory.class.isAssignableFrom(field.getType())) {
        field.set(object, processorsFactory);
        return accept = true;
      }
      if (PreProcessorExecutor.class.isAssignableFrom(field.getType())) {
        field.set(object, getPreProcessorExecutor());
        return accept = true;
      }
      if (NamingStrategy.class.isAssignableFrom(field.getType())) {
        field.set(object, wroManager.getNamingStrategy());
        return accept = true;
      }
      if (GroupsProcessor.class.isAssignableFrom(field.getType())) {
        field.set(object, groupsProcessor);
        inject(groupsProcessor);
        return accept = true;
      }
      if (Injector.class.isAssignableFrom(field.getType())) {
        field.set(object, this);
        return accept = true;
      }
      return accept;
    } finally {
      if (accept) {
        LOG.debug("\t[OK] Injected field of type: {}", field.getType().getSimpleName());
      }
    }
  }
}
