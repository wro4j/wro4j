/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import static ro.isdc.wro.util.StringUtils.cleanPath;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;


/**
 * Note: When used together with {@link CssImportPreProcessor}, the {@link CssUrlRewritingProcessor} should come first,
 * otherwise it will produce wrong results.
 * <p>
 * Rewrites background images url of the provided css content. This implementation takes care of most common cases such
 * as those described bellow:
 * <p>
 * <table border="1" cellpadding="5">
 * <thead>
 * <tr>
 * <th>Css resource URI</th>
 * <th>Image URL</th>
 * <th>Computed Image URL</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>ANY</td>
 * <td>[URL]/1.jpg</td>
 * <td>[URL]/1.jpg</td>
 * </tr>
 * <tr>
 * <td rowspan="4">/1.css</td>
 * <td>/a/1.jpg</td>
 * <td>/a/1.jpg</td>
 * </tr>
 * <tr>
 * <td>/1.jpg</td>
 * <td>/1.jpg</td>
 * </tr>
 * <tr>
 * <td>1.jpg</td>
 * <td>../1.jpg</td>
 * </tr>
 * <tr>
 * <td>../1.jpg</td>
 * <td>../../1.jpg</td>
 * </tr>
 * <tr>
 * <td rowspan="4">/WEB-INF/1.css</td>
 * <td>/a/1.jpg</td>
 * <td>/a/1.jpg</td>
 * </tr>
 * <tr>
 * <td>/1.jpg</td>
 * <td>/1.jpg</td>
 * </tr>
 * <tr>
 * <td>1.jpg</td>
 * <td>[WRO-PREFIX]?id=/WEB-INF/1.jpg</td>
 * </tr>
 * <tr>
 * <td>../1.jpg</td>
 * <td>[WRO-PREFIX]?id=/WEB-INF/../1.jpg</td>
 * </tr>
 * <tr>
 * <td rowspan="4">[X]/1.css <br/>
 * <br/>
 * where [X] is URL or a classpath resource<br/>
 * where [WRO-PREFIX] is a servletContext prefix <br/>
 * which will map WRO filter to the result url.</td>
 * <td>/a/1.jpg</td>
 * <td>[WRO-PREFIX]?id=[X]/a/1.jpg</td>
 * </tr>
 * <tr>
 * <td>/1.jpg</td>
 * <td>[WRO-PREFIX]?id=[X]/1.jpg</td>
 * </tr>
 * <tr>
 * <td>1.jpg</td>
 * <td>[WRO-PREFIX]?id=[X]/1.jpg</td>
 * </tr>
 * <tr>
 * <td>../1.jpg</td>
 * <td>[WRO-PREFIX]?id=[X]/../1.jpg</td>
 * </tr>
 * </tbody>
 * </table>
 * <p/>
 * The algorithm requires two types of {@link UriLocator} objects, one for resolving url resources & one for classpath
 * resources. Both need to be injected using IoC when creating the instance of {@link CssUrlRewritingProcessor} class.
 * 
 * @author Alex Objelean
 * @created Nov 19, 2008
 */
public class CssUrlRewritingProcessor
    extends AbstractCssUrlRewritingProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssUrlRewritingProcessor.class);
  public static final String ALIAS = "cssUrlRewriting";
  /**
   * A set of allowed url's.
   */
  private final Set<String> allowedUrls = Collections.synchronizedSet(new HashSet<String>());
  /**
   * Prefix of the path to the overwritten image url. This will be of the following type: "../" or "../.." depending on
   * the depth of the aggregatedFolderPath.
   */
  private String aggregatedPathPrefix;
  @Inject
  private Context context;
  
  /**
   * The folder where the final css is located. This is important for computing image location after url rewriting.
   * 
   * @param aggregatedFolderPath
   *          the aggregatedFolder to set
   */
  private CssUrlRewritingProcessor setAggregatedFolderPath(final String aggregatedFolderPath) {
    aggregatedPathPrefix = computeAggregationPathPrefix(aggregatedFolderPath);
    LOG.debug("computed aggregatedPathPrefix {}", aggregatedPathPrefix);
    return this;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onProcessCompleted() {
    LOG.debug("allowed urls: {}", allowedUrls);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onUrlReplaced(final String replacedUrl) {
    final String allowedUrl = StringUtils.removeStart(replacedUrl, getUrlPrefix());
    LOG.debug("adding allowed url: {}", allowedUrl);
    allowedUrls.add(allowedUrl);
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
  @Override
  protected String replaceImageUrl(final String cssUri, final String imageUrl) {
    if (ServletContextUriLocator.isValid(cssUri)) {
      if (ServletContextUriLocator.isValid(imageUrl)) {
        return imageUrl;
      }
      // Treat WEB-INF special case
      if (ServletContextUriLocator.isProtectedResource(cssUri)) {
        return getUrlPrefix() + computeNewImageLocation(cssUri, imageUrl);
      }
      // ensure the folder path is set
      setAggregatedFolderPath(context.getAggregatedFolderPath());
      LOG.debug("aggregatedPathPrefix: {}", this.aggregatedPathPrefix);
      return computeNewImageLocation(this.aggregatedPathPrefix + cssUri, imageUrl);
    }
    if (UrlUriLocator.isValid(cssUri)) {
      return computeNewImageLocation(cssUri, imageUrl);
    }
    if (ClasspathUriLocator.isValid(cssUri)) {
      return getUrlPrefix() + computeNewImageLocation(cssUri, imageUrl);
    }
    throw new WroRuntimeException("Could not replace imageUrl: " + imageUrl + ", contained at location: " + cssUri);
  }
  
  /**
   * @return the path to be prefixed after css aggregation. This depends on the aggregated css destination folder. This
   *         is a fix for the following issue: {@link http://code.google.com/p/wro4j/issues/detail?id=259}
   */
  private String computeAggregationPathPrefix(final String aggregatedFolderPath) {
    LOG.debug("aggregatedFolderPath: {}", aggregatedFolderPath);
    String computedPrefix = StringUtils.EMPTY;
    if (aggregatedFolderPath != null) {
      final String folderPrefix = "/..";
      final StringBuffer result = new StringBuffer("");
      final String[] depthFolders = aggregatedFolderPath.split("/");
      LOG.debug("subfolders {}", Arrays.toString(depthFolders));
      for (final String folder : depthFolders) {
        if (!StringUtils.isEmpty(folder)) {
          result.append(folderPrefix);
        }
      }
      computedPrefix = result.toString().replaceFirst("/", "");
    }
    LOG.debug("computedPrefix: {}", computedPrefix);
    return computedPrefix;
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
    // remove redundant part of the path
    final String computedImageLocation = cleanPath(cssUriFolder + processedImageUrl);
    LOG.debug("computedImageLocation: {}", computedImageLocation);
    return computedImageLocation;
  }
  
  /**
   * @param uri
   *          to check if is allowed.
   * @return true if passed argument is contained in allowed list.
   */
  public final boolean isUriAllowed(final String uri) {
    return allowedUrls.contains(uri);
  }
}
