/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.assembly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.WroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.DefaultStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.manager.factory.standalone.StandaloneContextAwareManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.processor.GroupsProcessor;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.util.encoding.SmartEncodingInputStream;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;

import com.google.common.base.Preconditions;


/**
 * @author Alex Objelean
 */
public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);
  public static class CompressorOptionHandler extends OptionHandler<ResourcePreProcessor> {
    public CompressorOptionHandler(final CmdLineParser parser, final OptionDef option,
      final Setter<? super ResourcePreProcessor> setter) {
      super(parser, option, setter);
    }


    @Override
    public String getDefaultMetaVariable() {
      return null;
    }


    @Override
    public int parseArguments(final Parameters params)
      throws CmdLineException {
      if (option.isArgument()) {
        final String valueStr = params.getParameter(0).toLowerCase();
        System.out.println("parse value: " + valueStr);
      }
      setter.addValue(new JSMinProcessor());
      return 0;
    }

  }

  public static class Options {
    @Option(name = "-minimize", handler = BooleanOptionHandler.class, usage = "Turns minimization on or off")
    private boolean minimize = true;
    @Option(name = "-targetGroups")
    private String targetGroups;
    @Option(name = "-ignoreMissingResources", usage = "If false, processing will not continue if there is at least one missing resource")
    private boolean ignoreMissingResources = true;
    @Option(name = "-wroFile")
    private File wroFile = new File(System.getProperty("user.dir"), "wro.xml");
    @Option(name = "-contextFolder")
    private File contextFolder = new File(System.getProperty("user.dir"));
    @Option(name = "-destinationFolder")
    private File destinationFolder = new File(System.getProperty("user.dir"), "wro");
    @Option(name = "-compressor", handler = CompressorOptionHandler.class)
    private ResourcePreProcessor compressor;

    /**
     * @return the compressor
     */
    public ResourcePreProcessor getCompressor() {
      return this.compressor;
    }


    /**
     * @param compressor the compressor to set
     */
    public void setCompressor(final ResourcePreProcessor compressor) {
      this.compressor = compressor;
    }


    /**
     * @return the destinationFolder
     */
    public File getDestinationFolder() {
      return this.destinationFolder;
    }


    /**
     * @param destinationFolder the destinationFolder to set
     */
    public void setDestinationFolder(final File destinationFolder) {
      this.destinationFolder = destinationFolder;
    }


    /**
     * @return the contextFolder
     */
    public File getContextFolder() {
      return this.contextFolder;
    }


    /**
     * @param contextFolder the contextFolder to set
     */
    public void setContextFolder(final File contextFolder) {
      this.contextFolder = contextFolder;
    }


    /**
     * @return the ignoreMissingResources
     */
    public boolean isIgnoreMissingResources() {
      return this.ignoreMissingResources;
    }


    /**
     * @param ignoreMissingResources the ignoreMissingResources to set
     */
    public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
      this.ignoreMissingResources = ignoreMissingResources;
    }


    /**
     * @return the wroFile
     */
    public File getWroFile() {
      return this.wroFile;
    }


    /**
     * @param wroFile the wroFile to set
     */
    public void setWroFile(final File wroFile) {
      this.wroFile = wroFile;
    }


    /**
     * @return the minimize
     */
    public boolean isMinimize() {
      return this.minimize;
    }


    /**
     * @param minimize the minimize to set
     */
    public void setMinimize(final boolean minimize) {
      this.minimize = minimize;
    }


    /**
     * @return the targetGroups
     */
    public String getTargetGroups() {
      return this.targetGroups;
    }


    /**
     * @param targetGroups the targetGroups to set
     */
    public void setTargetGroups(final String targetGroups) {
      this.targetGroups = targetGroups;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
  }


  public static void main(final String[] args)
    throws Exception {
    final Options options = new Options();
    final CmdLineParser parser = new CmdLineParser(options);
    try {
      parser.parseArgument(args);
      System.out.println("Options: " + options);
      new Main(options).process();
    } catch (final CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      return;
    }
  }

  private Options options;
  private File cssDestinationFolder;
  private File jsDestinationFolder;


  public Main(final Options options) {
    Preconditions.checkNotNull(options);
    this.options = options;
  }


  public void process() {
    try {
      if (!options.getWroFile().exists()) {
        throw new WroRuntimeException("No wro.xml file found at this location: "
          + options.getWroFile().getAbsolutePath());
      }
      Context.set(Context.standaloneContext());
      final Collection<String> groupsAsList = getTargetGroupsAsList();
      for (final String group : groupsAsList) {
        for (final ResourceType resourceType : ResourceType.values()) {
          final File destinationFolder = computeDestinationFolder(resourceType);
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
    if (options.getTargetGroups() == null) {
      final WroModel model = getManagerFactory().getInstance().getModel();
      return model.getGroupNames();
    }
    return Arrays.asList(options.getTargetGroups().split(","));
  }


  /**
   * Computes the destination folder based on resource type.
   *
   * @param resourceType {@link ResourceType} to process.
   * @return destinationFoder where the result of resourceType will be copied.
   * @throws MojoExecutionException if computed folder is null.
   */
  private File computeDestinationFolder(final ResourceType resourceType) {
    File folder = options.getDestinationFolder();
    if (resourceType == ResourceType.JS) {
      if (jsDestinationFolder != null) {
        folder = jsDestinationFolder;
      }
    }
    if (resourceType == ResourceType.CSS) {
      if (cssDestinationFolder != null) {
        folder = cssDestinationFolder;
      }
    }
    LOG.info("folder: " + folder);
    if (folder == null) {
      throw new RuntimeException("Couldn't compute destination folder for resourceType: " + resourceType
        + ". That means that you didn't define one of the following parameters: "
        + "destinationFolder, cssDestinationFolder, jsDestinationFolder");
    }
    if (!folder.exists()) {
      folder.mkdirs();
    }
    return folder;
  }


  /**
   * Process a single group.
   *
   * @throws IOException if any IO related exception occurs.
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
      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)));
      // perform processing
      getManagerFactory().getInstance().process();
      // encode version & write result to file
      resultInputStream = new UnclosableBufferedInputStream(resultOutputStream.toByteArray());
      final File destinationFile = new File(parentFoder, rename(group, resultInputStream));
      destinationFile.createNewFile();
      // allow the same stream to be read again
      resultInputStream.reset();
      LOG.debug("Created file: " + destinationFile.getName());

      final OutputStream fos = new FileOutputStream(destinationFile);
      // use reader to detect encoding
      IOUtils.copy(new SmartEncodingInputStream(resultInputStream), fos);
      fos.close();
      LOG.info("file size: " + destinationFile.getName() + " -> " + destinationFile.length() + " bytes");
      // delete empty files
      if (destinationFile.length() == 0) {
        LOG.info("No content found for group: " + group);
        destinationFile.delete();
      } else {
        LOG.info(destinationFile.getAbsolutePath() + " (" + destinationFile.length() + "bytes" + ") has been created!");
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
   * Encodes a version using some logic.
   *
   * @param group the name of the resource to encode.
   * @param input the stream of the result content.
   * @return the name of the resource with the version encoded.
   */
  private String rename(final String group, final InputStream input)
    throws IOException {
    return getManagerFactory().getNamingStrategy().rename(group, input);
  }


  /**
   * This method will ensure that you have a right and initialized instance of
   * {@link StandaloneContextAwareManagerFactory}.
   *
   * @return {@link WroManagerFactory} implementation.
   */
  private StandaloneContextAwareManagerFactory getManagerFactory() {
    final StandaloneContextAwareManagerFactory managerFactory = new DefaultStandaloneContextAwareManagerFactory() {
      @Override
      protected void configureProcessors(final GroupsProcessor groupsProcessor) {
        groupsProcessor.addPreProcessor(options.getCompressor());
      }
    };
    // initialize before process.
    managerFactory.initialize(createStandaloneContext());
    return managerFactory;
  }


  /**
   * Creates a {@link StandaloneContext} by setting properties passed after mojo is initialized.
   */
  private StandaloneContext createStandaloneContext() {
    final StandaloneContext runContext = new StandaloneContext();
    runContext.setContextFolder(options.getContextFolder());
    runContext.setMinimize(options.isMinimize());
    runContext.setWroFile(options.getWroFile());
    runContext.setIgnoreMissingResources(options.isIgnoreMissingResources());
    return runContext;
  }
}
