/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.algorithm.DataUriGenerator;


/**
 * Rewrites background images by replacing the url with data uri of the image. If the replacement is not successful, it
 * is left unchanged.
 *
 * @author Alex Objelean
 * @created 9 may, 2010
 */
@SupportedResourceType(ResourceType.CSS)
public class CssEmbedPreProcessor
  implements ResourcePreProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CssEmbedPreProcessor.class);
  /**
   * Compiled pattern.
   */
  private static final Pattern PATTERN = Pattern.compile(CssUrlRewritingProcessor.PATTERN_PATH,
    Pattern.CASE_INSENSITIVE);
  private DataUriGenerator dataUriGenerator;
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Default constructor.
   */
  public CssEmbedPreProcessor() {
    dataUriGenerator = new DataUriGenerator();
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      LOG.debug("<process>");
      final String cssUri = resource.getUri();
      LOG.debug("\t<cssUri>" + cssUri + "</cssUri>");
      final String css = IOUtils.toString(reader);
      final String result = parseCss(css, cssUri);
      writer.write(result);
      writer.close();
      LOG.debug("</process>");
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * Perform actual css parsing logic.
   *
   * @param cssContent to parse.
   * @param cssUri Uri of the css to parse.
   * @return parsed css.
   */
  private String parseCss(final String cssContent, final String cssUri) {
    final Matcher m = PATTERN.matcher(cssContent);
    final StringBuffer sb = new StringBuffer();
    while (m.find()) {
      final String oldMatch = m.group();
      final String urlGroup = m.group(1) != null ? m.group(1) : m.group(2);
      if (urlGroup == null) {
        throw new IllegalStateException("Could not extract urlGroup from: " + oldMatch);
      }
      final String replacedUrl = replaceImageUrl(cssUri, urlGroup);
      LOG.debug("replacedImageUrl: " + replacedUrl);
      final String newReplacement = oldMatch.replace(urlGroup, replacedUrl);
      // update allowedUrls list
      // TODO no need to hold absolute url's inside
      m.appendReplacement(sb, newReplacement);
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Replace provided url with the new url if needed.
   *
   * @param imageUrl to replace.
   * @param cssUri Uri of the parsed css.
   * @return replaced url.
   */
  private String replaceImageUrl(final String cssUri, final String imageUrl) {
    LOG.debug("replace url for image: " + imageUrl + ", from css: " + cssUri);
    final String cleanImageUrl = cleanImageUrl(imageUrl);
    final String fileName = FilenameUtils.getName(imageUrl);
    final String fullPath = FilenameUtils.getFullPath(cssUri) + cleanImageUrl;
    String result = imageUrl;
    try {
      final UriLocator uriLocator = uriLocatorFactory.getInstance(fullPath);
      result = dataUriGenerator.generateDataURI(uriLocator.locate(fullPath), fileName);
    } catch (final IOException e) {
      LOG.warn("Couldn't extract dataUri from:" + fullPath + ", because: " + e.getMessage());
    }
    return result;
  }

  /**
   * TODO - duplicated method in {@link CssUrlRewritingProcessor}, get rid of duplication.
   * Cleans the image url by triming result and removing \' or \" characters if such exists.
   *
   * @param imageUrl to clean.
   * @return cleaned image URL.
   */
  private String cleanImageUrl(final String imageUrl) {
    final String result = imageUrl.replace('\'', ' ').replace('\"', ' ').trim();
    return result.toString();
  }
}
