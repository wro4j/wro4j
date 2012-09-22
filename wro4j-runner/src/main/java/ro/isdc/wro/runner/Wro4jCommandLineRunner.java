/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.model.factory.SmartWroModelFactory;
import ro.isdc.wro.extensions.processor.css.CssLintProcessor;
import ro.isdc.wro.extensions.processor.js.JsHintProcessor;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.InjectableContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;


/**
 * Default command line runner. Interprets arguments and perform a processing.
 * 
 * @author Alex Objelean
 * @since 1.3.4
 */
public class Wro4jCommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(Wro4jCommandLineRunner.class);
  private final File defaultWroFile = newDefaultWroFile();
  
  @Option(name = "-m", aliases = {
    "--minimize"
  }, usage = "Turns on the minimization by applying compressor")
  private boolean minimize;
  @Option(name = "--parallel", usage = "Turns on the parallel preProcessing of resources. This value is false by default.")
  private boolean parallelPreprocessing;
  @Option(name = "--targetGroups", metaVar = "GROUPS", usage = "Comma separated value of the group names from wro.xml to process. If none is provided, all groups will be processed.")
  private String targetGroups;
  @Option(name = "-i", aliases = {
    "--ignoreMissingResources"
  }, usage = "Ignores missing resources")
  private boolean ignoreMissingResources;
  @Option(name = "--wroFile", metaVar = "PATH_TO_WRO_XML", usage = "The path to the wro model file. By default the model is searched inse the user current folder.")
  private final File wroFile = defaultWroFile;
  @Option(name = "--contextFolder", metaVar = "PATH", usage = "Folder used as a root of the context relative resources. By default this is the user current folder.")
  private final File contextFolder = new File(System.getProperty("user.dir"));
  @Option(name = "--destinationFolder", metaVar = "PATH", usage = "Where to store the processed result. By default uses the folder named [wro].")
  private File destinationFolder = new File(System.getProperty("user.dir"), "wro");
  @Option(name = "-c", aliases = {
    "--compressor", "--preProcessors"
  }, metaVar = "COMPRESSOR", usage = "Comma separated list of pre-processors")
  private String preProcessorsList;
  @Option(name = "--postProcessors", metaVar = "POST_PROCESSOR", usage = "Comma separated list of post-processors")
  private String postProcessorsList;
  
  public static void main(final String[] args)
      throws Exception {
    new Wro4jCommandLineRunner().doMain(args);
  }
  
  /**
   * @return the location where wro file is located by default. Default implementation uses current directory where user
   *         is located.
   */
  protected File newDefaultWroFile() {
    return new File(System.getProperty("user.dir"), "wro.xml");
  }
  
  /**
   * @param args
   */
  protected void doMain(final String[] args) {
    LOG.debug("arguments: " + Arrays.toString(args));
    final CmdLineParser parser = new CmdLineParser(this);
    parser.setUsageWidth(100);
    final StopWatch watch = new StopWatch();
    watch.start("processing");
    try {
      parser.parseArgument(args);
      LOG.debug("Options: {}", this);
      process();
    } catch (final Exception e) {
      System.err.println(e.getMessage() + "\n\n");
      System.err.println("=======================================");
      System.err.println("USAGE");
      System.err.println("=======================================");
      parser.printUsage(System.err);
      onRunnerException(e);
    } finally {
      watch.stop();
      LOG.debug(watch.prettyPrint());
      LOG.info("Processing took: {}ms", watch.getLastTaskTimeMillis());
    }
  }
  
  /**
   * Exception handler.
   */
  protected void onRunnerException(final Exception e) {
    System.out.println(e.getMessage());
    System.exit(1); // non-zero exit code indicates there was an error
  }
  
  private void process() {
    try {
      Context.set(Context.standaloneContext());
      // create destinationFolder if needed
      if (!destinationFolder.exists()) {
        destinationFolder.mkdirs();
      }
      final Collection<String> groupsAsList = getTargetGroupsAsList();
      for (final String group : groupsAsList) {
        for (final ResourceType resourceType : ResourceType.values()) {
          final String groupWithExtension = group + "." + resourceType.name().toLowerCase();
          processGroup(groupWithExtension, destinationFolder);
        }
      }
    } catch (final IOException e) {
      System.err.println(e.getMessage());
    }
  }
  
  /**
   * @return a list containing all groups needs to be processed.
   */
  private List<String> getTargetGroupsAsList() {
    if (targetGroups == null) {
      final WroModel model = getManagerFactory().create().getModelFactory().create();
      return new WroModelInspector(model).getGroupNames();
    }
    return Arrays.asList(targetGroups.split(","));
  }
  
  /**
   * Process a single group.
   * 
   * @throws IOException
   *           if any IO related exception occurs.
   */
  private void processGroup(final String group, final File parentFoder)
      throws IOException {
    ByteArrayOutputStream resultOutputStream = null;
    InputStream resultInputStream = null;
    try {
      LOG.info("processing group: " + group);
      
      // mock request
      final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Mockito.when(request.getRequestURI()).thenReturn(group);
      // mock response
      final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
      resultOutputStream = new ByteArrayOutputStream();
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(resultOutputStream));
      
      // init context
      final WroConfiguration config = new WroConfiguration();
      //
      config.setParallelPreprocessing(parallelPreprocessing);
      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);
      
      Context.get().setAggregatedFolderPath(computeAggregatedFolderPath());
      // perform processing
      getManagerFactory().create().process();
      // encode version & write result to file
      resultInputStream = new UnclosableBufferedInputStream(resultOutputStream.toByteArray());
      final File destinationFile = new File(parentFoder, rename(group, resultInputStream));
      destinationFile.createNewFile();
      // allow the same stream to be read again
      resultInputStream.reset();
      LOG.debug("Created file: {}", destinationFile.getName());
      
      final OutputStream fos = new FileOutputStream(destinationFile);
      // use reader to detect encoding
      IOUtils.copy(resultInputStream, fos);
      fos.close();
      // delete empty files
      if (destinationFile.length() == 0) {
        LOG.debug("No content found for group: {}", group);
        destinationFile.delete();
      } else {
        LOG.info("file size: {} -> {}bytes", destinationFile.getName(), destinationFile.length());
        LOG.info("{} ({}bytes) has been created!", destinationFile.getAbsolutePath(), destinationFile.length());
      }
    } finally {
      if (resultOutputStream != null) {
        resultOutputStream.close();
      }
      if (resultInputStream != null) {
        resultInputStream.close();
      }
    }
  }
  
  /**
   * This implementation is similar to the one from Wro4jMojo. TODO: reuse if possible.
   */
  private String computeAggregatedFolderPath() {
    Validate.notNull(destinationFolder, "DestinationFolder cannot be null!");
    Validate.notNull(contextFolder, "ContextFolder cannot be null!");
    final File cssTargetFolder = destinationFolder;
    File rootFolder = null;
    if (cssTargetFolder.getPath().startsWith(contextFolder.getPath())) {
      rootFolder = contextFolder;
    }
    // compute aggregatedFolderPath
    String aggregatedFolderPath = null;
    if (rootFolder != null) {
      aggregatedFolderPath = StringUtils.removeStart(cssTargetFolder.getPath(), rootFolder.getPath());
    }
    LOG.debug("aggregatedFolderPath: {}", aggregatedFolderPath);
    return aggregatedFolderPath;
  }
  
  /**
   * Encodes a version using some logic.
   * 
   * @param group
   *          the name of the resource to encode.
   * @param input
   *          the stream of the result content.
   * @return the name of the resource with the version encoded.
   */
  private String rename(final String group, final InputStream input)
      throws IOException {
    return getManagerFactory().create().getNamingStrategy().rename(group, input);
  }
  
  /**
   * This method will ensure that you have a right and initialized instance of
   * {@link StandaloneContextAwareManagerFactory}.
   */
  private StandaloneContextAwareManagerFactory getManagerFactory() {
    final DefaultStandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory();
    managerFactory.setProcessorsFactory(createProcessorsFactory());
    managerFactory.setModelFactory(createWroModelFactory());
    managerFactory.initialize(createStandaloneContext());
    //allow created manager to get injected immediately after creation
    return new InjectableContextAwareManagerFactory(managerFactory);
  }
  
  private WroModelFactory createWroModelFactory() {
    // autodetect if user didn't specify explicitly the wro file path (aka default is used).
    final boolean autoDetectWroFile = defaultWroFile.getPath().equals(wroFile.getPath());
    return new SmartWroModelFactory().setWroFile(wroFile).setAutoDetectWroFile(autoDetectWroFile);
  }
  
  private ProcessorsFactory createProcessorsFactory() {
    final Properties props = new Properties();
    if (preProcessorsList != null) {
      props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, preProcessorsList);
    }
    if (postProcessorsList != null) {
      props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, postProcessorsList);
    }
    return new ConfigurableProcessorsFactory() {
      protected Map<String, ResourcePreProcessor> newPreProcessorsMap() {
        final Map<String, ResourcePreProcessor> map = super.newPreProcessorsMap();
        // override csslint & jsHint aliases
        map.put(CssLintProcessor.ALIAS, new RunnerCssLintProcessor());
        map.put(JsHintProcessor.ALIAS, new RunnerJsHintProcessor());
        return map;
      }
      
      protected Map<String, ResourcePostProcessor> newPostProcessorsMap() {
        final Map<String, ResourcePostProcessor> map = super.newPostProcessorsMap();
        // override csslint & jsHint aliases
        map.put(CssLintProcessor.ALIAS, new RunnerCssLintProcessor());
        map.put(JsHintProcessor.ALIAS, new RunnerJsHintProcessor());
        return map;
      }
    }.setProperties(props);
  }
  
  /**
   * Creates a {@link StandaloneContext} by setting properties passed after mojo is initialized.
   */
  private StandaloneContext createStandaloneContext() {
    final StandaloneContext runContext = new StandaloneContext();
    runContext.setContextFolder(contextFolder);
    runContext.setMinimize(minimize);
    runContext.setWroFile(wroFile);
    runContext.setIgnoreMissingResources(ignoreMissingResources);
    return runContext;
  }
  
  /**
   * @param destinationFolder
   *          the destinationFolder to set
   * @VisibleForTestOnly
   */
  void setDestinationFolder(final File destinationFolder) {
    this.destinationFolder = destinationFolder;
  }
  
  /**
   * Linter classes with custom exception handling.
   */
  private class RunnerCssLintProcessor
      extends CssLintProcessor {
    @Override
    protected void onCssLintException(final CssLintException e, final Resource resource)
        throws Exception {
      super.onCssLintException(e, resource);
      System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
      System.err.println(e.getErrors());
      onRunnerException(e);
    }
  }
  
  private class RunnerJsHintProcessor
      extends JsHintProcessor {
    @Override
    protected void onLinterException(final LinterException e, final Resource resource) {
      super.onLinterException(e, resource);
      System.err.println("The following resource: " + resource + " has " + e.getErrors().size() + " errors.");
      System.err.println(e.getErrors());
      onRunnerException(e);
    }
  }
  
}
