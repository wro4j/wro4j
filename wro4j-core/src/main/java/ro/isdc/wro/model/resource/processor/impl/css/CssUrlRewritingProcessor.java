/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import static ro.isdc.wro.util.StringUtils.cleanPath;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.support.DefaultResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;


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
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private ReadOnlyContext context;
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onProcessCompleted() {
    if (authorizationManager instanceof DefaultResourceAuthorizationManager) {
      LOG.debug("allowed urls: {}", ((DefaultResourceAuthorizationManager) authorizationManager).list());
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onUrlReplaced(final String replacedUrl) {
    final String allowedUrl = StringUtils.removeStart(replacedUrl, getUrlPrefix());
    LOG.debug("adding allowed url: {}", allowedUrl);
    //add only if add is supported
    if (authorizationManager instanceof MutableResourceAuthorizationManager) {
      ((MutableResourceAuthorizationManager) authorizationManager).add(allowedUrl);      
    }
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
      // Compute the folder where the final css is located. This is important for computing image location after url
      // rewriting.
      // Prefix of the path to the overwritten image url. This will be of the following type: "../" or "../.." depending
      // on the depth of the aggregatedFolderPath.
      final String aggregatedPathPrefix = computeAggregationPathPrefix(context.getAggregatedFolderPath());
      LOG.debug("computed aggregatedPathPrefix {}", aggregatedPathPrefix);
      return computeNewImageLocation(aggregatedPathPrefix + cssUri, imageUrl);
    }
    if (UrlUriLocator.isValid(cssUri)) {
      if (ServletContextUriLocator.isValid(imageUrl)) {
        //when imageUrl starts with /, assume the cssUri is the external server host
        final String externalServerCssUri = computeCssUriForExternalServer(cssUri);
        return computeNewImageLocation(externalServerCssUri, imageUrl);
      }
      return computeNewImageLocation(cssUri, imageUrl);      
    }
    if (ClasspathUriLocator.isValid(cssUri)) {
      return getUrlPrefix() + computeNewImageLocation(cssUri, imageUrl);
    }
    throw new WroRuntimeException("Could not replace imageUrl: " + imageUrl + ", contained at location: " + cssUri);
  }

  /**
   * Css files hosted on external server, should use its host as the root context when rewriting image url's starting
   * with '/' character.
   */
  private String computeCssUriForExternalServer(final String cssUri) {
    String exernalServerCssUri = cssUri;
    try {
      //compute the host of the external server (with protocol & port).
      final String serverHost = cssUri.replace(new URL(cssUri).getPath(), "");
      //the uri should end mandatory with /
      exernalServerCssUri = serverHost + ServletContextUriLocator.PREFIX;
      LOG.debug("using {} host as cssUri", exernalServerCssUri);
    } catch(MalformedURLException e) {
      //should never happen
    }
    return exernalServerCssUri;
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
    // remove redundant part of the path, but skip protected resources (ex: located inside /WEB-INF/ folder). -> Not
    // sure if this is a problem yet.
    // final String computedImageLocation = ServletContextUriLocator.isProtectedResource(cssUriFolder) ? cssUriFolder
    // + processedImageUrl : cleanPath(cssUriFolder + processedImageUrl);
    final String computedImageLocation = cleanPath(cssUriFolder + processedImageUrl);
    LOG.debug("computedImageLocation: {}", computedImageLocation);
    return computedImageLocation;
  }
  
  /**
   * @param uri
   *          to check if is allowed.
   * @return true if passed argument is contained in allowed list.
   * @VisibleFortesting
   */
  public final boolean isUriAllowed(final String uri) {
    return authorizationManager.isAuthorized(uri);
  }
}
