/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.AbstractWroTest;
import ro.isdc.wro.processor.impl.CssUrlRewritingProcessor;
import ro.isdc.wro.test.util.ResourceProcessor;

/**
 * TestProcessor.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class TestCssUrlRewritingProcessor extends AbstractWroTest {
  private ResourcePreProcessor processor;

  private static final String CSS_INPUT_NAME = "cssUrlRewriting.css";

  @Before
  public void init() {
    processor = new CssUrlRewritingProcessor() {
      @Override
      protected String getUrlPrefix() {
        return "[WRO-PREFIX]?id=";
      }
    };
  }

  /**
   * Test a classpath css resource.
   * 
   * @throws IOException
   */
  @Test
  public void processClasspathResourceType() throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
        "classpath:cssUrlRewriting-classpath-outcome.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process("classpath:" + CSS_INPUT_NAME, reader, writer);
          }
        });
  }

  /**
   * Test a servletContext css resource.
   * 
   * @throws IOException
   */
  @Test
  public void processServletContextResourceType() throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
        "classpath:cssUrlRewriting-servletContext-outcome.css",
        new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process("/static/img/" + CSS_INPUT_NAME, reader, writer);
          }
        });
  }

  /**
   * Test a url css resource.
   * 
   * @throws IOException
   */
  @Test
  public void processUrlResourceType() throws IOException {
    compareProcessedResourceContents("classpath:" + CSS_INPUT_NAME,
        "classpath:cssUrlRewriting-url-outcome.css", new ResourceProcessor() {
          public void process(final Reader reader, final Writer writer)
              throws IOException {
            processor.process("http://www.site.com/static/css/"
                + CSS_INPUT_NAME, reader, writer);
          }
        });
  }
}
