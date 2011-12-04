/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.support.UrlResourceLocator;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;


/**
 * A processor responsible for rewriting url's from inside the css resources.
 *
 * @author Alex Objelean
 * @created Created on 9 May, 2010
 */
@SupportedResourceType(ResourceType.CSS)
public abstract class AbstractCssUrlRewritingProcessor
  implements ResourceProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCssUrlRewritingProcessor.class);

  /**
   * Pattern used to identify the placeholders where the url rewriting will be performed.
   */
  private static final String PATTERN_PATH = "url\\s*\\((\\s*['\"]?((?:.*?|\\s*?))['\"]?\\s*)\\)|src\\s*=\\s*['\"]((?:.|\\s)*?)['\"]";
  /**
   * Compiled pattern.
   */
  protected static final Pattern PATTERN = Pattern.compile(PATTERN_PATH, Pattern.CASE_INSENSITIVE);

  /**
   * {@inheritDoc}
   */
  public final void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    Validate.notNull(resource, "Resource cannot be null! Probably you are using this processor as a Post-Processor and it is intended to be used as a Pre-Processor only!");
    try {
      final String cssUri = resource.getUri();
      LOG.debug("cssUri: {}", cssUri);
      final String css = IOUtils.toString(reader);
      final String result = parseCss(css, cssUri);
      writer.write(result);
      onProcessCompleted();
    } finally {
      reader.close();
      writer.close();
    }
  }


  /**
   * Invoked when the process operation is completed. Useful to invoke some post processing logic or for custom logging.
   */
  protected void onProcessCompleted() {
  }


  /**
   * Perform actual css parsing logic.
   *
   * @param cssContent to parse.
   * @param cssUri Uri of the css to parse.
   * @return parsed css.
   */
  private String parseCss(final String cssContent, final String cssUri) {
    final Matcher matcher = PATTERN.matcher(cssContent);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      final String oldMatch = matcher.group();

      final String urlGroup = matcher.group(3) != null ? matcher.group(3) : matcher.group(2);
      LOG.debug("urlGroup: {}", urlGroup);
      //use urlContent to get rid of trailing spaces inside the url() construction
      final String urlContent = matcher.group(1) != null ? matcher.group(1) : urlGroup;

      Validate.notNull(urlGroup);
      if (isReplaceNeeded(urlGroup)) {
        final String replacedUrl = replaceImageUrl(cssUri, urlGroup);
        LOG.debug("replaced old Url: [{}] with: [{}].", urlContent, StringUtils.abbreviate(replacedUrl, 40));
        final String newReplacement = oldMatch.replace(urlContent, replacedUrl);
        onUrlReplaced(replacedUrl);
        matcher.appendReplacement(sb, newReplacement);
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }


  /**
   * Invoked when an url is replaced. Useful if you need to do something will replacements.
   * @param replacedUrl the newly computed url created as a result of url rewriting.
   */
  protected void onUrlReplaced(final String replacedUrl) {}


  /**
   * Replace provided url with the new url if needed.
   *
   * @param cssUri Uri of the parsed css.
   * @param imageUrl to replace.
   * @return replaced url.
   */
  protected abstract String replaceImageUrl(final String cssUri, final String imageUrl);

  /**
   * Cleans the image url by triming result and removing \' or \" characters if such exists.
   *
   * @param imageUrl to clean.
   * @return cleaned image URL.
   */
  protected final String cleanImageUrl(final String imageUrl) {
    return imageUrl.replace('\'', ' ').replace('\"', ' ').trim();
  }


  /**
   * Check if url must be replaced or not.
   *
   * @param url to check.
   * @return true if url needs to be replaced or remain unchanged.
   */
  protected boolean isReplaceNeeded(final String url) {
    // The replacement is not needed if the url of the image is absolute (can be
    // resolved by urlResourceLocator) or if the url is a data uri (base64 encoded value).
    return !(DataUriGenerator.isDataUri(url.trim()) || UrlResourceLocator.isValid(url));
  }
}
