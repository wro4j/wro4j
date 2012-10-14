package ro.isdc.wro.util;

import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
    extends AbstractDecorator<ObjectFactory<T>> {
  private static final Logger LOG = LoggerFactory.getLogger(ProxyFactory.class);
  private final Class<T> genericType;

  /**
   * @param lazyobjectFactory
   *          used as a factory for the proxied object.
   * @param genericType
   *          the Class of the generic object, required to create the proxy. This argument is required because of type
   *          erasure and generics info aren't available at runtime.
   */
  public ProxyFactory(final ObjectFactory<T> objectFactory, final Class<T> genericType) {
    super(objectFactory);
    notNull(genericType);
    this.genericType = genericType;
  }

  /**
   * Creates a proxy for the provided object.
   *
   * @param object
   *          for which a proxy will be created.
   * @param the
   *          type of the provided object.
   */
  public ProxyFactory(final T object, final Class<T> type) {
    this(new ObjectFactory<T>() {
      public T create() {
        return object;
      }
    }, type);
  }

  /**
   * {@inheritDoc}
   */
  public T create() {
    final InvocationHandler handler = new InvocationHandler() {
      public Object invoke(final Object proxy, final Method method, final Object[] args)
          throws Throwable {
        final Object object = getDecoratedObject().create();
        notNull(object, "Cannot Create Proxy for NULL object");
        return method.invoke(object, args);
      }
    };
    LOG.debug("genericType: {}", genericType);
    if (genericType == null) {
      throw new IllegalArgumentException("Could not determine the genericType");
    }
    final T proxy = (T) Proxy.newProxyInstance(genericType.getClassLoader(), new Class[] {
      genericType
    }, handler);
    return proxy;
  }
}
