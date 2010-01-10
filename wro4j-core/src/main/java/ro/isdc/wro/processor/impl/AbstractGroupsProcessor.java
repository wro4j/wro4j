/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.annot.Inject;
import ro.isdc.wro.annot.SupportedResourceType;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * Implements basic methods. Specialized classes should inherit this class instead of interface.
 *
 * @author Alex Objelean
 * @created Created on Nov 26, 2008
 */
public abstract class AbstractGroupsProcessor implements GroupsProcessor {
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
   * Used to get stream of the resources.
   */
  private UriLocatorFactory uriLocatorFactory;


  /**
   * Decorate the passed collection by overriding add family methods & calling
   * {@link AbstractGroupsProcessor#processInjectAnnotation(Object) on each added element.
   */
  @SuppressWarnings("serial")
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
  public <T extends ResourcePreProcessor> T findPreProcessorByClass(final Class<T> processorClass) {
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
          if (field.getType().equals(UriLocatorFactory.class)) {
            //accept even private modifiers
            field.setAccessible(true);
            if (uriLocatorFactory == null) {
              throw new WroRuntimeException("No uriLocatorFactory detected! Did you forget to call setUriLocatorFactory before adding any processors?");
            }
            field.set(processor, uriLocatorFactory);
            LOG.debug("Successfully injected field: " + field.getName());
          } else {
            throw new WroRuntimeException("@Inject can be applied only on fiels of " + UriLocatorFactory.class.getName() + " type");
          }
        }
      }
    } catch (final Exception e) {
      throw new WroRuntimeException("Exception while trying to process Inject annotation", e);
    }
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
      final boolean isTypeSatisfied = type != null && supportedType != null && type == supportedType.type();
      if (isAnyTypeSatisfied || isTypeSatisfied) {
        found.add(processor);
      }
    }
    return found;
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
}
