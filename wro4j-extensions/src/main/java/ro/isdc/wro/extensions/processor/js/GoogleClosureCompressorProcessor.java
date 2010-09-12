/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;


/**
 * Uses Google closure compiler for js minimization.
 *
 * @see http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class GoogleClosureCompressorProcessor
  implements ResourcePostProcessor, ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(GoogleClosureCompressorProcessor.class);
  /**
   * {@link CompilationLevel} to use for compression.
   */
  private final CompilationLevel compilationLevel;


  /**
   * Uses google closure compiler with default compilation level: {@link CompilationLevel#SIMPLE_OPTIMIZATIONS}
   */
  public GoogleClosureCompressorProcessor() {
    compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
  }


  /**
   * Uses google closure compiler with specified compilation level.
   *
   * @param compilationLevel not null {@link CompilationLevel} enum.
   */
  public GoogleClosureCompressorProcessor(final CompilationLevel compilationLevel) {
    if (compilationLevel == null) {
      throw new IllegalArgumentException("compilation level cannot be null!");
    }
    this.compilationLevel = compilationLevel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    process(reader, writer);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    try {
      final Compiler compiler = new Compiler();
      //make it play nice with GAE
      compiler.disableThreads();
      final CompilerOptions options = new CompilerOptions();
      // Advanced mode is used here, but additional options could be set, too.

      compilationLevel.setOptionsForCompilationLevel(options);
      final JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
      final JSSourceFile input = JSSourceFile.fromInputStream("", new ByteArrayInputStream(IOUtils.toByteArray(reader)));

      final Result result = compiler.compile(extern, input, options);
      if (result.success) {
        writer.write(compiler.toSource());
      } else {
        LOG.warn("The JS to compress contains errors: " + Arrays.toString(result.errors));
      }
    } finally {
      reader.close();
      writer.close();
    }
  }
}
