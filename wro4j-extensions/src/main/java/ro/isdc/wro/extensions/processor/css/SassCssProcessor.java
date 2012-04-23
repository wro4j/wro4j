/*
 * Copyright (C) 2010. All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.sass.RubySassEngine;
import ro.isdc.wro.extensions.processor.support.sass.SassCss;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * A processor using sass engine:
 * 
 * @author Alex Objelean
 * @created 27 Oct 2010
 */
@SupportedResourceType(ResourceType.CSS)
public class SassCssProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(SassCssProcessor.class);
  public static final String ALIAS = "sassCss";
  public static final String ALIAS_RUBY = "rubySassCss";
  
  /**
   * Private class definition for reflection use
   */
  private Class<?> clazz = SassCssProcessor.class;
  
  /**
   * ENUM for supported engine types
   */
  public static enum Engines {
    RHINO, RUBY
  }
  
  /**
   * Set private engine property.
   */
  private Engines engine;
  
  /**
   * default constructor that sets the engine used to RHINO for backwards compatibility.
   */
  public SassCssProcessor() {
    this.engine = Engines.RHINO;
  }
  
  /**
   * Overloaded constructor that accepts the engine to use for processing.
   * 
   * @param eg
   */
  public SassCssProcessor(Engines eg) {
    this.engine = eg;
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(processUsingEngine(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + clazz.getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }
  
  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
  }
  
  /**
   * Processor factory for use with multiple engines
   * 
   * @param content
   * @return Processed string
   */
  private String processUsingEngine(String content) {
    
    switch (engine) {
      case RUBY:
        return processUsingRuby(content);
      default:
        return processUsingRhino(content);
        
    }
  }
  
  /**
   * Method for processing with Rhino based engine
   * 
   * @param content
   * @return
   */
  private String processUsingRhino(String content) {
    return new SassCss().process(content);
  }
  
  /**
   * Method for processing with Ruby based engine
   * 
   * @param context
   * @return
   */
  private String processUsingRuby(String context) {
    return new RubySassEngine().process(context);
  }
  
  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
  
}
