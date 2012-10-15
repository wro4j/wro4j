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
 * @since 1.5.1
 * @created 14 Oct 2012
 * @param <T>
 *          the type of the object to create.
 */
public class ProxyFactory<T>
    extends AbstractDecorator<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ProxyFactory.class);
  private final Class<T> genericType;

  /**
   * Creates a proxy for the provided object.
   *
   * @param object
   *          for which a proxy will be created.
   * @param genericType
   *          the Class of the generic object, required to create the proxy. This argument is required because of type
   *          erasure and generics info aren't available at runtime.
   */
  public ProxyFactory(final T object, final Class<T> genericType) {
    super(object);
    notNull(genericType);
    this.genericType = genericType;
  }

  public ProxyFactory(final T object) {
    this(object, (Class<T>) object.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public T create() {
    final InvocationHandler handler = new InvocationHandler() {
      public Object invoke(final Object proxy, final Method method, final Object[] args)
          throws Throwable {
        try {
          return method.invoke(getDecoratedObject(), args);
        } catch (final InvocationTargetException ex) {
          //Preserve original exception
          throw ex.getCause();
        }
      }
    };
    LOG.debug("genericType: {}", genericType);
    return (T) Proxy.newProxyInstance(getDecoratedObject().getClass().getClassLoader(),
        getInterfacesSet().toArray(new Class[] {}), handler);
  }

  /**
   * @return a set of interfaces supported by proxied object.
   */
  private Set<Class<?>> getInterfacesSet() {
    final Set<Class<?>> set = new HashSet<Class<?>>();
    if (genericType.isInterface()) {
      set.add(genericType);
    }
    final Class[] classes = getDecoratedObject().getClass().getInterfaces();
    for (final Class<?> clazz : classes) {
      set.add(clazz);
    }
    LOG.debug("interfaces set: {}", set);
    return set;
  }
}
