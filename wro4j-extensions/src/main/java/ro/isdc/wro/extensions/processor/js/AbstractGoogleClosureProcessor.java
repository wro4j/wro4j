package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;

import org.apache.commons.lang3.Validate;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

public abstract class AbstractGoogleClosureProcessor<T extends AbstractGoogleClosureProcessor<T>>
        implements ResourcePostProcessor, ResourcePreProcessor, Destroyable {
    /**
     * {@link CompilationLevel} to use for compression.
     */
    private CompilationLevel compilationLevel;
    /**
     * Reuse options(which are not thread safe).
     */
    private ObjectPoolHelper<CompilerOptions> optionsPool;
    @Inject
    private ReadOnlyContext context;
    private String encoding;

    /**
     * Uses google closure compiler with default compilation level:
     * {@link CompilationLevel#SIMPLE_OPTIMIZATIONS}
     */
    public AbstractGoogleClosureProcessor() {
        this(CompilationLevel.SIMPLE_OPTIMIZATIONS);
    }

    /**
     * Uses google closure compiler with specified compilation level.
     *
     * @param compilationLevel
     *            not null {@link CompilationLevel} enum.
     */
    public AbstractGoogleClosureProcessor(final CompilationLevel compilationLevel) {
        Validate.notNull(compilationLevel);
        /**
         * Using pool to fix the threadSafety issue. See <a href=
         * "http://code.google.com/p/closure-compiler/issues/detail?id=781">issue</a>.
         */
        optionsPool = new ObjectPoolHelper<CompilerOptions>(new ObjectFactory<CompilerOptions>() {
            @Override
            public CompilerOptions create() {
                return newCompilerOptions();
            }
        });
        this.compilationLevel = compilationLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
        final CompilerOptions compilerOptions = optionsPool.getObject();
        final Compiler compiler = newCompiler(compilerOptions);
        try {
            final String fileName = resource == null ? "wro4j-processed-file.js" : resource.getUri();
            final SourceFile[] input = new SourceFile[] { SourceFile.fromReader(fileName, reader) };
            SourceFile[] externs = getExterns(resource);
            if (externs == null) {
                // fallback to empty array when null is provided.
                externs = new SourceFile[] {};
            }
            Result result = null;
            result = compiler.compile(Arrays.asList(externs), Arrays.asList(input), compilerOptions);
            if (result.success) {
                writeResults(compiler, fileName, writer);
            } else {
                throw new WroRuntimeException("Compilation has errors: " + Arrays.asList(result.errors));
            }
        } catch (final Exception e) {
            onException(e);
        } finally {
            reader.close();
            writer.close();
            optionsPool.returnObject(compilerOptions);
        }
    }

    abstract void writeResults(Compiler compiler, String fileName, Writer writer) throws IOException;

    /**
     * Invoked when an exception occurs during processing. Default
     * implementation wraps the exception into {@link WroRuntimeException} and
     * throws it further.
     *
     * @param e
     *            {@link Exception} thrown during processing.
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

    private Compiler newCompiler(final CompilerOptions compilerOptions) {
        Compiler.setLoggingLevel(Level.SEVERE);
        final Compiler compiler = new Compiler();
        compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        // make it play nice with GAE
        compiler.disableThreads();
        compiler.initOptions(compilerOptions);
        return compiler;
    }

    /**
     * @param encoding
     *            the encoding to set
     */
    @SuppressWarnings("unchecked")
    public T setEncoding(final String encoding) {
        this.encoding = encoding;
        return (T) this;
    }

    /**
     * @param resource
     *            Currently processed resource. The resource can be null, when
     *            the closure compiler is used as a post processor.
     * @return An Array of externs files for the resource to process.
     */
    protected SourceFile[] getExterns(final Resource resource) {
        return new SourceFile[] {};
    }

    /**
     * @param compilationLevel
     *            the compilationLevel to set
     */
    @SuppressWarnings("unchecked")
    public T setCompilationLevel(final CompilationLevel compilationLevel) {
        this.compilationLevel = compilationLevel;
        return (T) this;
    }

    /**
     * @return default {@link CompilerOptions} object to be used by compressor.
     */
    protected CompilerOptions newCompilerOptions() {
        final CompilerOptions options = new CompilerOptions();
        /**
         * According to John Lenz from the Closure Compiler project, if you are
         * using the Compiler API directly, you should specify a
         * CodingConvention.
         * {@link http://code.google.com/p/wro4j/issues/detail?id=155}
         */
        options.setCodingConvention(new ClosureCodingConvention());
        // use the wro4j encoding by default
        options.setOutputCharset(Charset.forName(getEncoding()));
        // set it to warning, otherwise compiler will fail
        options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final Reader reader, final Writer writer) throws IOException {
        process(null, reader, writer);
    }

    @Override
    public void destroy() throws Exception {
        optionsPool.destroy();
    }
}
