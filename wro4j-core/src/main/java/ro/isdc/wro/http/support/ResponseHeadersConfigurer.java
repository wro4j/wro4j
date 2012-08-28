package ro.isdc.wro.http.support;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.WroUtil;


/**
 * Responsible for configuring response headers. The configured headers contains the default headers overridden by those
 * set through "headers" property.
 * 
 * @author Alex Objelean
 * @since 1.4.9
 * @created 27 Aug 2012
 */
public class ResponseHeadersConfigurer {
  private static final Logger LOG = LoggerFactory.getLogger(ResponseHeadersConfigurer.class);
  /**
   * Default value used by Cache-control header.
   */
  private static final String DEFAULT_CACHE_CONTROL_VALUE = "public, max-age=315360000";
  /**
   * String representation of headers to set. Each header is separated by a | character.
   */
  private String headers;
  /**
   * Flag for debug mode
   */
  private boolean debug;
  
  /**
   * Map containing header values used to control caching. The keys from this values are trimmed and lower-cased when
   * put, in order to avoid duplicate keys. This is done, because according to RFC 2616 Message Headers field names are
   * case-insensitive.
   */
  @SuppressWarnings("serial")
  private final Map<String, String> headersMap = new LinkedHashMap<String, String>() {
    @Override
    public String put(final String key, final String value) {
      return super.put(key.trim().toLowerCase(), value);
    }

    @Override
    public String get(final Object key) {
      return super.get(((String) key).toLowerCase());
    }
  };
  
  /**
   * Factory method which creates a {@link ResponseHeadersConfigurer} containing headers used to disable cache.
   */
  public static ResponseHeadersConfigurer noCache() {
    return new ResponseHeadersConfigurer() {
      @Override
      public void configureDefaultHeaders(final Map<String, String> map) {
        addNoCacheHeaders(map);
      }
    };
  }
  
  /**
   * Factory method which creates a {@link ResponseHeadersConfigurer} containing headers used to disable cache.
   */
  public static ResponseHeadersConfigurer emptyHeaders() {
    return new ResponseHeadersConfigurer() {
      @Override
      public void configureDefaultHeaders(final Map<String, String> map) {
      }
    };
  }

  public ResponseHeadersConfigurer() {
    reset();
  }

  /**
   * Initialize header values.
   */
  private void initHeaderValues() {
    configureDefaultHeaders(headersMap);
    if (!StringUtils.isEmpty(headers)) {
      try {
        if (headers.contains("|")) {
          final String[] headerAsArray = headers.split("[|]");
          for (final String header : headerAsArray) {
            parseHeader(header);
          }
        } else {
          parseHeader(headers);
        }
      } catch (final Exception e) {
        throw new WroRuntimeException("Invalid header init-param value: " + headers
            + ". A correct value should have the following format: "
            + "<HEADER_NAME1>: <VALUE1> | <HEADER_NAME2>: <VALUE2>. " + "Ex: <look like this: "
            + "Expires: Thu, 15 Apr 2010 20:00:00 GMT | cache-control: public", e);
      }
    }
    LOG.debug("Header Values: {}", headersMap);
  }

  /**
   * Parse header value & puts the found values in headersMap field.
   * 
   * @param header
   *          value to parse.
   */
  private void parseHeader(final String header) {
    LOG.debug("parseHeader: {}", header);
    final String headerName = header.substring(0, header.indexOf(":"));
    if (!headersMap.containsKey(headerName)) {
      final String value = header.substring(header.indexOf(":") + 1);
      headersMap.put(headerName, StringUtils.trim(value));
    }
  }

  /**
   * Allow configuration of default headers. This is useful when you need to set custom expires headers.
   * 
   * @param map
   *          the {@link Map} where key represents the header name, and value - header value.
   */
  public void configureDefaultHeaders(final Map<String, String> map) {
    // put defaults
    if (debug) {
      // prevent caching when in development mode
      addNoCacheHeaders(map);
    } else {
      final Long timestamp = new Date().getTime();
      final Calendar cal = Calendar.getInstance();
      cal.roll(Calendar.YEAR, 1);
      map.put(HttpHeader.CACHE_CONTROL.toString(), DEFAULT_CACHE_CONTROL_VALUE);
      map.put(HttpHeader.LAST_MODIFIED.toString(), WroUtil.toDateAsString(timestamp));
      map.put(HttpHeader.EXPIRES.toString(), WroUtil.toDateAsString(cal.getTimeInMillis()));
    }
  }

  /**
   * Populates the map with headers used to disable cache.
   */
  private static void addNoCacheHeaders(final Map<String, String> map) {
    map.put(HttpHeader.PRAGMA.toString(), "no-cache");
    map.put(HttpHeader.CACHE_CONTROL.toString(), "no-cache");
    map.put(HttpHeader.EXPIRES.toString(), "0");
  }

  /**
   * Reset the configured headers and compute them again based on default headers and those set by "headers" property.
   */
  public final void reset() {
    init(new HashMap<String, String>());
  }
  
  private void init(final Map<String, String> map) {
    Validate.notNull(map);
    headersMap.clear();
    headersMap.putAll(map);
    initHeaderValues();
  }

  /**
   * Method called for each request and responsible for setting response headers, used mostly for cache control.
   * Override this method if you want to change the way headers are set.<br>
   * 
   * @param response
   *          {@link HttpServletResponse} object.
   */
  public void setHeaders(final HttpServletResponse response) {
    // Force resource caching as best as possible
    for (final Map.Entry<String, String> entry : headersMap.entrySet()) {
      response.setHeader(entry.getKey(), entry.getValue());
    }
  }

  /**
   * @param headers
   *          String representation of headers to set. Each header is separated by | character.
   */
  public ResponseHeadersConfigurer setHeaders(final String headers) {
    this.headers = headers;
    return this;
  }

  /**
   * @param debug flag for debug mode. When this flag is true, the no cache headers will be set.
   */
  public ResponseHeadersConfigurer setDebug(final boolean debug) {
    this.debug = debug;
    return this;
  }
  
  /**
   * @VisibleForTesting
   */
  final Map<String, String> getHeadersMap() {
    return Collections.unmodifiableMap(headersMap);
  }
}
