/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.encoding.SmartEncodingInputStream;


/**
 * Implements basic methods. Specialized classes should inherit this class instead of interface.
 *
 * @author Alex Objelean
 * @created Created on Nov 26, 2008
 */
@SuppressWarnings("serial")
public abstract class AbstractGroupsProcessor
  implements GroupsProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractGroupsProcessor.class);
  /**
   * a list of pre processors.
   */
  private final Collection<ResourcePreProcessor> preProcessors = decorateCollection(new ArrayList<ResourcePreProcessor>());
  /**
   * a list of post processors.
   */
  private final Collection<ResourcePostProcessor> postProcessors = decorateCollection(new ArrayList<ResourcePostProcessor>());
  /**
   * Used to get a stream of the resources.
   */
  private UriLocatorFactory uriLocatorFactory;
  /**
   * If true, missing resources are ignored. By default this value is true.
   */
  private boolean ignoreMissingResources = true;


  /**
   * Decorate the passed collection by overriding add family methods & calling
   * {@link AbstractGroupsProcessor#processInjectAnnotation(Object) on each added element.
   */
  private <T> Collection<T> decorateCollection(final Collection<T> c) {
    return new ArrayList<T>(c) {
      @Override
      public void add(final int index, final T element) {
        processInjectAnnotation(element);
        super.add(index, element);
      };

      @Override
      public boolean add(final T element) {
        processInjectAnnotation(element);
        return super.add(element);
      };

      @Override
      public boolean addAll(final Collection<? extends T> c) {
        for (final T element : c) {
          processInjectAnnotation(element);
        }
        return super.addAll(c);
      }

      @Override
      public boolean addAll(final int index, final Collection<? extends T> c) {
        for (final T element : c) {
          processInjectAnnotation(element);
        }
        return super.addAll(index, c);
      }
    };
  }


  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public final <T extends ResourcePreProcessor> T findPreProcessorByClass(final Class<T> processorClass) {
    T found = null;
    final Set<ResourcePreProcessor> allPreProcessors = new HashSet<ResourcePreProcessor>();
    allPreProcessors.addAll(preProcessors);
    for (final ResourcePreProcessor processor : allPreProcessors) {
      if (processorClass.isInstance(processor)) {
        found = (T) processor;
        return found;
      }
    }
    return null;
  }


  /**
   * Check for each field from the passed object if @Inject annotation is present & inject the required field if
   * supported, otherwise warns about invalid usage.
   *
   * @param processor object to check for annotation presence.
   */
  private void processInjectAnnotation(final Object processor) {
    try {
      final Field[] fields = processor.getClass().getDeclaredFields();
      for (final Field field : fields) {
        if (field.isAnnotationPresent(Inject.class)) {
          if (!acceptAnnotatedField(processor, field)) {
            throw new WroRuntimeException("@Inject can be applied only on fiels of "
              + UriLocatorFactory.class.getName() + " type");
          }
        }
      }
    } catch (final Exception e) {
      throw new WroRuntimeException("Exception while trying to process Inject annotation", e);
    }
  }


  /**
   * Analyze the field containing {@link Inject} annotation and set its value to appropriate value. Override this method
   * if you want to inject something else but uriLocatorFactory.
   *
   * @param processor an instance of processor which is the holder of the field.
   * @param field {@link Field} object containing {@link Inject} annotation.
   * @return true if field was injected with some not null value.
   * @throws IllegalAccessException
   */
  protected boolean acceptAnnotatedField(final Object processor, final Field field)
    throws IllegalAccessException {
    field.setAccessible(true);
    if (field.getType().equals(UriLocatorFactory.class)) {
      // accept even private modifiers
      if (uriLocatorFactory == null) {
        throw new WroRuntimeException(
          "No uriLocatorFactory detected! Did you forget to call setUriLocatorFactory before adding any processors?");
      }
      field.set(processor, uriLocatorFactory);
      LOG.debug("Successfully injected field: " + field.getName());
      return true;
    }
    return false;
  }


  /**
   * @param <T> processor type. Can be {@link ResourcePreProcessor} or {@link ResourcePostProcessor}.
   * @param type {@link ResourceType} to apply for searching on available processors.
   * @param availableProcessors a list where to perform the search.
   * @return a list of found processors which satisfy the search criteria.
   */
  private <T> Collection<T> getProcessorsByType(final ResourceType type, final Collection<T> availableProcessors) {
    final Collection<T> found = new ArrayList<T>();
    for (final T processor : availableProcessors) {
      final SupportedResourceType supportedType = processor.getClass().getAnnotation(SupportedResourceType.class);
      final boolean isAnyTypeSatisfied = type == null && supportedType == null;
      final boolean isTypeSatisfied = type != null && supportedType != null && type == supportedType.value();
      if (isAnyTypeSatisfied || isTypeSatisfied) {
        found.add(processor);
      }
    }
    return found;
  }


  /**
   * @param resource {@link Resource} for which a Reader should be returned.
   * @return {@link Reader} for the resource.
   * @throws IOException if there was a problem retrieving a reader.
   * @throws WroRuntimeException if {@link Reader} is null.
   */
  protected final Reader getResourceReader(final Resource resource)
    throws IOException {
    Reader reader = null;
    final UriLocator locator = getUriLocatorFactory().getInstance(resource.getUri());
    if (locator != null) {
      final InputStream is = locator.locate(resource.getUri());
      // wrap reader with bufferedReader for top efficiency
      reader = new BufferedReader(new InputStreamReader(new SmartEncodingInputStream(is)));
    }
    if (reader == null) {
      // TODO skip invalid resource, instead of throwing exception
      throw new IOException("Exception while retrieving InputStream from uri: " + resource.getUri());
    }
    return reader;
  }

  /**
   * @param groups
   *          list of groups where to search resources to filter.
   * @param type
   *          of resources to collect.
   * @return a list of resources of provided type.
   */
  protected final List<Resource> getFilteredResources(final Collection<Group> groups, final ResourceType type) {
    final List<Resource> allResources = new ArrayList<Resource>();
    for (final Group group : groups) {
      allResources.addAll(group.getResources());
    }
    // retain only resources of needed type
    final List<Resource> filteredResources = new ArrayList<Resource>();
    for (final Resource resource : allResources) {
      if (type == resource.getType()) {
        if (filteredResources.contains(resource)) {
          LOG.warn("Duplicated resource detected: " + resource + ". This resource won't be included more than once!");
        } else {
          filteredResources.add(resource);
        }
      }
    }
    return filteredResources;
  }


  /**
   * @return the ignoreMissingResources
   */
  public boolean isIgnoreMissingResources() {
    return this.ignoreMissingResources;
  }


  /**
   * @param ignoreMissingResources the ignoreMissingResources to set
   */
  public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
    this.ignoreMissingResources = ignoreMissingResources;
  }


  /**
   * {@inheritDoc}
   */
  public void setResourcePreProcessors(final Collection<ResourcePreProcessor> processors) {
    preProcessors.clear();
    if (processors != null) {
      preProcessors.addAll(processors);
    }
  }


  /**
   * {@inheritDoc}
   */
  public void setResourcePostProcessors(final Collection<ResourcePostProcessor> processors) {
    postProcessors.clear();
    if (processors != null) {
      postProcessors.addAll(processors);
    }
  }


  /**
   * Add a {@link ResourcePreProcessor}.
   */
  public void addPreProcessor(final ResourcePreProcessor processor) {
    preProcessors.add(processor);
  }


  /**
   * Add a {@link ResourcePostProcessor}.
   */
  public void addPostProcessor(final ResourcePostProcessor processor) {
    postProcessors.add(processor);
  }


  /**
   * It is important to setUriLocators before any processors are set. Otherwise, some of them (which depends on
   * uriLocatorFactory) will not work properly.
   */
  public final void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
  }


  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return this.uriLocatorFactory;
  }


  /**
   * @param type of resource for which you want to apply preProcessors or null if it doesn't matter (any resource).
   * @return a list of {@link ResourcePreProcessor} by provided type.
   */
  public final Collection<ResourcePreProcessor> getPreProcessorsByType(final ResourceType type) {
    return getProcessorsByType(type, preProcessors);
  }


  /**
   * @param type of resource for which you want to apply postProcessors or null if it doesn't matter (any resource).
   * @return a list of {@link ResourcePostProcessor} by provided type.
   */
  public final Collection<ResourcePostProcessor> getPostProcessorsByType(final ResourceType type) {
    return getProcessorsByType(type, postProcessors);
  }

}
