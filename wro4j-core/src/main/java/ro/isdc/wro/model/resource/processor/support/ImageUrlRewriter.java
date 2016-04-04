package ro.isdc.wro.model.resource.processor.support;

import static org.apache.commons.lang3.Validate.notNull;
import static ro.isdc.wro.util.StringUtils.cleanPath;
import static ro.isdc.wro.util.WroUtil.cleanImageUrl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * Responsible for computing the url of the images from css based on the location of the css where they are located.
 *
 * @author Alex Objelean
 * @since 1.7.0
 */
public class ImageUrlRewriter {
  private static final Logger LOG = LoggerFactory.getLogger(ImageUrlRewriter.class);
  private static final String ROOT_CONTEXT_PATH = ServletContextUriLocator.PREFIX;
  private static final String FOLDER_PREFIX = "/..";
  private final RewriterContext context;

  /**
   * Holds the properties required by this class to perform rewrite operation.
   */
  public static final class RewriterContext {
    private String proxyPrefix;
    private String aggregatedFolderPath;
    private String contextPath;

    public RewriterContext setProxyPrefix(final String proxyPrefix) {
      this.proxyPrefix = proxyPrefix;
      return this;
    }

    public RewriterContext setAggregatedFolderPath(final String aggregatedFolderPath) {
      this.aggregatedFolderPath = aggregatedFolderPath;
      return this;
    }

    public RewriterContext setContextPath(final String contextPath) {
      this.contextPath = contextPath;
      return this;
    }
  }

  public ImageUrlRewriter(final RewriterContext context) {
    notNull(context);
    notNull(context.proxyPrefix);
    if (context.contextPath == null) {
      context.setContextPath(ROOT_CONTEXT_PATH);
    }
    this.context = context;
  }

  /**
   * Computes the url of the image to be replaced in a css resource.
   *
   * @param cssUri
   *          the uri of the css where the image is located.
   * @param imageUrl
   *          the url of the image (relative or absolute).
   * @return replaced url of the image.
   */
  public String rewrite(final String cssUri, final String imageUrl) {
    notNull(cssUri);
    notNull(imageUrl);
    if (StringUtils.isEmpty(imageUrl)) {
       return imageUrl;
    }
    if (ServletContextUriLocator.isValid(cssUri)) {
      if (ServletContextUriLocator.isValid(imageUrl)) {
        return prependContextPath(imageUrl);
      }
      // Treat WEB-INF special case
      if (ServletContextUriLocator.isProtectedResource(cssUri)) {
        return context.proxyPrefix + computeNewImageLocation(cssUri, imageUrl);
      }
      // Compute the folder where the final css is located. This is important for computing image location after url
      // rewriting.
      // Prefix of the path to the overwritten image url. This will be of the following type: "../" or "../.." depending
      // on the depth of the aggregatedFolderPath.
      final String aggregatedPathPrefix = computeAggregationPathPrefix(context.aggregatedFolderPath);
      LOG.debug("computed aggregatedPathPrefix {}", aggregatedPathPrefix);
      String newImageLocation = computeNewImageLocation(aggregatedPathPrefix + cssUri, imageUrl);
      if (newImageLocation.startsWith(ServletContextUriLocator.PREFIX)) {
        newImageLocation = prependContextPath(newImageLocation);
      }
      LOG.debug("newImageLocation: {}", newImageLocation);
      return newImageLocation;
    }
    if (ClasspathUriLocator.isValid(cssUri)) {
      final String proxyUrl = context.proxyPrefix + computeNewImageLocation(cssUri, imageUrl);
      final String contextRelativeUrl = prependContextPath(imageUrl);
      //final String contextRelativeUrl = context.contextPath + imageUrl;
      // leave imageUrl unchanged if it is a servlet context relative resource
      return (ServletContextUriLocator.isValid(imageUrl) ? contextRelativeUrl : proxyUrl);
    }
    if (UrlUriLocator.isValid(cssUri)) {
      final String computedCssUri = ServletContextUriLocator.isValid(imageUrl) ? computeCssUriForExternalServer(cssUri)
          : cssUri;
      return computeNewImageLocation(computedCssUri, imageUrl);
    }
    throw new WroRuntimeException("Could not replace imageUrl: " + imageUrl + ", contained at location: " + cssUri);
  }

  private String prependContextPath(final String imageUrl) {
    //avoid double slash
    return context.contextPath.endsWith(ROOT_CONTEXT_PATH) ? imageUrl
        : context.contextPath + imageUrl;
  }

  /**
   * @return the path to be prefixed after css aggregation. This depends on the aggregated css destination folder. This
   *         is a fix for the following issue: {@link http://code.google.com/p/wro4j/issues/detail?id=259}
   */
  private String computeAggregationPathPrefix(final String aggregatedFolderPath) {
    LOG.debug("aggregatedFolderPath: {}", aggregatedFolderPath);
    String computedPrefix = StringUtils.EMPTY;
    if (aggregatedFolderPath != null) {
      final StringBuffer result = new StringBuffer(StringUtils.EMPTY);
      final String[] depthFolders = WroUtil.normalize(aggregatedFolderPath).split(ROOT_CONTEXT_PATH);
      LOG.debug("subfolders {}", Arrays.toString(depthFolders));

      for (final String folder : depthFolders) {
        if (!StringUtils.isEmpty(folder)) {
          result.append(FOLDER_PREFIX);
        }
      }
      computedPrefix = result.toString().replaceFirst(ROOT_CONTEXT_PATH, StringUtils.EMPTY);
    }
    LOG.debug("computedPrefix: {}", computedPrefix);
    return computedPrefix;
  }

  /**
   * Css files hosted on external server, should use its host as the root context when rewriting image url's starting
   * with '/' character.
   */
  private String computeCssUriForExternalServer(final String cssUri) {
    String exernalServerCssUri = cssUri;
    try {
      // compute the host of the external server (with protocol & port).
      final String serverHost = cssUri.replace(new URL(cssUri).getPath(), StringUtils.EMPTY);
      // the uri should end mandatory with /
      exernalServerCssUri = serverHost + ServletContextUriLocator.PREFIX;
      LOG.debug("using {} host as cssUri", exernalServerCssUri);
    } catch (final MalformedURLException e) {
      // should never happen
    }
    return exernalServerCssUri;
  }

  /**
   * Concatenates cssUri and imageUrl after few changes are applied to both input parameters.
   *
   * @param cssUri
   *          the URI of css resource.
   * @param imageUrl
   *          the URL of image referred in css.
   * @return processed new location of image url.
   */
  private String computeNewImageLocation(final String cssUri, final String imageUrl) {
    LOG.debug("cssUri: {}, imageUrl {}", cssUri, imageUrl);
    final String cleanImageUrl = cleanImageUrl(imageUrl);
    // TODO move to ServletContextUriLocator as a helper method?
    // for the following input: /a/b/c/1.css => /a/b/c/
    int idxLastSeparator = cssUri.lastIndexOf(ServletContextUriLocator.PREFIX);
    if (idxLastSeparator == -1) {
      if (ClasspathUriLocator.isValid(cssUri)) {
        idxLastSeparator = cssUri.lastIndexOf(ClasspathUriLocator.PREFIX);
        // find the index of ':' character used by classpath prefix
        if (idxLastSeparator >= 0) {
          idxLastSeparator += ClasspathUriLocator.PREFIX.length() - 1;
        }
      }
      if (idxLastSeparator < 0) {
        throw new IllegalStateException("Invalid cssUri: " + cssUri + ". Should contain at least one '/' character!");
      }
    }
    final String cssUriFolder = cssUri.substring(0, idxLastSeparator + 1);
    // remove '/' from imageUrl if it starts with one.
    final String processedImageUrl = cleanImageUrl.startsWith(ServletContextUriLocator.PREFIX) ? cleanImageUrl.substring(1)
        : cleanImageUrl;
    final String computedImageLocation = cleanPath(cssUriFolder + processedImageUrl);
    LOG.debug("computedImageLocation: {}", computedImageLocation);
    return computedImageLocation;
  }
}
