/*
 * Copyright (C) 2009 Betfair.
 * All rights reserved.
 */
package ro.isdc.wro.http;

/**
 * Enumerates Http Headers used by wro4j.
 *
 * @author Alex Objelean
 */
public enum HttpHeader {
  CACHE_CONTROL("Cache-Control"), LAST_MODIFIED("Last-Modified"), ETAG("ETag"), EXPIRES("Expires"), IF_MODIFIED_SINCE(
    "If-Modified-Since"), IF_NONE_MATCH("If-None-Match"), CONTENT_ENCODING("Content-Encoding"), ACCEPT_ENCODING(
    "Accept-Encoding");
  /**
   * HTTP header as string.
   */
	private String name;

	/**
	 * @param name string representation of the header.
	 */
	private HttpHeader(final String name) {
		this.name = name;
	}

	/**
	 * Use this method instead of name() to get the header name.
	 * @return the name of the header.
	 */
	@Override
	public String toString() {
		return name;
	}
}
