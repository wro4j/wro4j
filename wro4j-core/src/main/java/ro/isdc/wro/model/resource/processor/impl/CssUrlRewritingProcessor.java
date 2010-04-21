/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * CssUrlRewritingProcessor.<br>
 * The algorithm requires two types of {@link UriLocator} objects, one for resolving url resources & one for
 * classpathresources. Both need to be injected using IoC when creating the instance of {@link CssUrlRewritingProcessor}
 * class.
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
 *
 * @author Alex Objelean
 * @created Created on Nov 19, 2008
 */
@SupportedResourceType(ResourceType.CSS)
public class CssUrlRewritingProcessor
  implements ResourcePreProcessor {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CssUrlRewritingProcessor.class);

  /**
   * Resources mapping path. If request uri contains this, the filter will dispatch it to the original resource.
   */
  public static final String PATH_RESOURCES = "wroResources";

  /**
   * The name of resource id parameter.
   */
  public static final String PARAM_RESOURCE_ID = "id";

  /**
   * Src Url pattern.
   */
  private static final String PATTERN_PATH = "url\\s*\\(((?:.|\\s)*?)\\)|src\\s*=\\s*['\"]((?:.|\\s)*?)['\"]";

  /**
   * Compiled pattern.
   */
  private static final Pattern PATTERN = Pattern.compile(PATTERN_PATH, Pattern.CASE_INSENSITIVE);
  /**
   * A set of allowed url's.
   */
  private final Set<String> allowedUrls = Collections.synchronizedSet(new HashSet<String>());


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
      LOG.debug("allowed urls: " + allowedUrls);
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
      final String replacedUrl = replaceImageUrl(urlGroup, cssUri);
      LOG.debug("replacedImageUrl: " + replacedUrl);
      final String newReplacement = oldMatch.replace(urlGroup, replacedUrl);
      // update allowedUrls list
      // TODO no need to hold absolute url's inside
      allowedUrls.add(replacedUrl.replace(getUrlPrefix(), ""));
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
  private String replaceImageUrl(final String imageUrl, final String cssUri) {
    LOG.debug("replace url for image: " + imageUrl + ", from css: " + cssUri);
    if (isReplaceNeeded(imageUrl)) {
      if (ServletContextUriLocator.isValid(cssUri)) {
        if (ServletContextUriLocator.isValid(imageUrl)) {
          return imageUrl;
        }
        // Treat WEB-INF special case
        if (ServletContextUriLocator.isProtectedResource(cssUri)) {
          return getUrlPrefix() + computeNewImageLocation(cssUri, imageUrl);
        }
        return computeNewImageLocation(".." + cssUri, imageUrl);
      }
      if (UrlUriLocator.isValid(cssUri) || ClasspathUriLocator.isValid(cssUri)) {
        return getUrlPrefix() + computeNewImageLocation(cssUri, imageUrl);
      } else {
        throw new WroRuntimeException("Could not replace imageUrl: " + imageUrl + ", contained at location: " + cssUri);
      }
    }
    return imageUrl;
  }


  /**
   * Concatenates cssUri and imageUrl after few changes are applied to both input parameters.
   *
   * @param cssUri the URI of css resource.
   * @param imageUrl the URL of image referred in css.
   * @return processed new location of image url.
   */
  private String computeNewImageLocation(final String cssUri, final String imageUrl) {
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
    final String processedImageUrl = cleanImageUrl.startsWith(ServletContextUriLocator.PREFIX)
      ? cleanImageUrl.substring(1)
      : cleanImageUrl;
    return cssUriFolder + processedImageUrl;
  }


  /**
   * Cleans the image url by triming result and removing \' or \" characters if such exists.
   *
   * @param imageUrl to clean.
   * @return cleaned image URL.
   */
  private String cleanImageUrl(final String imageUrl) {
    final String result = imageUrl.replace('\'', ' ').replace('\"', ' ').trim();
    return result.toString();
  }


  /**
   * Check if url must be replaced or not.
   *
   * @param url to check.
   * @return true if url needs to be replaced or remain unchanged.
   */
  private boolean isReplaceNeeded(final String url) {
    // The replacement is not needed if the url of the image is absolute (can be
    // resolved by urlResourceLocator).
    return !UrlUriLocator.isValid(url);
  }


  /**
   * @param uri to check if is allowed.
   * @return true if passed argument is contained in allowed list.
   */
  public final boolean isUriAllowed(final String uri) {
    return allowedUrls.contains(uri);
  }


  /**
   * This method has protected modifier in order to be accessed by unit test class.
   *
   * @return urlPrefix value.
   */
  protected String getUrlPrefix() {
    final String requestURI = Context.get().getRequest().getRequestURI();
    return WroUtil.getFolderOfUri(requestURI) + PATH_RESOURCES + "?" + PARAM_RESOURCE_ID + "=";
  }
}
