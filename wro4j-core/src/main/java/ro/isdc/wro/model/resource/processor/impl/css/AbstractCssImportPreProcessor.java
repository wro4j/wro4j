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
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ImportAware;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.CssImportInspector;
import ro.isdc.wro.util.StringUtils;
import ro.isdc.wro.util.WroUtil;


/**
 * <p>CssImport Processor responsible for handling css <code>@import</code> statement. It is implemented as both:
 * preProcessor and postProcessor. It is necessary because preProcessor is responsible for updating model with found
 * imported resources, while post processor removes import occurrences.</p>
 *
 * <p>When processor finds an import which is not valid, it will check the
 * {@link WroConfiguration#isIgnoreMissingResources()} flag. If it is set to false, the processor will fail.</p>
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
   * A map useful for detecting deep recursion. The key (correlationId) - identifies a processing unit, while the value
   * contains a pair between the list o processed resources and a stack holding recursive calls (value contained on this
   * stack is not important). This map is used to ensure that the processor is thread-safe and doesn't erroneously
   * detect recursion when running in concurrent environment (when processor is invoked from within the processor for
   * child resources).
   */
  private final Map<String, Pair<List<String>, Stack<String>>> contextMap = new ConcurrentHashMap<>() {
	private static final long serialVersionUID = 1L;
	/**
     * Make sure that the get call will always return a not null object. To avoid growth of this map, it is important to
     * call remove for each invoked get.
     */
    @Override
    public Pair<List<String>, Stack<String>> get(final Object key) {
      Pair<List<String>, Stack<String>> result = super.get(key);
      if (result == null) {
        final List<String> list = new ArrayList<String>();
        result = ImmutablePair.of(list, new Stack<String>());
        put(key.toString(), result);
      }
      return result;
    };
  };


  /**
   * Useful to check that there is no memory leak after processing completion.
   * 
   * @return The context map.
   */
  protected final Map<String, Pair<List<String>, Stack<String>>> getContextMap() {
    return contextMap;
  }

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
    } finally {
      //imkportant to avoid memory leak
      clearProcessedImports();
      reader.close();
      writer.close();
    }
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
    if (isImportProcessed(resource.getUri())) {
      LOG.debug("[WARN] Recursive import detected: {}", resource);
      onRecursiveImportDetected();
      return "";
    }
    final String importedUri = resource.getUri().replace(File.separatorChar,'/');
    addProcessedImport(importedUri);
    final List<Resource> importedResources = findImportedResources(resource.getUri(), cssContent);
    return doTransform(cssContent, importedResources);
  }

  private boolean isImportProcessed(final String uri) {
    return getProcessedImports().contains(uri);
  }

  private void addProcessedImport(final String importedUri) {
    final String correlationId = Context.getCorrelationId();
    contextMap.get(correlationId).getValue().push(importedUri);
    getProcessedImports().add(importedUri);
  }

  private List<String> getProcessedImports() {
    return contextMap.get(Context.getCorrelationId()).getKey();
  }

  private void clearProcessedImports() {
    final String correlationId = Context.getCorrelationId();
    final Stack<String> stack = contextMap.get(correlationId).getValue();
    if (!stack.isEmpty()) {
      stack.pop();
    }
    if (stack.isEmpty()) {
      contextMap.remove(correlationId);
    }
  }

  /**
   * Find a set of imported resources inside a given resource.
   */
  private List<Resource> findImportedResources(final String resourceUri, final String cssContent)
    throws IOException {
    // it should be sorted
    final List<Resource> imports = new ArrayList<Resource>();
    final String css = cssContent;
    final List<String> foundImports = findImports(css);
    for (final String importUrl : foundImports) {
      final Resource importedResource = createImportedResource(resourceUri, importUrl);
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
   * Extracts a list of imports from css content.
   *
   * @param css The CSS file in which the imports should be looked up.
   * @return a list of found imports.
   */
  protected List<String> findImports(final String css) {
    return new CssImportInspector(css).findImports();
  }

  /**
   * Build a {@link Resource} object from a found importedResource inside a given resource.
   */
  private Resource createImportedResource(final String resourceUri, final String importUrl) {
    final String absoluteUrl = uriLocatorFactory.getInstance(importUrl) != null ? importUrl
        : computeAbsoluteUrl(resourceUri, importUrl);
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
    final String folder = WroUtil.getFullPath(relativeResourceUri);
    // remove '../' & normalize the path.
    return StringUtils.cleanPath(folder + importUrl);
  }

  /**
   * Perform actual transformation of provided cssContent and the list of found import resources.
   *
   * @param cssContent
   *          the css to transform.
   * @param importedResources
   *          the list of found imports.
   * @return A string containing the transformed CSS content.
   * @throws IOException when the transformation cannot be done.
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
