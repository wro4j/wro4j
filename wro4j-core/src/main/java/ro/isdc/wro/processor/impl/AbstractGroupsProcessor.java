/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;

/**
 * AbstractGroupsProcessor.java.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 26, 2008
 */
public abstract class AbstractGroupsProcessor implements GroupsProcessor {

  /**
   * a list of css pre processors.
   */
  private final List<ResourcePreProcessor> cssPreProcessors = new ArrayList<ResourcePreProcessor>();

  /**
   * a list of js pre processors.
   */
  private final List<ResourcePreProcessor> jsPreProcessors = new ArrayList<ResourcePreProcessor>();

  /**
   * a list of pre processors for all resources, will be applied on both (css &
   * js).
   */
  private final List<ResourcePreProcessor> anyResourcePreProcessors = new ArrayList<ResourcePreProcessor>();

  /**
   * a list of css post processors.
   */
  private final List<ResourcePostProcessor> cssPostProcessors = new ArrayList<ResourcePostProcessor>();

  /**
   * a list of js post processors.
   */
  private final List<ResourcePostProcessor> jsPostProcessors = new ArrayList<ResourcePostProcessor>();

  /**
   * a list of post processors for all resources, will be applied on both (css &
   * js).
   */
  private final List<ResourcePostProcessor> anyResourcePostProcessors = new ArrayList<ResourcePostProcessor>();

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends ResourcePreProcessor> T findPreProcessorByClass(final Class<T> processorClass) {
    T found = null;
    final Set<ResourcePreProcessor> allPreProcessors = new HashSet<ResourcePreProcessor>();
    allPreProcessors.addAll(cssPreProcessors);
    allPreProcessors.addAll(jsPreProcessors);
    allPreProcessors.addAll(anyResourcePreProcessors);
    for (final ResourcePreProcessor processor : allPreProcessors) {
      if (processorClass.isInstance(processor)) {
        found = (T) processor;
        return found;
      }
    }
    return null;
  }

  /**
   * @param processors
   *          a list of css post processors.
   */
  public final void setCssPostProcessors(
      final List<ResourcePostProcessor> processors) {
    this.cssPostProcessors.addAll(processors);
  }

  /**
   * @param cssPreProcessors
   *          the cssPreProcessors to set
   */
  public final void setCssPreProcessors(
      final List<ResourcePreProcessor> cssPreProcessors) {
    this.cssPreProcessors.addAll(cssPreProcessors);
  }

  /**
   * @param jsPreProcessors
   *          the jsPreProcessors to set
   */
  public final void setJsPreProcessors(
      final List<ResourcePreProcessor> jsPreProcessors) {
    this.jsPreProcessors.addAll(jsPreProcessors);
  }

  /**
   * @param anyResourcePreProcessors
   *          the anyResourcePreProcessors to set
   */
  public final void setAnyResourcePreProcessors(
      final List<ResourcePreProcessor> anyResourcePreProcessors) {
    this.anyResourcePreProcessors.addAll(anyResourcePreProcessors);
  }

  /**
   * @param jsPostProcessors
   *          the jsPostProcessors to set
   */
  public final void setJsPostProcessors(
      final List<ResourcePostProcessor> jsPostProcessors) {
    this.jsPostProcessors.addAll(jsPostProcessors);
  }

  /**
   * @param anyResourcePostProcessors
   *          the anyResourcePostProcessors to set
   */
  public final void setAnyResourcePostProcessors(
      final List<ResourcePostProcessor> anyResourcePostProcessors) {
    this.anyResourcePostProcessors.addAll(anyResourcePostProcessors);
  }

  /**
   * @return the cssPreProcessors
   */
  protected final List<ResourcePreProcessor> getCssPreProcessors() {
    return cssPreProcessors;
  }

  /**
   * @return the jsPreProcessors
   */
  protected final List<ResourcePreProcessor> getJsPreProcessors() {
    return jsPreProcessors;
  }

  /**
   * @return the anyResourcePreProcessors
   */
  protected final List<ResourcePreProcessor> getAnyResourcePreProcessors() {
    return anyResourcePreProcessors;
  }

  /**
   * @return the cssPostProcessors
   */
  protected final List<ResourcePostProcessor> getCssPostProcessors() {
    return cssPostProcessors;
  }

  /**
   * @return the jsPostProcessors
   */
  protected final List<ResourcePostProcessor> getJsPostProcessors() {
    return jsPostProcessors;
  }

  /**
   * @return the anyResourcePostProcessors
   */
  protected final List<ResourcePostProcessor> getAnyResourcePostProcessors() {
    return anyResourcePostProcessors;
  }

	/**
	 * {@inheritDoc}
	 */
	public void addAnyPostProcessor(final ResourcePostProcessor processor) {
		anyResourcePostProcessors.add(processor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAnyPreProcessor(final ResourcePreProcessor processor) {
		anyResourcePreProcessors.add(processor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addCssPostProcessor(final ResourcePostProcessor processor) {
		cssPostProcessors.add(processor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addCssPreProcessor(final ResourcePreProcessor processor) {
		cssPreProcessors.add(processor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addJsPostProcessor(final ResourcePostProcessor processor) {
		jsPostProcessors.add(processor);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addJsPreProcessor(final ResourcePreProcessor processor) {
		jsPreProcessors.add(processor);
	}
}
