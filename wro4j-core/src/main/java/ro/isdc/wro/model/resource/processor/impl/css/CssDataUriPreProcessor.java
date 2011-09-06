/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.algorithm.DataUriGenerator;


/**
 * Rewrites background images by replacing the url with data uri of the image. If the replacement is not successful, it
 * is left unchanged.
 * <p/>
 * For more details, @see http://en.wikipedia.org/wiki/Data_URI_scheme
 *
 * @author Alex Objelean
 * @created May 9, 2010
 */
public class CssDataUriPreProcessor
  extends AbstractCssUrlRewritingProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssDataUriPreProcessor.class);
  public static final String ALIAS = "cssDataUri";
  /**
   * The size limit. Images larger than this limit won't be transformed (due to IE8 limitation).
   */
  private static final int SIZE_LIMIT = 32 * 1024;;
  /**
   * Generates dataUri based on inputStream of the url's found inside the css resource.
   */
  private DataUriGenerator dataUriGenerator;
  /**
   * Injected by wro4j.
   */
  @Inject
  private ResourceLocatorFactory resourceLocatorFactory;

  /**
   * Replace provided url with the new url if needed.
   *
   * @param imageUrl to replace.
   * @param cssUri Uri of the parsed css.
   * @return replaced url.
   */
  @Override
  protected String replaceImageUrl(final String cssUri, final String imageUrl) {
    Validate.notNull(resourceLocatorFactory);
    LOG.debug("replace url for image: " + imageUrl + ", from css: " + cssUri);
    final String cleanImageUrl = cleanImageUrl(imageUrl);
    final String fileName = FilenameUtils.getName(imageUrl);
    String fullPath = cleanImageUrl;
    /**
     * Allow dataUri transformation of absolute url's using http(s) protocol. All url's protocola are intentionally not
     * allowed, because it could be a potential security issue. For instance: <code>
     * .class {
     *   background: url(file:/path/to/secure/file.png);
     * }
     * <code>
     * This should not be allowed.
     */
    if (!cleanImageUrl.startsWith("http")) {
      fullPath = FilenameUtils.getFullPath(cssUri) + cleanImageUrl;
    }
    String result = imageUrl;
    try {
      final InputStream inputStream = resourceLocatorFactory.locate(fullPath).getInputStream();
      final String dataUri = getDataUriGenerator().generateDataURI(inputStream, fileName);
      if (replaceWithDataUri(dataUri)) {
        result = dataUri;
        LOG.debug("dataUri replacement: " + StringUtils.abbreviate(dataUri, 30));
      }
    } catch (final IOException e) {
      LOG.warn("Couldn't extract dataUri from:" + fullPath + ", because: " + e.getMessage());
    }
    return result;
  }

  private DataUriGenerator getDataUriGenerator() {
    if (dataUriGenerator == null) {
      dataUriGenerator = new DataUriGenerator();
    }
    return dataUriGenerator;
  }

  /**
   * Decides whether the computed dataUri should replace the image url. It is useful when you want to limit the dataUri size.
   * By default the size of dataUri is limited to 32KB (because IE8 has a 32KB limitation).
   *
   * @param dataUri base64 encoded stream.
   * @return true if dataUri should replace original image url.
   */
  protected boolean replaceWithDataUri(final String dataUri) throws UnsupportedEncodingException {
    final byte[] bytes = dataUri.getBytes("UTF8");
    final boolean exceedLimit = bytes.length >= SIZE_LIMIT;
    LOG.debug("dataUri size: " + bytes.length/1024 + "KB, limit exceeded: " + exceedLimit);
    return !exceedLimit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isReplaceNeeded(final String url) {
    //the dataUri should replace also absolute url's
    return !DataUriGenerator.isDataUri(url.trim());
  }
}
