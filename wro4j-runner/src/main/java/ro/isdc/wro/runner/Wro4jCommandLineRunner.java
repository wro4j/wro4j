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

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
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
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.util.encoding.SmartEncodingInputStream;
import ro.isdc.wro.util.io.UnclosableBufferedInputStream;


/**
 * Default command line runner. Interprets arguments and perform a processing.
 *
 * @author Alex Objelean
 */
public class Wro4jCommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(Wro4jCommandLineRunner.class);
  @Option(name = "-m", aliases = { "--minimize" }, usage = "Turns on the minimization by applying compressor")
  private boolean minimize;
  @Option(name = "-targetGroups", metaVar = "GROUPS", usage = "Comma separated value of the group names from wro.xml to process. If none is provided, all groups will be processed.")
  private String targetGroups;
  @Option(name = "-i", aliases = { "--ignoreMissingResources" }, usage = "Ignores missing resources")
  private boolean ignoreMissingResources;
  @Option(name = "-wroFile", metaVar = "PATH_TO_WRO_XML", usage = "The path to the wro.xml. By default this is the user current folder.")
  private File wroFile = new File(System.getProperty("user.dir"), "wro.xml");
  @Option(name = "-contextFolder", metaVar = "PATH", usage = "Folder used as a root of the context relative resources. By default this is the user current folder.")
  private File contextFolder = new File(System.getProperty("user.dir"));
  @Option(name = "-destinationFolder", metaVar = "PATH", usage = "Where to store the processed result. By default uses the folder named [wro].")
  private File destinationFolder = new File(System.getProperty("user.dir"), "wro");
  @Option(name = "-c", aliases = { "--compressor" }, metaVar = "COMPRESSOR", handler = CompressorOptionHandler.class, usage = "Name of the compressor to process scripts")
  private ResourcePreProcessor compressor = new JSMinProcessor();


  public static void main(final String[] args)
    throws Exception {
    // final InputStreamReader reader = new InputStreamReader(System.in);
    // final BufferedReader in = new BufferedReader(reader);
    // final String[] inArgs = in.readLine().split(" ");
    new Wro4jCommandLineRunner().doMain(args);
  }


  /**
   * @param args
   */
  public void doMain(final String[] args) {
    final CmdLineParser parser = new CmdLineParser(this);
    parser.setUsageWidth(80);
    try {
      parser.parseArgument(args);
      LOG.debug("Options: " + this);
      process();
    } catch (final Exception e) {
      System.err.println(e.getMessage() + "\n\n");
      System.err.println("=======================================");
      System.err.println("ARGUMENTS");
      System.err.println("=======================================");
      parser.printUsage(System.err);
    }
  }


  private void process() {
    try {
      if (!wroFile.exists()) {
        throw new WroRuntimeException("No wro.xml file found at this location: " + wroFile.getAbsolutePath());
      }
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
      final WroModel model = getManagerFactory().getInstance().getModel();
      return model.getGroupNames();
    }
    return Arrays.asList(targetGroups.split(","));
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
      protected ProcessorsFactory newProcessorsFactory() {
        final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
        factory.addPreProcessor(new BomStripperPreProcessor());
        factory.addPreProcessor(new CssImportPreProcessor());
        factory.addPreProcessor(new CssUrlRewritingProcessor());
        factory.addPreProcessor(new SemicolonAppenderPreProcessor());
        factory.addPreProcessor(new JawrCssMinifierProcessor());
        factory.addPreProcessor(compressor);
        return factory;
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
    runContext.setContextFolder(contextFolder);
    runContext.setMinimize(minimize);
    runContext.setWroFile(wroFile);
    runContext.setIgnoreMissingResources(ignoreMissingResources);
    return runContext;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
