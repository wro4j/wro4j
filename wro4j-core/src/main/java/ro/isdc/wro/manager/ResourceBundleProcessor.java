package ro.isdc.wro.manager;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.factory.CacheKeyFactory;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.WroUtil;


/**
 * Encapsulates the bundle creation.
 *
 * @author Alex Objelean
 * @since 1.4.7
 */
public class ResourceBundleProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleProcessor.class);

  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  @Inject
  private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
  
  @Inject
  private ReadOnlyContext context;
  
  @Inject
  private CacheKeyFactory cacheKeyFactory;

  /**
   * Write to stream the content of the processed resource bundle.
   */
  public void serveProcessedBundle()
      throws IOException {

    final WroConfiguration configuration = context.getConfig();
    final HttpServletRequest request = context.getRequest();
    final HttpServletResponse response = context.getResponse();

    final CacheKey cacheKey = getSafeCacheKey(request);
    initAggregatedFolderPath(request, cacheKey.getType());
    final CacheValue cacheValue = cacheStrategy.get(cacheKey);

    // TODO move ETag check in wroManagerFactory
    final String ifNoneMatch = request.getHeader(HttpHeader.IF_NONE_MATCH.toString());

    // enclose etag value in quotes to be compliant with the RFC
    final String etagValue = String.format("\"%s\"", cacheValue.getHash());

    if (etagValue != null && etagValue.equals(ifNoneMatch)) {
      LOG.debug("ETag hash detected: {}. Sending {} status code", etagValue, HttpServletResponse.SC_NOT_MODIFIED);
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      // because we cannot return null, return a stream containing nothing.
      // TODO close output stream?
      return;
    }
    /**
     * Set contentType before actual content is written, solves <br/>
     * <a href="http://code.google.com/p/wro4j/issues/detail?id=341">issue341</a>
     */
    response.setContentType(cacheKey.getType().getContentType() + "; charset=" + configuration.getEncoding());
    // set ETag header
    response.setHeader(HttpHeader.ETAG.toString(), etagValue);
    response.addHeader(HttpHeader.VARY.toString(), HttpHeader.ACCEPT_ENCODING.toString());

    try (OutputStream os = response.getOutputStream()) {
      if (cacheValue.getRawContent() != null) {
        // use gziped response if supported & Set content length based on gzip flag
        if (isGzipAllowed()) {
          response.setContentLength(cacheValue.getGzippedContent().length);
          // add gzip header and gzip response
          response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
          IOUtils.write(cacheValue.getGzippedContent(), os);
        } else {
          // using getRawContent().length() is not the same and can return 2Bytes smaller
          // size.
          response.setContentLength(cacheValue.getRawContent().getBytes(configuration.getEncoding()).length);
          IOUtils.write(cacheValue.getRawContent(), os, configuration.getEncoding());
        }
      }
    }
  }

  private CacheKey getSafeCacheKey(final HttpServletRequest request) {
    final CacheKey cacheKey = cacheKeyFactory.create(request);
    if (cacheKey == null) {
      throw new WroRuntimeException("Cannot build valid CacheKey from request: " + request.getRequestURI());
    }
    return cacheKey;
  }

  private boolean isGzipAllowed() {
    return context.getConfig().isGzipEnabled() && WroUtil.isGzipSupported(context.getRequest());
  }

  /**
   * Set the aggregatedFolderPath if required.
   */
  private void initAggregatedFolderPath(final HttpServletRequest request, final ResourceType type) {
    if (ResourceType.CSS == type && context.getAggregatedFolderPath() == null) {
      final String requestUri = request.getRequestURI();
      final String cssFolder = StringUtils.removeEnd(requestUri, FilenameUtils.getName(requestUri));
      final String aggregatedFolder = StringUtils.removeStart(cssFolder, request.getContextPath());
      LOG.debug("set aggregatedFolderPath: {}", aggregatedFolder);
      Context.get().setAggregatedFolderPath(aggregatedFolder);
    }
  }

}
