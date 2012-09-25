/*
 * Copyright (C) 2009. All rights reserved.
 */
package ro.isdc.wro.http.support;

/**
 * Enumerates Http Headers used by wro4j.
 * 
 * @author Alex Objelean
 */
public enum HttpHeader {
  CACHE_CONTROL("Cache-Control"),
  LAST_MODIFIED("Last-Modified"),
  ETAG("ETag"),
  EXPIRES("Expires"),
  IF_MODIFIED_SINCE("If-Modified-Since"),
  IF_NONE_MATCH("If-None-Match"),
  CONTENT_ENCODING("Content-Encoding"),
  PRAGMA("Pragma");
  /**
   * HTTP header as string.
   */
  private String name;
  
  /**
   * @param name
   *          string representation of the header.
   */
  private HttpHeader(final String name) {
    this.name = name;
  }
  
  /**
   * @return lower-case string representation of the header.
   */
  public String getHeaderName() {
    return this.name.toLowerCase();
  }
  
  /**
   * Use this method instead of name() to get the header name.
   * 
   * @return the name of the header.
   */
  @Override
  public String toString() {
    return name;
  }
}
