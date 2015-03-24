Note: This configuration can be done only in 1.3.x branch, because the newCacheStrategy method was private in earlier versions.

# Introduction #
In order to provide a custom caching implementation you have to follow these steps:

## Extend CacheStrategy ##
By default, wro4j provides MapCacheStrategy and LruMemoryCacheStrategy (this one is being used by default) which uses a map for caching. It is pretty straightforward to create your own implementation. Just implement the interface:
```
public class MyCacheStrategy<K, V> implements CacheStrategy<K, V> {
  public V get(final K key) {}
  public void put(final K key, final V value) {}
  public void clear() {}
  public void destroy() {}
}
```

## Extend WroManagerFactory ##
To instruct wro4j to use your custom caching strategy, you have to extend wroManagerFactory:
```
public class MyWroManagerFactory extends ServletContextAwareWroManagerFactory {
  @Override
  protected CacheStrategy<CacheEntry, ContentHashEntry> newCacheStrategy() {
    return new MyCacheStrategy<CacheEntry, ContentHashEntry>();
  }
}
```

## Update web.xml configuration ##
Update the xml, and confiure the filter to use you custom implemenetation of WroManagerFactory:

```
  <filter>
    <filter-name>WebResourceOptimizer</filter-name>
    <filter-class>
      ro.isdc.wro.http.WroFilter
    </filter-class>
    <init-param>
      <param-name>managerFactoryClassName</param-name>
      <param-value>com.mycompany.MyWroManagerFactory</param-value>
    </init-param>
  </filter>
```

That is enough to make Wro4j work with you custom cache strategy.