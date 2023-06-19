/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.InjectorBuilder.InjectorObjectFactory;
import ro.isdc.wro.util.ObjectDecorator;


/**
 * Injector scans some object fields and checks if a value can be provided to a field; Injector will ignore all non-null
 * fields.
 *
 * @author Alex Objelean
 */
public final class Injector {
  private static final Logger LOG = LoggerFactory.getLogger(Injector.class);
  private final Map<Class<?>, Object> map;
  private final Map<Object, Boolean> injectedObjects = Collections.synchronizedMap(new WeakHashMap<Object, Boolean>());
  /**
   * Mapping of classes to be annotated and the corresponding injected object.
   */
  Injector(final Map<Class<?>, Object> map) {
    notNull(map);
    this.map = map;
  }

  /**
   * Scans the object and inject the supported values into the fields having @Inject annotation present.
   *
   * @param object
   *          {@link Object} which will be scanned for @Inject annotation presence.
   * @return the injected object instance. Useful for fluent interface.
   */
  public <T> T inject(final T object) {
    notNull(object);
//    if (!Context.isContextSet()) {
//      throw new WroRuntimeException("No Context Set");
//    }
    if (!injectedObjects.containsKey(computeKey(object))) {
      injectedObjects.put(computeKey(object), true);
      processInjectAnnotation(object);
    }
    return object;
  }

  private <T> int computeKey(final T object) {
    return System.identityHashCode(object);
  }

  /**
   * Check for each field from the passed object if @Inject annotation is present & inject the required field if
   * supported, otherwise warns about invalid usage.
   *
   * @param object
   *          to check for annotation presence.
   */
  private void processInjectAnnotation(final Object object) {
    try {
      final Collection<Field> fields = getAllFields(object);
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
          if (!acceptAnnotatedField(object, field)) {
            final String message = String.format(
                "@Inject cannot be applied on object: %s to field of type: %s using injector %s", object,
                field.getType(), this);
            LOG.error("{}. Supported types are: {}", message, map.keySet());
            throw new WroRuntimeException(message);
          }
        }
      }
      // handle special cases like decorators. Perform recursive injection
      if (object instanceof ObjectDecorator) {
        processInjectAnnotation(((ObjectDecorator<?>) object).getDecoratedObject());
      }
    } catch (final Exception e) {
      LOG.error("Error while scanning @Inject annotation", e);
      throw WroRuntimeException.wrap(e, "Exception while trying to process @Inject annotation on object: " + object);
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
    while (superClass != null) {
      fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
      superClass = superClass.getSuperclass();
    }
    return fields;
  }

  /**
   * Analyze the field containing {@link Inject} annotation and set its value to appropriate value. Override this method
   * if you want to inject something else but uriLocatorFactory.
   *
   * @param object
   *          an object containing @Inject annotation.
   * @param field
   *          {@link Field} object containing {@link Inject} annotation.
   * @return true if field was injected with some not null value.
   * @throws IllegalAccessException
   */
  private boolean acceptAnnotatedField(final Object object, final Field field)
      throws IllegalAccessException {
    boolean accept = false;
    // accept private modifiers
    field.setAccessible(true);
    for (final Map.Entry<Class<?>, Object> entry : map.entrySet()) {
      if (entry.getKey().isAssignableFrom(field.getType())) {
        Object value = entry.getValue();
        // treat factories as a special case for lazy load of the objects.
        if (value instanceof InjectorObjectFactory) {
          value = ((InjectorObjectFactory<?>) value).create();
          inject(value);
        }
        field.set(object, value);
        accept = true;
        break;
      }
    }
    return accept;
  }
}
