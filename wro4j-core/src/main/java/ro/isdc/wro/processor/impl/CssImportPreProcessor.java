/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.annot.Inject;
import ro.isdc.wro.annot.SupportedResourceType;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;
import ro.isdc.wro.util.WroUtil;


/**
 * Css preProcessor responsible for handling css @import statement.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(type=ResourceType.CSS)
public class CssImportPreProcessor
  implements ResourcePreProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CssImportPreProcessor.class);
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  /** The url pattern */
  private static final Pattern PATTERN = Pattern.compile("@import\\s*url\\(\\s*"
    + "[\"']?([^\"']*)[\"']?" // any sequence of characters, except an unescaped ')'
    + "\\s*\\);?", // Any number of whitespaces, then ')'
    Pattern.CASE_INSENSITIVE); // works with 'URL('

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String result = parseCss(resource, reader);
    writer.write(result);
    writer.close();
  }

  /**
   * Parse css, find all import statements.
   *
   * @param resource {@link Resource} where the parsed css resides.
   */
  private String parseCss(final Resource resource, final Reader reader) throws IOException {
    final List<Resource> resourcesList = new LinkedList<Resource>();
    final String result = parseImports(resource, reader, new Stack<Resource>(), resourcesList);
    //prepend entire list of resources
    for (final Resource importedResource : resourcesList) {
      resource.prepend(importedResource);
    }
    LOG.debug("" + resourcesList);
    return result;
  }


  /**
   * TODO update javadoc
   */
  private String parseImports(final Resource resource, final Reader reader,
    final Stack<Resource> stack, final List<Resource> resourcesList)
    throws IOException {
    //check recursivity
    //TODO find a correct way for handling recursivity
    if (resourcesList.contains(resource)) {
      LOG.warn("!!!RECURSIVITY : " + resource);
      if (resourcesList.contains(resource)) {
        LOG.warn("IN LIST");
      } else {
        LOG.warn("IN STACK");
      }
      return null;
    }
    final Set<Resource> importedResources = getImportedResources(resource, reader);
    final StringBuffer sb = new StringBuffer();
    for (final Resource imported : importedResources) {
      if (resource.equals(imported)) {
        LOG.warn("Recursivity detected for resource: " + resource);
      } else {
        stack.push(imported);
        try {
          final Reader importReader = getReaderForResource(imported);
          parseImports(imported, importReader, stack, resourcesList);
          final Resource processedImport = stack.pop();
          resourcesList.add(processedImport);
        } catch (final IOException e) {
          LOG.warn("Invalid imported resource: " + imported + " located in: " + resource);
        }
      }
    }
    //LOG.debug("Appending stream: " + IOUtils.toString(getReaderForResource(resource)) + " for resource: " + resource);
    sb.append(IOUtils.toString(getReaderForResource(resource)));
    return sb.toString();
  }

  //we use this because a reader cannot be used twice for reading
  private Reader getReaderForResource(final Resource imported)
    throws IOException {
    final UriLocator uriLocator = uriLocatorFactory.getInstance(imported.getUri());
    final Reader importReader = new InputStreamReader(uriLocator.locate(imported.getUri()));
    return importReader;
  }

  private Set<Resource> getImportedResources(final Resource resource, final Reader reader) throws IOException {
    final Set<Resource> importSet = new HashSet<Resource>();
    //Check if @Scanner#findWithinHorizon can be used instead
    final String css = IOUtils.toString(reader);
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final Resource importedResource = buildImportedResource(resource, m);
      //add and check if already exist
      final boolean alreadyExist = !importSet.add(importedResource);
      if (alreadyExist) {
        LOG.warn("Duplicate imported resource: " + importedResource);
      }
    }
    return importSet;
  }

  /**
   * TODO update javadoc
   */
  private String parseImports1(final Resource resource, final Reader reader,
    final Stack<Resource> stack, final List<Resource> resourcesList)
    throws IOException {
    //check recursivity
    //TODO find a correct way for handling recursivity
    if (stack.contains(resource)) {
      LOG.warn("!!!RECURSIVITY : " + resource);
      if (resourcesList.contains(resource)) {
        LOG.warn("IN LIST");
      } else {
        LOG.warn("IN STACK");
      }
      return null;
    }
    final StringBuffer sb = new StringBuffer();
    //Check if @Scanner#findWithinHorizon can be used instead
    final String css = IOUtils.toString(reader);
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final String importUrl = m.group(1);
      final String absoluteImportUrl = computeAbsoluteUrl(resource, importUrl);
      final UriLocator uriLocator = uriLocatorFactory.getInstance(absoluteImportUrl);
      final Resource importResource = Resource.create(absoluteImportUrl, ResourceType.CSS);
      LOG.debug("@IMPORT " + importResource);
      if (resource.equals(importResource)) {
        LOG.warn("Recursivity detected for resource: " + resource);
      } else {
        stack.push(importResource);

        try {
          final Reader importReader = new InputStreamReader(uriLocator.locate(importResource.getUri()));
          parseImports(importResource, importReader, stack, resourcesList);
          resourcesList.add(stack.pop());
        } catch (final IOException e) {
          LOG.warn("Invalid imported resource: " + importResource + " located in: " + resource);
        }
      }
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    final String result = sb.toString();
    return result;
  }

  private Resource buildImportedResource(final Resource resource, final Matcher m) {
    final String importUrl = m.group(1);
    final String absoluteImportUrl = computeAbsoluteUrl(resource, importUrl);
    final Resource importResource = Resource.create(absoluteImportUrl, ResourceType.CSS);
    return importResource;
  }
	/**
	 * Computes absolute url of the imported resource.
	 *
	 * @param relativeResource {@link Resource} where the import statement is found.
	 * @param importUrl found import url.
	 * @return absolute url of the resource to import.
	 */
  private String computeAbsoluteUrl(final Resource relativeResource, final String importUrl) {
    final String folder = WroUtil.getFolderOfUri(relativeResource.getUri());
    //normalize it
    final String absoluteImportUrl = WroUtil.normalizePath(folder + importUrl);
    return absoluteImportUrl;
  }
}
