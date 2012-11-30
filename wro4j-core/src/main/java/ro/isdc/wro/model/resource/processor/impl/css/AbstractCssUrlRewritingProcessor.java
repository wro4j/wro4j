/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ImportAware;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;
import ro.isdc.wro.util.WroUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ro.isdc.wro.http.handler.ResourceProxyRequestHandler.PARAM_RESOURCE_ID;
import static ro.isdc.wro.http.handler.ResourceProxyRequestHandler.PATH_RESOURCES;


/**
 * A processor responsible for rewriting url's from inside the css resources.
 *
 * @author Alex Objelean
 * @created Created on 9 May, 2010
 */
@SupportedResourceType(ResourceType.CSS)
public abstract class AbstractCssUrlRewritingProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, ImportAware {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCssUrlRewritingProcessor.class);
  /**
   * Compiled pattern.
   */
  private final Pattern PATTERN;

  public AbstractCssUrlRewritingProcessor() {
      PATTERN = Pattern.compile(getPattern());
  }

  @Inject
  private ReadOnlyContext context;
  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    throw new WroRuntimeException("This processor: " + getClass().getSimpleName() + " cannot work as a postProcessor!");
  }

  /**
   * Parse the css content and transform found url's.
   *
   * @param cssContent
   *          to parse.
   * @param cssUri
   *          Uri of the css to parse.
   * @return parsed css.
   */
  private String parseCss(final String cssContent, final String cssUri) {
    final Matcher matcher = PATTERN.matcher(cssContent);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      int urlIndexA = getUrlIndexA();
      int urlIndexB = getUrlIndexB();

      String originalDeclaration = matcher.group(getDeclarationIndex());
      String groupA = matcher.group(urlIndexA);
      String originalUrl = groupA != null ? groupA : matcher.group(urlIndexB);
      LOG.debug("urlGroup: {}", originalUrl);

      Validate.notNull(originalUrl);
      if (isReplaceNeeded(originalUrl)) {
        String modifiedUrl = replaceImageUrl(cssUri.trim(), cleanImageUrl(originalUrl));
        LOG.debug("replaced old Url: [{}] with: [{}].", originalUrl, modifiedUrl);
        /**
         * prevent the IllegalArgumentException because of invalid characters like $ (@see issue381) The solution is
         * from stackoverflow: @see
         * http://stackoverflow.com/questions/947116/matcher-appendreplacement-with-literal-text
         */
        String modifiedDeclaration = Matcher.quoteReplacement(originalDeclaration.replace(originalUrl,
            modifiedUrl));
        onUrlReplaced(modifiedUrl);
        matcher.appendReplacement(sb, replaceDeclaration(originalDeclaration.trim(), modifiedDeclaration));
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   */
  public final void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    LOG.debug("Applying {} processor", getClass().getSimpleName());
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
   * Invoked to replace the entire css declaration.
   * <p/>
   * An example of css declaration:
   * <pre>
   * background: url(/image.png);
   * </pre>
   * Useful when the css declaration should be changed. The use-case is:
   * {@link FallbackCssDataUriProcessor}.
   *
   * @param originalDeclaration
   *          the original, unchanged declaration.
   * @param modifiedDeclaration
   *          the changed expression.
   * @return the expression to apply. By default the modifiedExpression will be returned.
   */
  protected String replaceDeclaration(final String originalDeclaration, final String modifiedDeclaration) {
    return modifiedDeclaration;
  }

  /**
   * Invoked when an url is replaced. Useful if you need to do something with newly replaced url.
   *
   * @param replacedUrl
   *          the newly computed url created as a result of url rewriting.
   */
  protected void onUrlReplaced(final String replacedUrl) {
  }

  /**
   * Replace provided url with the new url if needed.
   *
   * @param cssUri
   *          Uri of the parsed css.
   * @param imageUrl
   *          to replace.
   * @return replaced url.
   */
  protected abstract String replaceImageUrl(final String cssUri, final String imageUrl);

  /**
   * Cleans the image url by triming result and removing \' or \" characters if such exists.
   *
   * @param imageUrl
   *          to clean.
   * @return cleaned image URL.
   */
  protected final String cleanImageUrl(final String imageUrl) {
    return imageUrl.replace('\'', ' ').replace('\"', ' ').trim();
  }

  /**
   * Check if url must be replaced or not.
   *
   * @param url
   *          to check.
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

  /**
   * {@inheritDoc}
   */
  public boolean isImportAware() {
    //We want this processor to be applied when processing resources referred with @import directive
    return true;
  }


  protected String getPattern() {
      return WroUtil.loadRegexpWithKey(getRegexPatternKey());
  }

  protected String getRegexPatternKey() {
      return "cssUrlRewrite";
  }

 /**
  *
  * @return index of the group containing entire declaration (Ex: background: url(/path/to/image.png);)
  */
  protected int getDeclarationIndex() {
      return 0;
  }

  /**
   * index of the group containing an url inside a declaration of this form:
   *
   * <pre>
   * body {
   *   filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='../images/tabs/tabContent.png', sizingMethod='scale' );
   * }
   * </pre>
   * or
   * <pre>
   * @font-face {
   *   src: url(btn_icons.png);
   * }
   */
   protected int getUrlIndexA() {
       return 1;
   }

   /**
   * index of the group containing an url inside a declaration of this form:
   *
   * <pre>
   * body {
   *     background: #B3B3B3 url(img.gif);color:red;
   * }
   * </pre>
   * </pre>
   */
   protected int getUrlIndexB() {
      return 2;
   }
}
