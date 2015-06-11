/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;

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
    implements ResourcePostProcessor, ResourcePreProcessor, Destroyable {
  public static final String ALIAS_SIMPLE = "googleClosureSimple";
  public static final String ALIAS_ADVANCED = "googleClosureAdvanced";
  public static final String ALIAS_WHITESPACE_ONLY = "googleClosureWhitespace";
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
    /**
     * Using pool to fix the threadSafety issue. See <a
     * href="http://code.google.com/p/closure-compiler/issues/detail?id=781">issue</a>.
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
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    final String content = IOUtils.toString(reader);
    final CompilerOptions compilerOptions = optionsPool.getObject();
    final Compiler compiler = newCompiler(compilerOptions);
    try {
      final String fileName = resource == null ? "wro4j-processed-file.js" : resource.getUri();
      final SourceFile[] input = new SourceFile[] {
        SourceFile.fromInputStream(fileName, new ByteArrayInputStream(content.getBytes(getEncoding())))
      };
      SourceFile[] externs = getExterns(resource);
      if (externs == null) {
        // fallback to empty array when null is provided.
        externs = new SourceFile[] {};
      }
      // js source map creation
      if (isJsSourceMapEnabled()) {
        createMapFile(externs);
      }
      Result result = null;
      result = compiler.compile(Arrays.asList(externs), Arrays.asList(input), compilerOptions);
      if (result.success) {
        writer.write(compiler.toSource());
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
    // javascript source maps creation 
    if( isJsSourceMapEnabled() ) {
    	options.setSourceMapOutputPath(Context.get().getConfig().getJsSourceMapPath());
    	options.setSourceMapDetailLevel(SourceMap.DetailLevel.ALL);
    	options.setSourceMapFormat(SourceMap.Format.DEFAULT);
    }
    // jQuery pass flag set (only useful if adavanced mode is set)
    if( Context.get().getConfig().isJqueryPass() ) {
    	options.jqueryPass = true;
    }
    return options;
  }
  
  /**
   * @return flag describing if js source map should be created
   */
  private boolean isJsSourceMapEnabled() {
	  WroConfiguration config = Context.get().getConfig();
	  return config.isMinimizeEnabled() && null != config.getJsSourceMapPath();
  }
  
  /**
   * Creates the map file compiling the whole list of resources at once
   * 
   * @param externs
   */
  private void createMapFile(SourceFile[] externs) {
	  WroConfiguration config = Context.get().getConfig();
	  if( !config.isJsSourceMapCreated() ){
		  try {
			  final CompilerOptions compilerOptions = optionsPool.getObject();
			  final Compiler compiler = newCompiler(compilerOptions);
			  
			  FileOutputStream mapFile = new FileOutputStream(config.getJsSourceMapPath());
			  Writer mapWriter = new BufferedWriter(new OutputStreamWriter(mapFile, config.getEncoding()));
			  
			  compiler.compile(new ArrayList<SourceFile>(1), getResourcesForCompile(config.getResources(), config.getJsSourceMapPath().substring(0, config.getJsSourceMapPath().lastIndexOf(File.separatorChar))), compilerOptions);
			  compiler.toSource();
			  // source map code append
			  compiler.getSourceMap().appendTo(mapWriter, new File(config.getJsSourceMapPath()).getName().replaceFirst(".map",".js"));
			  
			  // data to file
			  mapWriter.flush();
			  // clean the source urls cause resources use absolute path 
			  cleanMapFileSources();
			  // finalize file
			  mapWriter.close();
			  
			  config.setJsSourceMapCreated(true);
		  } catch (IOException e){
		      e.printStackTrace();
		  }
	  }
  }
  
  /**
   * Prepare the resources list to be consumed by Compiler
   * 
   * @param resources
   * @return {List<SourceFile>}
   */
  private List<SourceFile> getResourcesForCompile(List<Resource> resources, String contextPath) {
	List<SourceFile> inputs = new ArrayList<SourceFile>(resources.size());
	for( Resource resource : resources ) {
	  String splitter = File.separatorChar == '\\' ? "\\\\" : "////";
	  String context = Paths.get(contextPath).toString().concat(File.separator);
	  
	  String[] p = Paths.get(resource.getUri()).toString().split(splitter);
      for (int i=0; i<=p.length-1; i++) {
		if (p[i].length() > 0) {
		  context = context.replace(p[i], "");
		}
      }
	  context = Paths.get(context).toString();
	  
	  inputs.add(SourceFile.fromFile(context.concat(resource.getUri()), Charset.forName(Context.get().getConfig().getEncoding())));
	}
	return inputs;
  }
  
  /**
   * Map file sources absolute paths replaced by relative paths.
   * Normally, resultant path would be the same as passed in the
   * group list in wro.xml 
   */
  private void cleanMapFileSources(){
	WroConfiguration config = Context.get().getConfig();
	try {
	  File file = new File(config.getJsSourceMapPath());
	  List<String> lines = FileUtils.readLines(file, config.getEncoding());
	  String sources = lines.get(5);
	  String path = file.getParent();
	  if (File.separatorChar == '\\') {
		path = path.replace(File.separatorChar, '/').concat("/");
	  }
	  sources = sources.replaceAll(path, "");
	  lines.set(5, sources);
	  FileUtils.writeLines(file, lines, "\n");
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  @Override
  public void destroy()
      throws Exception {
    optionsPool.destroy();
  }
}
