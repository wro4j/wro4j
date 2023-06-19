/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.Resource;


/**
 * Perform actual processing of the resource content from the {@link Reader} and writes the processed content to the
 * {@link Writer}. The processor should focus only on transformation. If a processing fails, it is preferred to
 * propagate the exception, because wro4j can allow custom behavior for this situation. It is possible to treat the
 * exceptions by leaving the processed output unchanged, but in this case you may not benefit of global configuration.
 * 
 * @author Alex Objelean
 */
public interface ResourcePreProcessor {
  /**
   * Process a content supplied by a reader and perform some sort of processing. It is important to know that you should
   * use reader for processing instead of trying to access the resource original content using {@link Resource}, because
   * this way you can ignore the other preProcessors from the chain.<br/>
   * It is not require to explicitly handle exception. When the processing fails, the following can happen:
   * <ul>
   * <li>the exception is wrapped in {@link RuntimeException} and the processing chain is interrupted (by default)</li>
   * <li>content remains unchanged (if {@link WroConfiguration#isIgnoreFailingProcessor()} is true)</li>
   * </ul>
   * <br/>
   * It is not required to close the reader and writers, because these will be closed for you.
   * 
   * @param resource
   *          the original resource as it found in the model.
   * @param reader
   *          {@link Reader} used to read processed resource content.
   * @param writer
   *          {@link Writer} where used to write processed results.
   * @throws IOException
   *           when an exception occurs. The future version will change the exception type to {@link Exception}, because
   *           any exception may occur during processing. The processing failure will be handled based on value of
   *           {@link WroConfiguration#isIgnoreFailingProcessor()} configuration flag.
   */
  void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException;
}
