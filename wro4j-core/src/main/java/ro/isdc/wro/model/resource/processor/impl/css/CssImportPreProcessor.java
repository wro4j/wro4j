/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.PreProcessorExecutor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StringUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * CssImport Processor responsible for handling css <code>@import</code> statement. It is implemented as both:
 * preProcessor & postProcessor. It is necessary because preProcessor is responsible for updating model with found
 * imported resources, while post processor removes import occurrences.
 * <p/>
 * When processor finds an import which is not valid, it will check the
 * {@link WroConfiguration#isIgnoreMissingResources()} flag. If it is set to false, the processor will fail.
 * 
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public class CssImportPreProcessor
  implements ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssImportPreProcessor.class);
  public static final String ALIAS = "cssImport";
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  @Inject
  private PreProcessorExecutor preProcessorExecutor;
  @Inject
  private WroConfiguration configuration;
  /**
   * List of processed resources, useful for detecting deep recursion. A {@link ThreadLocal} is used to ensure that the
   * processor is thread-safe and doesn't eroneously detect recursion when running in concurrent environment.
   */
  private final ThreadLocal<List<String>> processedImports = new ThreadLocal<List<String>>() {
    @Override
    protected List<String> initialValue() {
      return new ArrayList<String>();
    };
  }; 
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssImport"));
  private static final String REGEX_IMPORT_FROM_COMMENTS = WroUtil.loadRegexpWithKey("cssImportFromComments");
  
  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    LOG.debug("Applying {} processor", CssImportPreProcessor.this.getClass().getSimpleName());
    validate();
    try {
      final String result = parseCss(resource, reader);
      writer.write(result);
      getProcessedList().clear();
    } finally {
      reader.close();
      writer.close();
    }
  }

  private List<String> getProcessedList() {
    return processedImports.get();
  }

  /**
   * Checks if required fields were injected.
   */
  private void validate() {
    Validate.notNull(uriLocatorFactory);
    Validate.notNull(preProcessorExecutor);
  }

  /**
   * @param resource {@link Resource} to process.
   * @param reader Reader for processed resource.
   * @return css content with all imports processed.
   */
  private String parseCss(final Resource resource, final Reader reader)
    throws IOException {
    if (getProcessedList().contains(resource.getUri())) {
      LOG.debug("[WARN] Recursive import detected: {}", resource);
      onRecursiveImportDetected();
      return "";
    }
    getProcessedList().add(resource.getUri().replace(File.separatorChar,'/'));
    final StringBuffer sb = new StringBuffer();
    final List<Resource> importsCollector = getImportedResources(resource);
    // for now, minimize always
    // TODO: find a way to get minimize property dynamically.
    //groupExtractor.isMinimized(Context.get().getRequest())
    sb.append(preProcessorExecutor.processAndMerge(importsCollector, true));
    if (!importsCollector.isEmpty()) {
      LOG.debug("Imported resources found : {}", importsCollector.size());
    }
    sb.append(IOUtils.toString(reader));
    LOG.debug("importsCollector: {}", importsCollector);
    return removeImportStatements(sb.toString());
  }

  /**
   * Invoked when a recursive import is detected. Used to assert the recursive import detection correct behavior. By
   * default this method does nothing.
   * 
   * @VisibleForTesting
   */
  protected void onRecursiveImportDetected() {
  }

  /**
   * Removes all @import statements for css.
   */
  private String removeImportStatements(final String content) {
    final Matcher m = PATTERN.matcher(content);
    final StringBuffer sb = new StringBuffer();
    while (m.find()) {
      // add and check if already exist
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Find a set of imported resources inside a given resource.
   */
  private List<Resource> getImportedResources(final Resource resource)
    throws IOException {
    // it should be sorted
    final List<Resource> imports = new ArrayList<Resource>();
    String css = EMPTY;
    try {
      css = IOUtils.toString(new AutoCloseInputStream(uriLocatorFactory.locate(resource.getUri())),
          configuration.getEncoding());
    } catch (IOException e) {
      if (!configuration.isIgnoreMissingResources()) {
        LOG.error("Invalid import detected: {}", resource.getUri());
        throw e;
      }
    }
    //remove imports from comments before parse the file
    css = css.replaceAll(REGEX_IMPORT_FROM_COMMENTS, "");
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final Resource importedResource = buildImportedResource(resource.getUri(), m.group(1));
      // check if already exist
      if (imports.contains(importedResource)) {
        LOG.debug("[WARN] Duplicate imported resource: {}", importedResource);
      } else {
        imports.add(importedResource);
      }
    }
    return imports;
  }


  /**
   * Build a {@link Resource} object from a found importedResource inside a given resource.
   */
  private Resource buildImportedResource(final String resourceUri, final String importUrl) {
    final String absoluteUrl = computeAbsoluteUrl(resourceUri, importUrl);
    return Resource.create(absoluteUrl, ResourceType.CSS);
  }


  /**
   * Computes absolute url of the imported resource.
   *
   * @param relativeResourceUri uri of the resource containing the import statement.
   * @param importUrl found import url.
   * @return absolute url of the resource to import.
   */
  private String computeAbsoluteUrl(final String relativeResourceUri, final String importUrl) {
    final String folder = FilenameUtils.getFullPath(relativeResourceUri);
    // remove '../' & normalize the path.
    final String absoluteImportUrl = StringUtils.cleanPath(folder + importUrl);
    return absoluteImportUrl;
  }
}
