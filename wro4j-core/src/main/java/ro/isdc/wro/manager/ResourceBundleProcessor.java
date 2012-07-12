package ro.isdc.wro.manager;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheEntry;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.ContentHashEntry;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.HttpHeader;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.util.WroUtil;


/**
 * Encapsulates the bundle creation.
 * 
 * @author Alex Objelean
 * @created 18 Jun 2012
 * @since 1.4.7
 */
public class ResourceBundleProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleProcessor.class);
  /**
   * A cacheStrategy used for caching processed results. <GroupName, processed result>.
   */
  @Inject
  private CacheStrategy<CacheEntry, ContentHashEntry> cacheStrategy;
  @Inject
  private WroConfiguration config;
  @Inject
  private GroupExtractor groupExtractor;
  
  private boolean isGzipAllowed() {
    return config.isGzipEnabled() && isGzipSupported();
  }
  
  /**
   * Write to stream the content of the processed resource bundle.
   */
  public void serveProcessedBundle()
      throws IOException {
    final Context context = Context.get();
    final WroConfiguration configuration = context.getConfig();
    
    final HttpServletRequest request = context.getRequest();
    final HttpServletResponse response = context.getResponse();
    
    OutputStream os = null;
    try {
      // find names & type
      final ResourceType type = groupExtractor.getResourceType(request);
      final String groupName = groupExtractor.getGroupName(request);
      final boolean minimize = groupExtractor.isMinimized(request);
      if (groupName == null || type == null) {
        throw new WroRuntimeException("No groups found for request: " + request.getRequestURI());
      }
      initAggregatedFolderPath(request, type);
      
      final CacheEntry cacheKey = new CacheEntry(groupName, type, minimize);
      final ContentHashEntry cacheValue = cacheStrategy.get(cacheKey);
      
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
      if (type != null) {
        response.setContentType(type.getContentType() + "; charset=" + configuration.getEncoding());
      }
      // set ETag header
      response.setHeader(HttpHeader.ETAG.toString(), etagValue);
      
      os = response.getOutputStream();
      if (cacheValue.getRawContent() != null) {
        // use gziped response if supported & Set content length based on gzip flag
        if (isGzipAllowed()) {
          response.setContentLength(cacheValue.getGzippedContent().length);
          // add gzip header and gzip response
          response.setHeader(HttpHeader.CONTENT_ENCODING.toString(), "gzip");
          response.setHeader("Vary", "Accept-Encoding");
          IOUtils.write(cacheValue.getGzippedContent(), os);
        } else {
          //using getRawContent().length() is not the same and can return 2Bytes smaller size.
          response.setContentLength(cacheValue.getRawContent().getBytes(configuration.getEncoding()).length);
          IOUtils.write(cacheValue.getRawContent(), os, configuration.getEncoding());
        }
      }
    } finally {
      if (os != null)
        IOUtils.closeQuietly(os);
    }
  }
  
  /**
   * @return true if Gzip is Supported
   */
  private boolean isGzipSupported() {
    return WroUtil.isGzipSupported(Context.get().getRequest());
  }
  
  /**
   * Set the aggregatedFolderPath if required.
   */
  private void initAggregatedFolderPath(final HttpServletRequest request, final ResourceType type) {
    if (ResourceType.CSS == type && Context.get().getAggregatedFolderPath() == null) {
      final String requestUri = request.getRequestURI();
      final String cssFolder = StringUtils.removeEnd(requestUri, FilenameUtils.getName(requestUri));
      final String aggregatedFolder = StringUtils.removeStart(cssFolder, request.getContextPath());
      Context.get().setAggregatedFolderPath(aggregatedFolder);
    }
  }
}
