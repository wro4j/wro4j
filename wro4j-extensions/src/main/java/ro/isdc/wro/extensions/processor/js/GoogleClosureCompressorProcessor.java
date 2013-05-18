/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;


/**
 * Uses Google closure compiler for js minimization.
 * <p/>
 * In order to make this class {@link Context} agnostic, set the encoding explicitly using
 * {@link GoogleClosureCompressorProcessor#setEncoding(String)}.
 *
 * @see http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class GoogleClosureCompressorProcessor
    implements ResourcePostProcessor, ResourcePreProcessor {
  public static final String ALIAS_SIMPLE = "googleClosureSimple";
  public static final String ALIAS_ADVANCED = "googleClosureAdvanced";
  public static final String ALIAS_WHITESPACE_ONLY = "googleClosureWhitespace";
  /**
   * {@link CompilationLevel} to use for compression.
   */
  private CompilationLevel compilationLevel;
  private CompilerOptions compilerOptions;
  @Inject
  private ReadOnlyContext context;
  private String encoding;

  /**
   * Uses google closure compiler with default compilation level: {@link CompilationLevel#SIMPLE_OPTIMIZATIONS}
   */
  public GoogleClosureCompressorProcessor() {
    this(CompilationLevel.SIMPLE_OPTIMIZATIONS);
  }

  /**
   * Uses google closure compiler with specified compilation level.
   *
   * @param compilationLevel
   *          not null {@link CompilationLevel} enum.
   */
  public GoogleClosureCompressorProcessor(final CompilationLevel compilationLevel) {
    Validate.notNull(compilationLevel);
    this.compilationLevel = compilationLevel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      Compiler.setLoggingLevel(Level.SEVERE);
      final Compiler compiler = new Compiler();
      if (compilerOptions == null) {
        compilerOptions = newCompilerOptions();
      }

      final String fileName = resource == null ? "wro4j-processed-file.js" : resource.getUri();
      final SourceFile[] input = new SourceFile[] {
          SourceFile.fromInputStream(fileName, new ByteArrayInputStream(content.getBytes(getEncoding())))
      };
      SourceFile[] externs = getExterns(resource);
      if (externs == null) {
        // fallback to empty array when null is provided.
        externs = new SourceFile[] {};
      }
      Result result = null;
      /**
       * fix the threadSafety issue.<br/>
       * TODO remove synchronization after the <a
       * href="http://code.google.com/p/closure-compiler/issues/detail?id=781">issue</a> is fixed
       */
      synchronized (this) {
        compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        // make it play nice with GAE
        compiler.disableThreads();
        compiler.initOptions(compilerOptions);
        result = compiler.compile(Arrays.asList(externs), Arrays.asList(input), compilerOptions);
      }
      if (result.success) {
        writer.write(compiler.toSource());
      } else {
        throw new WroRuntimeException("Compilation has errors: " + Arrays.asList(result.errors));
      }
    } catch(final Exception e) {
      onException(e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * Invoked when an exception occurs during processing. Default implementation wraps the exception into
   * {@link WroRuntimeException} and throws it further.
   *
   * @param e
   *          {@link Exception} thrown during processing.
   */
  protected void onException(final Exception e) {
    throw WroRuntimeException.wrap(e);
  }

  private String getEncoding() {
    if (encoding == null) {
      // use config is available to get encoding
      this.encoding = Context.isContextSet() ? context.getConfig().getEncoding() : WroConfiguration.DEFAULT_ENCODING;
    }
    return encoding;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public GoogleClosureCompressorProcessor setEncoding(final String encoding) {
    this.encoding = encoding;
    return this;
  }

  /**
   * @param resource
   *          Currently processed resource. The resource can be null, when the closure compiler is used as a post
   *          processor.
   * @return An Array of externs files for the resource to process.
   */
  protected SourceFile[] getExterns(final Resource resource) {
    return new SourceFile[] {};
  }

  /**
   * @param compilerOptions
   *          the compilerOptions to set
   */
  public GoogleClosureCompressorProcessor setCompilerOptions(final CompilerOptions compilerOptions) {
    this.compilerOptions = compilerOptions;
    return this;
  }

  /**
   * @param compilationLevel
   *          the compilationLevel to set
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
     * According to John Lenz from the Closure Compiler project, if you are using the Compiler API directly, you should
     * specify a CodingConvention. {@link http://code.google.com/p/wro4j/issues/detail?id=155}
     */
    options.setCodingConvention(new ClosureCodingConvention());
    // use the wro4j encoding by default
    options.setOutputCharset(getEncoding());
    // set it to warning, otherwise compiler will fail
    options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
    return options;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
}
