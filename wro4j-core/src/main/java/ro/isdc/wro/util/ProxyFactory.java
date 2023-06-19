package ro.isdc.wro.util;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An {@link ObjectFactory} used to create Proxy for objects initialized by provided {@link LazyInitializer}'s.
 *
 * @author Alex Objelean
 * @since 1.6.0
 * @param <T>
 *          the type of the object to create.
 */
public class ProxyFactory<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ProxyFactory.class);
  private final Class<T> genericType;

  private final TypedObjectFactory<T> objectFactory;

  /**
   * A specialized version of {@link ObjectFactory} which provides more information about type class. This is required
   * to avoid premature creation because the class cannot be extracted from generic type.
   */
  public static interface TypedObjectFactory <T> extends ObjectFactory<T> {
    Class<? extends T> getObjectClass();
  }

  /**
   * Creates a proxy for the provided object.
   *
   * @param objectFactory
   *          Object factory for which a proxy will be created.
   * @param genericType
   *          the Class of the generic object, required to create the proxy. This argument is required because of type
   *          erasure and generics info aren't available at runtime.
   */
  private ProxyFactory(final TypedObjectFactory<T> objectFactory, final Class<T> genericType) {
    notNull(objectFactory);
    notNull(genericType);
    this.objectFactory = objectFactory;
    this.genericType = genericType;
  }

  public static <T> T proxy(final TypedObjectFactory<T> objectFactory, final Class<T> genericType) {
    try {
      return new ProxyFactory<T>(objectFactory, genericType).create();
    } catch (final RuntimeException e) {
      LOG.error("exception", e);
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  private T create() {
    final InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(final Object proxy, final Method method, final Object[] args)
          throws Throwable {
        try {
          return method.invoke(objectFactory.create(), args);
        } catch (final InvocationTargetException ex) {
          // Preserve original exception
          throw ex.getCause();
        }
      }
    };
    LOG.debug("genericType: {}", genericType);
    return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), getInterfacesSet().toArray(
        new Class[] {}), handler);
  }

  /**
   * @return a set of interfaces supported by proxied object.
   */
  private Set<Class<?>> getInterfacesSet() {
    final Set<Class<?>> set = new HashSet<Class<?>>();
    if (genericType.isInterface()) {
      set.add(genericType);
    }
    final Class<?>[] classes = objectFactory.getObjectClass().getInterfaces();
    for (final Class<?> clazz : classes) {
      set.add(clazz);
    }
    LOG.debug("interfaces set: {}", set);
    return set;
  }
}
