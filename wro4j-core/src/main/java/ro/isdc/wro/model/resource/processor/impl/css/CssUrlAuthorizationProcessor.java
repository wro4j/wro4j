/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;


/**
 * <p>
 * This needs to be paired with the CssUrlRewritingProcessor for expected functionality.
 * </p>
 * <p>
 * It addresses a specific use case where a combination of pre/post processors are modifying URLs so that the
 * ResourceAuthorizationManager no longer recognizes them to be retrieved from the classpath.
 * </p>
 * <p>
 * For example, assets stored in LESS variables are manipulated by both the CSS URL rewriter, which generates the first
 * entry in the ResourceAuthorizationManager, and then the LESS compiler, which does not update the
 * ResourceAuthorizationManager. This post processor simply adds any missing entries in ResourceAuthorizationManager for
 * assets loaded from the classpath.
 * </p>
 * <p>
 * It should be noted that this processor exposes you to considerable risk if you use dynamic CSS provided by users,
 * because it authorizes any and all asset loads if they are found in your CSS/LESS. It is not suitable for use in those
 * conditions since it would allow users to retrieve any data from your classpath.
 * </p>
 * Adapted from {@link CssUrlRewritingProcessor} and {@link AbstractCssUrlRewritingProcessor}
 *
 * @author Greg Pendlebury
 * @since 1.7.6
 */
public class CssUrlAuthorizationProcessor
    extends CssUrlRewritingProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssUrlAuthorizationProcessor.class);
  public static final String ALIAS = "cssClasspathUrlAuthorization";

  @Inject
  private ResourceAuthorizationManager authorizationManager;

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  @Override
  protected String replaceImageUrl(final String cssUri, final String imageUrl) {
    // Don't really replace, we are just hijacking the functionality of CssUrlRewritingProcessor
    return imageUrl;
  }

  @Override
  protected void onUrlReplaced(final String replacedUrl) {
    final String allowedUrl = StringUtils.removeStart(replacedUrl, getUrlPrefix());

    if (authorizationManager instanceof MutableResourceAuthorizationManager) {
      if (!authorizationManager.isAuthorized(allowedUrl)) {
        LOG.debug("Authorizing url: '{}'", allowedUrl);
        ((MutableResourceAuthorizationManager) authorizationManager).add(allowedUrl);
      }

    } else {
      throw new WroRuntimeException("This processor (" + getClass().getSimpleName()
          + ") requires an instance of MutableResourceAuthorizationManager!");
    }
  }
}
