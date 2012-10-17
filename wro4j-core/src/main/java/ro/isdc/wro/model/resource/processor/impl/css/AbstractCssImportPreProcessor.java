/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

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
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ImportAware;
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
public abstract class AbstractCssImportPreProcessor
  implements ResourcePreProcessor, ImportAware {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCssImportPreProcessor.class);
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  /**
   * List of processed resources, useful for detecting deep recursion. A {@link ThreadLocal} is used to ensure that the
   * processor is thread-safe and doesn't erroneously detect recursion when running in concurrent environment.
   */
  private final ThreadLocal<List<String>> processedImports = new ThreadLocal<List<String>>() {
    @Override
    protected List<String> initialValue() {
      return new ArrayList<String>();
    };
  };
  protected static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssImport"));
  private static final String REGEX_IMPORT_FROM_COMMENTS = WroUtil.loadRegexpWithKey("cssImportFromComments");

  /**
   * {@inheritDoc}
   */
  public final void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    LOG.debug("Applying {} processor", toString());
    validate();
    try {
      final String result = parseCss(resource, IOUtils.toString(reader));
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
  }

  /**
   * @param resource {@link Resource} to process.
   * @param cssContent Reader for processed resource.
   * @return css content with all imports processed.
   */
  private String parseCss(final Resource resource, final String cssContent)
    throws IOException {
    if (getProcessedList().contains(resource.getUri())) {
      LOG.debug("[WARN] Recursive import detected: {}", resource);
      onRecursiveImportDetected();
      return "";
    }
    final String importedUri = resource.getUri().replace(File.separatorChar,'/');
    getProcessedList().add(importedUri);
    final List<Resource> importedResources = findImportedResources(resource.getUri(), cssContent);
    return doTransform(cssContent, importedResources);
  }

  /**
   * Find a set of imported resources inside a given resource.
   */
  private List<Resource> findImportedResources(final String resourceUri, final String cssContent)
    throws IOException {
    // it should be sorted
    final List<Resource> imports = new ArrayList<Resource>();
    String css = cssContent;
    //remove imports from comments before parse the file
    css = css.replaceAll(REGEX_IMPORT_FROM_COMMENTS, "");
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final Resource importedResource = createImportedResource(resourceUri, m.group(1));
      // check if already exist
      if (imports.contains(importedResource)) {
        LOG.debug("[WARN] Duplicate imported resource: {}", importedResource);
      } else {
        imports.add(importedResource);
        onImportDetected(importedResource.getUri());
      }
    }
    return imports;
  }

  /**
   * Build a {@link Resource} object from a found importedResource inside a given resource.
   */
  private Resource createImportedResource(final String resourceUri, final String importUrl) {
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

  /**
   * Perform actual transformation of provided cssContent and the list of found import resources.
   *
   * @param cssContent
   *          the css to transform.
   * @param importedResources
   *          the list of found imports.
   */
  protected abstract String doTransform(final String cssContent, final List<Resource> importedResources)
      throws IOException;


  /**
   * Invoked when an import is detected. By default this method does nothing.
   * @param foundImportUri the uri of the detected imported resource
   */
  protected void onImportDetected(final String foundImportUri) {
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
   * {@inheritDoc}
   */
  public boolean isImportAware() {
    //We want this processor to be applied when processing resources referred with @import directive
    return true;
  }
}
