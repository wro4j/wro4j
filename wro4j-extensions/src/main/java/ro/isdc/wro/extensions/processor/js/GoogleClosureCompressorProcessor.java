/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.jscomp.DiagnosticGroups;
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
  implements ResourcePreProcessor {
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
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      Compiler.setLoggingLevel(Level.SEVERE);
      final Compiler compiler = new Compiler();
      final CompilerOptions options = new CompilerOptions();
      compilationLevel.setOptionsForCompilationLevel(options);
      //This is important in order to avoid INTERNAL_ERROR (@see https://groups.google.com/forum/#!topic/closure-compiler-discuss/TDPtHU503Xk}
      options.foldConstants = false;
      //make it play nice with GAE
      compiler.disableThreads();
      compiler.initOptions(options);
      /**
       * According to John Lenz from the Closure Compiler project, if you are using the Compiler API directly, you
       * should specify a CodingConvention. {@link http://code.google.com/p/wro4j/issues/detail?id=155}
       */
      options.setCodingConvention(new DefaultCodingConvention());
      //set it to warning, otherwise compiler will fail
      options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES,
        CheckLevel.WARNING);

      final JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
      final String fileName = resource == null ? "wro4j-processed-file.js" : resource.getUri();
      final JSSourceFile input = JSSourceFile.fromInputStream(fileName,
        new ByteArrayInputStream(content.getBytes(Context.get().getConfig().getEncoding())));
      final Result result = compiler.compile(extern, input, options);
      if (result.success) {
        writer.write(compiler.toSource());
      } else {
        writer.write(content);
      }
    } finally {
      LOG.debug("finally");
      reader.close();
      writer.close();
    }
  }
}
