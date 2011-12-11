/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.InjectorUriLocatorFactoryDecorator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
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
  private final UriLocatorFactory uriLocatorFactory;
  private final ProcessorsFactory processorsFactory;
  private final GroupsProcessor groupsProcessor = new GroupsProcessor();
  private PreProcessorExecutor preProcessorExecutor = new PreProcessorExecutor();
  /**
   * Mapping of classes to be annotated and the coresponding injected object.
   */
  private Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

  /**
   * Creates the Injector and initialize the provided manager.
   */
  public Injector(final WroManager wroManager) {
    Validate.notNull(wroManager);
    this.wroManager = wroManager;

    this.uriLocatorFactory = new InjectorUriLocatorFactoryDecorator(wroManager.getUriLocatorFactory(), this);
    wroManager.setUriLocatorFactory(this.uriLocatorFactory);

    this.processorsFactory = new InjectorProcessorsFactoryDecorator(wroManager.getProcessorsFactory(), this);
    wroManager.setProcessorsFactory(this.processorsFactory);
    //first initialize the map
    initMap();

    inject(preProcessorExecutor);
    inject(groupsProcessor);
    inject(wroManager);
  }

  private void initMap() {
    map.put(PreProcessorExecutor.class, preProcessorExecutor);
    map.put(GroupsProcessor.class, groupsProcessor);
    map.put(UriLocatorFactory.class, uriLocatorFactory);
    map.put(ProcessorsFactory.class, processorsFactory);
    map.put(NamingStrategy.class, wroManager.getNamingStrategy());
    map.put(LifecycleCallbackRegistry.class, wroManager.getCallbackRegistry());
    map.put(Injector.class, this);
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
      for (final Map.Entry<Class<?>, Object> entry : map.entrySet()) {
        if (entry.getKey().isAssignableFrom(field.getType())) {
          field.set(object, entry.getValue());
          return accept = true;
        }
      }
      return accept;
    } finally {
      if (accept) {
        LOG.debug("\t[OK] Injected field of type: {} for object of type: {}", field.getType().getSimpleName(), object.getClass().getSimpleName());
      }
    }
  }
}
