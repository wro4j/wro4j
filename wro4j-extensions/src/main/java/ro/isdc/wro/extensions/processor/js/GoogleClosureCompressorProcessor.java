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
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;

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
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(GoogleClosureCompressorProcessor.class);
  public static final String ALIAS_SIMPLE = "googleClosureSimple";
  public static final String ALIAS_ADVANCED = "googleClosureAdvanced";
  /**
   * {@link CompilationLevel} to use for compression.
   */
  private CompilationLevel compilationLevel;
  private CompilerOptions compilerOptions;

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
    Validate.notNull(compilationLevel);
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
      if (compilerOptions == null) {
        compilerOptions = newCompilerOptions();
      }
      compilationLevel.setOptionsForCompilationLevel(compilerOptions);
      //This is important in order to avoid INTERNAL_ERROR (@see https://groups.google.com/forum/#!topic/closure-compiler-discuss/TDPtHU503Xk}
      compilerOptions.foldConstants = false;
      //make it play nice with GAE
      compiler.disableThreads();
      compiler.initOptions(compilerOptions);

      final JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
      final String fileName = resource == null ? "wro4j-processed-file.js" : resource.getUri();
      final JSSourceFile input = JSSourceFile.fromInputStream(fileName,
        new ByteArrayInputStream(content.getBytes(Context.get().getConfig().getEncoding())));
      final Result result = compiler.compile(extern, input, compilerOptions);
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

  /**
   * @param compilerOptions the compilerOptions to set
   */
  public GoogleClosureCompressorProcessor setCompilerOptions(final CompilerOptions compilerOptions) {
    this.compilerOptions = compilerOptions;
    return this;
  }

  /**
   * @param compilationLevel the compilationLevel to set
   */
  public GoogleClosureCompressorProcessor setCompilationLevel(final CompilationLevel compilationLevel) {
    this.compilationLevel = compilationLevel;
    return this;
  }

  /**
   * @return default {@link CompilerOptions} object to be used by compressor.
   */
  protected CompilerOptions newCompilerOptions() {
    final CompilerOptions options = new CompilerOptions();
    /**
     * According to John Lenz from the Closure Compiler project, if you are using the Compiler API directly, you
     * should specify a CodingConvention. {@link http://code.google.com/p/wro4j/issues/detail?id=155}
     */
    options.setCodingConvention(new DefaultCodingConvention());
    //use the wro4j encoding by default
    options.setOutputCharset(Context.get().getConfig().getEncoding());
    //set it to warning, otherwise compiler will fail
    options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES,
      CheckLevel.WARNING);
    return options;
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
}
