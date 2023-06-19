/*
 * Copyright (c) 2009.
 */
package ro.isdc.wro.model.group;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * Default implementation capable of extracting a single group from the request.
 *
 * @author Alex Objelean
 */
public class DefaultGroupExtractor
  implements GroupExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultGroupExtractor.class);
  /**
   * The name of the attribute where the servlet path is stored when requestDispatcher.include is called.
   */
  public static final String ATTR_INCLUDE_PATH = "javax.servlet.include.servlet_path";
  /**
   * The name of the parameter used to decide if the group must be minimized.
   */
  public static final String PARAM_MINIMIZE = "minimize";

  /**
   * {@inheritDoc}
   */
  public String getGroupName(final HttpServletRequest request) {
    Validate.notNull(request);
    String uri = request.getRequestURI();
    // check if include or uri path are present and use one of these as request uri.
    final String includeUriPath = (String) request.getAttribute(ATTR_INCLUDE_PATH);
    uri = includeUriPath != null ? includeUriPath : uri;
    final String groupName = FilenameUtils.getBaseName(stripSessionID(uri));
    return StringUtils.isEmpty(groupName) ? null : groupName;
  }

  /**
   * Extracts the resource type, by parsing the uri and finds the extension. If extension is valid ('css' or 'js'),
   * returns corresponding ResourceType, otherwise throws exception.
   * <p>
   * Valid examples of uri are: <code>/context/somePath/test.js</code> or <code>/context/somePath/test.css</code>
   * {@inheritDoc}
   */
  public ResourceType getResourceType(final HttpServletRequest request) {
    Validate.notNull(request);
    final String uri = request.getRequestURI();
    Validate.notNull(uri);
    ResourceType type = null;
    try {
      type = ResourceType.get(FilenameUtils.getExtension(stripSessionID(uri)));
    } catch (final IllegalArgumentException e) {
      LOG.debug("[FAIL] Cannot identify resourceType for uri: {}", uri);
    }
    return type;
  }

  /**
   * The uri is cleaned up (the ;jsessionID is removed).
   * @return the extension of the resource.
   */
  private String stripSessionID(final String uri) {
    return uri.replaceFirst("(?i)(;jsessionid.*)", "");
  }

  /**
   * {@inheritDoc}
   */
  public String encodeGroupUrl(final String groupName, final ResourceType resourceType, final boolean minimize) {
    return String.format("%s.%s?" + PARAM_MINIMIZE + "=%s", groupName, resourceType.name().toLowerCase(), minimize);
  }

  /**
   * The minimization is can be switched off only in debug mode.
   *
   * @return false if the request contains parameter {@link DefaultGroupExtractor#PARAM_MINIMIZE} with value false,
   *         otherwise returns true.
   */
  public boolean isMinimized(final HttpServletRequest request) {
    Validate.notNull(request);
    final String minimizeAsString = request.getParameter(PARAM_MINIMIZE);
    return !(Context.get().getConfig().isDebug() && "false".equalsIgnoreCase(minimizeAsString));
  }
}
