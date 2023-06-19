/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.support.ImageUrlRewriter;
import ro.isdc.wro.model.resource.processor.support.ImageUrlRewriter.RewriterContext;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;


/**
 * <p>Note: When used together with {@link CssImportPreProcessor}, the {@link CssUrlRewritingProcessor} should come first,
 * otherwise it will produce wrong results.</p>
 *
 * <p>Rewrites background images url of the provided css content. This implementation takes care of most common cases such
 * as those described below:</p>
 *
 * <table border="1">
 * <caption>URL rewrite common use cases</caption>
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
 * <p>The algorithm requires two types of {@link UriLocator} objects, one for resolving URL resources and one for classpath
 * resources. Both need to be injected using IoC when creating the instance of {@link CssUrlRewritingProcessor} class.</p>
 *
 * @author Alex Objelean
 */
public class CssUrlRewritingProcessor
    extends AbstractCssUrlRewritingProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssUrlRewritingProcessor.class);
  public static final String ALIAS = "cssUrlRewriting";
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private ReadOnlyContext context;

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
    //Can be null when using standalone context.
    final String contextPath = context.getRequest() != null ? context.getRequest().getContextPath() : null;
    final RewriterContext rewriterContext = new RewriterContext().setAggregatedFolderPath(
        context.getAggregatedFolderPath()).setProxyPrefix(getUrlPrefix()).setContextPath(contextPath);

    return new ImageUrlRewriter(rewriterContext).rewrite(cssUri, imageUrl);
  }

  /**
   * @param uri
   *          to check if is allowed.
   * @return true if passed argument is contained in allowed list.
   */
  public final boolean isUriAllowed(final String uri) {
    return authorizationManager.isAuthorized(uri);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onProcessCompleted() {
  }
}
