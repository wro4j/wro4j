/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.WroManager;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.locator.UriLocator;


/**
 * Holds a list of uri locators. The uriLocator will be created based on the first
 * uriLocator from the supplied list which will accept the url.
 *
 * @author Alex Objelean
 * @created 4 Nov 2008
 */
public class SimpleUriLocatorFactory extends AbstractUriLocatorFactory {
  private final List<UriLocator> uriLocators = new ArrayList<UriLocator>();

  /**
   * @param uri to handle by the locator.
   * @return an instance of {@link UriLocator} which is capable of handling provided uri. Returns null if no locator
   *         found.
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
   * Allow adding more than one uriLocators.
   *
   * @param locators list of {@link UriLocator} arguments.
   */
  public final SimpleUriLocatorFactory addLocator(final UriLocator... locators) {
    for (final UriLocator locator : locators) {
      uriLocators.add(locator);
    }
    return this;
  }

  /**
   * @param locators {@link Collection} of locators to add.
   */
  public final SimpleUriLocatorFactory addLocators(final Collection<UriLocator> locators) {
    try {
      Context context = Context.get();
      String managerClassName = context.getConfig().getWroManagerClassName();

      Class clz = Class.forName(managerClassName);
      if (WroManagerFactory.class.isAssignableFrom(clz)) {
        WroManagerFactory manager = (WroManagerFactory) clz.newInstance();

        InjectorBuilder builder = InjectorBuilder.create(manager);
        Injector injector = builder.build();

        for (UriLocator locator : locators) {
          injector.inject(locator);
        }
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    uriLocators.addAll(locators);
    return this;
  }

  /**
   * @return the list of currently configured locators.
   * @VisibleForTesting
   */
  public List<UriLocator> getUriLocators() {
    return Collections.unmodifiableList(this.uriLocators);
  }
}
