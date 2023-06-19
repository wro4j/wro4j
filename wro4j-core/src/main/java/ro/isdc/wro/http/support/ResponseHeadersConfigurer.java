package ro.isdc.wro.http.support;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.util.WroUtil;


/**
 * Responsible for configuring response headers. The configured headers contains the default headers overridden by those
 * set through "headers" property.
 *
 * @author Alex Objelean
 * @since 1.4.9
 */
public class ResponseHeadersConfigurer {
  private static final Logger LOG = LoggerFactory.getLogger(ResponseHeadersConfigurer.class);

  /**
   * String representation of headers to set. Each header is separated by a | character.
   */
  private final String headersAsString;
  private final Long lastModifiedTimestamp = initTimestamp();

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
   * Factory method which creates a {@link ResponseHeadersConfigurer} containing no headers set.
   */
  public static ResponseHeadersConfigurer emptyHeaders() {
    return new ResponseHeadersConfigurer();
  }

  /**
   * Factory method which creates a {@link ResponseHeadersConfigurer} containing headers used to disable cache in debug
   * mode.
   */
  public static ResponseHeadersConfigurer fromConfig(final WroConfiguration config) {
    return new ResponseHeadersConfigurer(config.getHeader()) {
      @Override
      public void configureDefaultHeaders(final Map<String, String> map) {
        if (config.isCacheHttpEnabled()) {
          if (config.isDebug()) {
            // prevent caching when in development mode
            addNoCacheHeaders(map);
          } else {
            final Calendar cal = Calendar.getInstance();
            cal.roll(Calendar.YEAR, 1);
            map.put(HttpHeader.CACHE_CONTROL.toString(), config.getCacheHttpValue());
            map.put(HttpHeader.EXPIRES.toString(), WroUtil.toDateAsString(cal.getTimeInMillis()));
            // TODO probably this is not a good idea to set this field which will have a
            // different value when there will be
            // more than one instance of wro4j.
            map.put(HttpHeader.LAST_MODIFIED.toString(), WroUtil.toDateAsString(getLastModifiedTimestamp()));
          }
        }
      };
    };
  }

  /**
   * Factory method which creates a {@link ResponseHeadersConfigurer} containing headers provided as string (separated
   * by | character).
   */
  public static ResponseHeadersConfigurer withHeadersSet(final String headersAsString) {
    return new ResponseHeadersConfigurer(headersAsString);
  }

  public ResponseHeadersConfigurer() {
    this(null);
  }

  /**
   * @param headersAsString
   *          string representation of the headers to add separated by | character.
   */
  public ResponseHeadersConfigurer(final String headersAsString) {
    this.headersAsString = headersAsString;
    headersMap.clear();
    initHeaderValues();
  }

  /**
   * Initialize header values.
   */
  private void initHeaderValues() {
    configureDefaultHeaders(headersMap);
    configureFromHeadersAsString();
    LOG.debug("Header Values: {}", headersMap);
  }

  private void configureFromHeadersAsString() {
    if (!StringUtils.isEmpty(headersAsString)) {
      try {
        if (headersAsString.contains("|")) {
          final String[] headerAsArray = headersAsString.split("[|]");
          for (final String header : headerAsArray) {
            parseHeader(header);
          }
        } else {
          parseHeader(headersAsString);
        }
      } catch (final Exception e) {
        throw new WroRuntimeException("Invalid header init-param value: " + headersAsString
            + ". A correct value should have the following format: "
            + "<HEADER_NAME1>: <VALUE1> | <HEADER_NAME2>: <VALUE2>. " + "Ex: <look like this: "
            + "Expires: Thu, 15 Apr 2010 20:00:00 GMT | Cache-Control: public", e);
      }
    }
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
   * @VisibleForTesting
   */
  final Map<String, String> getHeadersMap() {
    return Collections.unmodifiableMap(headersMap);
  }

  /**
   * @return the timestamp of the last modification.
   */
  public Long getLastModifiedTimestamp() {
    return lastModifiedTimestamp;
  }

  /**
   * @return the timestamp value milliseconds stripped. Strip operation is important, because when timestamp is
   *         extracted from response header, it ends with 000 (milliseconds are not applied).
   */
  private long initTimestamp() {
    long timestamp = new Date().getTime();
    timestamp = timestamp - (timestamp % 1000);
    return timestamp;
  }
}
