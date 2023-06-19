/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import static ro.isdc.wro.util.WroUtil.cleanImageUrl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.http.handler.ResourceProxyRequestHandler;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.support.DataUriGenerator;
import ro.isdc.wro.util.WroUtil;


/**
 * <p>Rewrites background images by replacing the url with data uri of the image. If the replacement is not successful, it
 * is left unchanged.</p>
 *
 * <p>Attention: This processor should be added before {@link CssUrlRewritingProcessor}, otherwise the URLs won't be
 * replaced. For more details, see <a href="http://en.wikipedia.org/wiki/Data_URI_scheme">DataUri Scheme on
 * Wikipedia</a></p>
 *
 * @author Alex Objelean
 */
public class CssDataUriPreProcessor
    extends AbstractCssUrlRewritingProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssDataUriPreProcessor.class);
  public static final String ALIAS = "cssDataUri";
  /**
   * The size limit. Images larger than this limit won't be transformed (due to IE8 limitation).
   */
  private static final int SIZE_LIMIT = 32 * 1024;
  /**
   * Generates dataUri based on inputStream of the url's found inside the css resource.
   */
  private DataUriGenerator dataUriGenerator;
  /**
   * Contains a {@link UriLocatorFactory} reference injected externally.
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Replace provided url with the new url if needed.
   *
   * @param imageUrl
   *          to replace.
   * @param cssUri
   *          Uri of the parsed css.
   * @return replaced url.
   */
  @Override
  protected String replaceImageUrl(final String cssUri, final String imageUrl) {
    Validate.notNull(uriLocatorFactory);
    LOG.debug("replace url for image: {} from css: {}", imageUrl, cssUri);
    final String cleanImageUrl = cleanImageUrl(imageUrl);
    final String fileName = FilenameUtils.getName(imageUrl);
    String fullPath = cleanImageUrl;
    /**
     * Allow dataUri transformation of absolute url's using http(s) protocol. All url's protocol are intentionally not
     * allowed, because it could be a potential security issue. For instance:
     *
     * <pre>
     * .class {
     *   background: url(file:/path/to/secure/file.png);
     * }
     * </pre>
     *
     * This should not be allowed.
     */
    if (isImageUrlChangeRequired(cleanImageUrl)) {
      fullPath = WroUtil.getFullPath(cssUri) + cleanImageUrl;
    }
    String result = imageUrl;

    try (InputStream is = uriLocatorFactory.locate(fullPath)) {
      final String dataUri = getDataUriGenerator().generateDataURI(is, fileName);
      if (isReplaceAccepted(dataUri)) {
        result = dataUri;
        LOG.debug("dataUri replacement: {}", StringUtils.abbreviate(dataUri, 30));
      }
    } catch (final IOException e) {
      LOG.warn("[FAIL] extract dataUri from: {}, because: {}. "
          + "A possible cause: using CssUrlRewritingProcessor before CssDataUriPreProcessor.", fullPath, e.getMessage());
    }
    return result;
  }

  /**
   * @param imageUrl
   *          the original url of the image.
   * @return true if the image url should be replaced with another (servlet context relative).
   */
  private boolean isImageUrlChangeRequired(final String imageUrl) {
    return !(imageUrl.startsWith("http") || ResourceProxyRequestHandler.isProxyUri(imageUrl));
  }

  /**
   * Similar to {@link CssDataUriPreProcessor#isReplaceAccepted(String)}, but decides whether the computed dataUri
   * should replace the image url. It is useful when you want to limit the dataUri size. By default the size of dataUri
   * is limited to 32KB (because IE8 has a 32KB limitation).
   *
   * @param dataUri
   *          base64 encoded stream.
   * @return true if dataUri should replace original image url.
   */
  protected boolean isReplaceAccepted(final String dataUri) {
    final byte[] bytes = dataUri.getBytes(StandardCharsets.UTF_8);
    final boolean exceedLimit = bytes.length >= SIZE_LIMIT;
    LOG.debug("dataUri size: {}KB, limit exceeded: {}", bytes.length / 1024, exceedLimit);
    return !exceedLimit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isReplaceNeeded(final String url) {
    // the dataUri should replace also absolute url's
    return !DataUriGenerator.isDataUri(url.trim());
  }

  /**
   * @return the DataUriGenerator class responsible for transforming streams into base64 encoded strings.
   */
  protected DataUriGenerator getDataUriGenerator() {
    if (dataUriGenerator == null) {
      dataUriGenerator = new DataUriGenerator();
    }
    return dataUriGenerator;
  }
}
