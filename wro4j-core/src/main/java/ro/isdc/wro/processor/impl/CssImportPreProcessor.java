/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
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
    final Stack<Resource> stack = new Stack<Resource>();
    //final LinkedList<String> importsList = new LinkedList<String>();
    final LinkedList<Resource> resourcesList = new LinkedList<Resource>();
    final String result = parseImports(resource, reader, stack, resourcesList);
    System.out.println(result);
    System.out.println(resourcesList);
    return result;
  }

  /**
   * @param resource
   * @param reader
   * @param stack
   * @param resourcesList
   * @return
   * @throws IOException
   */
  private String parseImports(final Resource resource, final Reader reader,
    final Stack<Resource> stack, final LinkedList<Resource> resourcesList)
    throws IOException {
    final StringBuffer sb = new StringBuffer();
    //Check if @Scanner#findWithinHorizon can be used instead
    final String css = IOUtils.toString(reader);
    final Matcher m = PATTERN.matcher(css);
    while (m.find()) {
      final String importUrl = m.group(1);
      LOG.debug("iterate importUrl: " + importUrl);
      final String absoluteImportUrl = computeAbsoluteUrl(resource, importUrl);
      final UriLocator uriLocator = uriLocatorFactory.getInstance(absoluteImportUrl);
      //LOG.debug("content of located import resource: " + IOUtils.toString(uriLocator.locate(absoluteImportUrl)));
      //importsList.add(absoluteImportUrl);
      final Resource importResource = Resource.create(absoluteImportUrl, ResourceType.CSS);
      stack.push(importResource);
      LOG.debug("<parseImports>");
      LOG.debug("\tresource: " + resource);
      //Pass correct Reader instead of reader of original resource.
      parseImports(importResource, reader, stack, resourcesList);
      LOG.debug("</parseImports>");
      resourcesList.add(stack.pop());
      //LOG.debug("import statement: " + m.group(0));
      //LOG.debug("import url: " + importUrl);
      m.appendReplacement(sb, "");
    }
    m.appendTail(sb);
    final String result = sb.toString();
    return result;
  }

  /**
   * Computes absolute url of the imported resource.
   * @param relativeResource {@link Resource} where the import statement is found.
   * @param importUrl found import url.
   * @return absolute url of the resource to import.
   */
  private String computeAbsoluteUrl(final Resource relativeResource, final String importUrl) {
    final String folder = WroUtil.getFolderOfUri(relativeResource.getUri());
    final String absoluteImportUrl = folder + importUrl;
    return absoluteImportUrl;
  }
}
