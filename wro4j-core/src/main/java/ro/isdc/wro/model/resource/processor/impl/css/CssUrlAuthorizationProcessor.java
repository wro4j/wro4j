/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.support.CssImportInspector;
import ro.isdc.wro.model.resource.support.MutableResourceAuthorizationManager;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.WroUtil;


/**
 * <p>This needs to be paired with the CssUrlRewritingProcessor for expected functionality.</p>
 * 
 * <p>It addresses a specific use case where a combination of pre/post processors are modifying URLs so that
 * the ResourceAuthorizationManager no longer recognizes them to be retrieved from the classpath.</p>
 * 
 * <p>For example, assets stored in LESS variables are manipulated by both the CSS URL rewriter, which generates
 * the first entry in the ResourceAuthorizationManager, and then the LESS compiler, which does not update the
 * ResourceAuthorizationManager. This post processor simply adds any missing entries in ResourceAuthorizationManager
 * for assets loaded from the classpath.</p>
 * 
 * <p>It should be noted that this processor exposes you to considerable risk if you use dynamic CSS provided by users,
 * because it authorizes any and all asset loads if they are found in your CSS/LESS. It is not suitable for use in
 * those conditions since it would allow users to retrieve any data from your classpath.</p>
 * 
 * @author Greg Pendlebury
 * @created June 10, 2014
 * 
 * Adapted from CssUrlRewritingProcessor and AbstractCssUrlRewritingProcessor
 * 
 * @author Alex Objelean
 */
public class CssUrlAuthorizationProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssUrlAuthorizationProcessor.class);
  public static final String ALIAS = "cssClasspathUrlAuthorization";
  /**
   * Compiled pattern.
   */
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssUrlRewrite"));
  @Inject
  private ResourceAuthorizationManager authorizationManager;
  @Inject
  private ReadOnlyContext context;

  /**
   * {@inheritDoc}
   */
  public void process(Reader reader, Writer writer) throws IOException {
     LOG.debug("Applying {} processor", getClass().getSimpleName());
    try {
      final String css = IOUtils.toString(reader);
      parseCss(css);
      writer.write(css);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(Resource resource, Reader reader, Writer writer) throws IOException {
    process(reader, writer);
  }

  /**
   * Parse the css content and find previously processed URLs
   * 
   * @param cssContent
   *          to parse.
   */
  private void parseCss(final String cssContent) {
    final Matcher matcher = PATTERN.matcher(cssContent);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      // Do not process @import statements
      final String cssStatement = matcher.group();
      if (!new CssImportInspector(cssStatement).containsImport()) {
        final String originalUrl = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        Validate.notNull(originalUrl);

        final String allowedUrl = StringUtils.removeStart(originalUrl, getUrlPrefix());
        if (!authorizationManager.isAuthorized(allowedUrl) && allowedUrl.contains("classpath")) {
          LOG.debug("Authorizing classpath url: '{}'", allowedUrl);

          if (authorizationManager instanceof MutableResourceAuthorizationManager) {
            ((MutableResourceAuthorizationManager) authorizationManager).add(allowedUrl);
  
          } else {
            throw new WroRuntimeException("This processor (" + getClass().getSimpleName()
                    + ") requires an instance of MutableResourceAuthorizationManager!");
          }
        }
      }
    }
  }

  /**
   * This method has protected modifier in order to be accessed by unit test class.
   * 
   * @return urlPrefix value.
   * @VisibleForTesting
   */
  protected String getUrlPrefix() {
    return ResourceProxyRequestHandler.createProxyPath(context.getRequest().getRequestURI(), "");
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
