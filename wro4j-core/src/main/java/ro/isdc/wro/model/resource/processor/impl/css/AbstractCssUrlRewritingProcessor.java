/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;


/**
 * A processor responsible for rewriting url's from inside the css resources.
 *
 * @author Alex Objelean
 * @created Created on 9 May, 2010
 */
@SupportedResourceType(ResourceType.CSS)
public abstract class AbstractCssUrlRewritingProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCssUrlRewritingProcessor.class);
  /**
   * Resources mapping path. If request uri contains this, the filter will dispatch it to the original resource.
   */
  public static final String PATH_RESOURCES = "wroResources";
  
  /**
   * The name of resource id parameter.
   */
  public static final String PARAM_RESOURCE_ID = "id";
  /**
   * Pattern used to identify the placeholders where the url rewriting will be performed.
   */
  private static final String PATTERN_PATH = "(?ims)([\\w-]*\\s*:[\\s]*url\\s*\\((['\"]?.*?['\"]?)\\)[;]?)|filter\\s*:.*?\\(src\\s*=['\"]?(.*?)['\"]?\\)[;]?";
  /**
   * Compiled pattern.
   */
  private static final Pattern PATTERN = Pattern.compile(PATTERN_PATH, Pattern.CASE_INSENSITIVE);
  @Inject
  private Context context;
  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    throw new WroRuntimeException("This processor: " + getClass().getSimpleName() + " cannot work as a postProcessor!");
  }

  /**
   * {@inheritDoc}
   */
  public final void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
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
      final int matchIndex = 0;
      final String originalExpression = matcher.group(matchIndex);
      final String urlGroup = matcher.group(matchIndex + 3) != null ? matcher.group(matchIndex + 3) : matcher.group(matchIndex + 2);
      LOG.debug("urlGroup: {}", urlGroup);

      Validate.notNull(urlGroup);
      if (isReplaceNeeded(urlGroup)) {
        final String replacedUrl = replaceImageUrl(cssUri, urlGroup);
        LOG.debug("replaced old Url: [{}] with: [{}].", urlGroup, StringUtils.abbreviate(replacedUrl, 40));
        /**
         * prevent the IllegalArgumentException because of invalid characters like $ (@see issue381) The solution is
         * from stackoverflow: @see http://stackoverflow.com/questions/947116/matcher-appendreplacement-with-literal-text 
         */
        final String modifiedExpression = Matcher.quoteReplacement(originalExpression.replace(urlGroup, replacedUrl));
        onUrlReplaced(replacedUrl);
        matcher.appendReplacement(sb, replaceExpression(originalExpression, modifiedExpression));
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * Invoked to replace the entire css expression.
   * 
   * @param originalExpression
   *          the original, unchanged expression.
   * @param modifiedExpression
   *          the changed expression.
   * @return the expression to apply. By default the modifiedExpression will be returned.
   */
  protected String replaceExpression(final String originalExpression, final String modifiedExpression) {
    return modifiedExpression;
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
    return !(UrlUriLocator.isValid(url) || DataUriGenerator.isDataUri(url.trim()));
  }
  
  /**
   * This method has protected modifier in order to be accessed by unit test class.
   * 
   * @return urlPrefix value.
   * @VisibleForTesting
   */
  protected String getUrlPrefix() {
    final String requestURI = context.getRequest().getRequestURI();
    return FilenameUtils.getFullPath(requestURI) + getProxyResourcePath();
  }
  
  /**
   * @return the part of the url used to identify a proxy resource.
   */
  private String getProxyResourcePath() {
    return String.format("%s?%s=", PATH_RESOURCES, PARAM_RESOURCE_ID);
  }
  
  /**
   * @param url
   *          of the resource to check.
   * @return true if the provided url is a proxy resource (rewritten by {@link CssUrlRewritingProcessor}.
   */
  final boolean isProxyResource(final String url) {
    Validate.notNull(url);
    return url.contains(getProxyResourcePath());
  }
}
