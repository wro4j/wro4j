/*
 * Copyright (c) 2010. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import static ro.isdc.wro.util.WroUtil.cleanImageUrl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ImportAware;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.CssUrlInspector;
import ro.isdc.wro.model.resource.processor.support.CssUrlInspector.ItemHandler;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;


/**
 * A processor responsible for rewriting URLs from inside the css resources.
 *
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public abstract class AbstractCssUrlRewritingProcessor
    implements ResourcePreProcessor, ResourcePostProcessor, ImportAware {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCssUrlRewritingProcessor.class);

  @Inject
  private ReadOnlyContext context;

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    throw new WroRuntimeException("This processor: " + getClass().getSimpleName() + " cannot work as a postProcessor!");
  }

  @Override
  public final void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    LOG.debug("Applying {} processor", getClass().getSimpleName());
    try (reader;writer) {
      final String cssUri = resource != null ? resource.getUri() : StringUtils.EMPTY;
      LOG.debug("cssUri: {}", cssUri);
      final String css = IOUtils.toString(reader);
      final String result = newCssUrlInspector().findAndReplace(css, createUrlItemHandler(cssUri));
      writer.write(result);
      onProcessCompleted();
    }
  }

  private ItemHandler createUrlItemHandler(final String cssUri) {
    return new ItemHandler() {
      @Override
      public String replace(final String originalDeclaration, final String originalUrl) {
        Validate.notNull(originalUrl);
        String replacement = originalDeclaration;
        if (isReplaceNeeded(originalUrl)) {
          final String modifiedUrl = replaceImageUrl(cssUri.trim(), cleanImageUrl(originalUrl));
          LOG.debug("replaced old Url: [{}] with: [{}].", originalUrl, modifiedUrl);
          /**
           * prevent the IllegalArgumentException because of invalid characters like $ (@see issue381) The solution is
           * from stackoverflow: @see
           * http://stackoverflow.com/questions/947116/matcher-appendreplacement-with-literal-text
           */
          final String modifiedDeclaration = Matcher.quoteReplacement(originalDeclaration.replace(originalUrl,
              modifiedUrl));
          onUrlReplaced(modifiedUrl);
          replacement = replaceDeclaration(originalDeclaration.trim(), modifiedDeclaration);
        }
        return replacement;
      }
    };
  }

  protected CssUrlInspector newCssUrlInspector() {
    return new CssUrlInspector();
  }

  /**
   * Invoked when the process operation is completed. Useful to invoke some post processing logic or for custom logging.
   */
  protected void onProcessCompleted() {
  }

  /**
   * Invoked to replace the entire css declaration. An example of css declaration:
   *
   * <pre>
   * background: url(/image.png);
   * </pre>
   *
   * Useful when the css declaration should be changed. The use-case is: {@link FallbackCssDataUriProcessor}.
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
   * Check if url must be replaced or not. The replacement is not needed if the url of the image is absolute (can be
   * resolved by urlResourceLocator) or if the url is a data uri (base64 encoded value).
   *
   * @param url
   *          to check.
   * @return true if url needs to be replaced or remain unchanged.
   */
  protected boolean isReplaceNeeded(final String url) {
    return !(UrlUriLocator.isValid(url) || DataUriGenerator.isDataUri(url.trim()));
  }

  /**
   * This method has protected modifier in order to be accessed by unit test class.
   *
   * @return urlPrefix value.
   */
  protected String getUrlPrefix() {
    return ResourceProxyRequestHandler.createProxyPath(context.getRequest().getRequestURI(), "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isImportAware() {
    // We want this processor to be applied when processing resources referred with @import directive
    return true;
  }
}
