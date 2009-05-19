/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.ContextHolder;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.processor.GroupsProcessor;
import ro.isdc.wro.processor.UriProcessor;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * WroManager. Contains all the factories used by optimizer in order to perform the logic.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public final class WroManager {
  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(WroManager.class);

  /**
   * Dynamic parameters regexp pattern
   */
  private static Pattern paramPattern = Pattern.compile("\\$\\{(.+?)\\}");

  /**
   * ResourcesModel factory.
   */
  private WroModelFactory modelFactory;

  /**
   * UriProcessor.
   */
  private UriProcessor uriProcessor;

  /**
   * UriLocatorFactory.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Groups processor.
   */
  private GroupsProcessor groupsProcessor;

  /**
   * A cacheStrategy used for caching processed results. Key: uri, Value: processed result.
   */
  private CacheStrategy<String, String> cacheStrategy;

  /**
   * Default constructor.
   */
  public WroManager() {
  }

  /**
   * Perform processing of the uri and depending on it will return {@link WroProcessResult} object containing
   * the input stream of the requested resource and it's content type.
   *
   * @param uri
   * @return
   */
  public WroProcessResult process(final String uri) {
    log.debug("<process>");
    validate();
    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    // find names & type
    final List<String> groupNames = uriProcessor.getGroupNames(uri);
    final ResourceType type = uriProcessor.getResourceType(uri);

    // create model & find groups
    final WroModel model = modelFactory.getInstance(uriLocatorFactory);
    final List<Group> groups = model.getGroupsByNames(groupNames);

    String processedResult = null;
    if (WroSettings.getConfiguration().isDeployment()) {
      // Cache based on uri
      processedResult = cacheStrategy.get(uri);
      if (processedResult == null) {
        // process groups & put update cache
        processedResult = groupsProcessor.process(groups, type);
        cacheStrategy.put(uri, processedResult);
      }
    } else {
      processedResult = groupsProcessor.process(groups, type);
    }

    final WroProcessResult result = new WroProcessResult();
    result.setContentType(getContentType(type));

    // replace expressions before writing to stream
    String exprReplaced = replaceExpressions(processedResult);

    result.setInputStream(new ByteArrayInputStream(exprReplaced.getBytes()));

    stopWatch.stop();
    log.info("WroManage process time: " + stopWatch.toString());

    log.debug("</process>");
    return result;
  }

  /**
   * Try to replace the params expressions with suitable values.
   *
   * @param configStream
   * @return
   * @throws IOException
   */
  private String replaceExpressions(String resource) {
    StringBuilder builder = new StringBuilder(resource);
    Matcher matcher = null;

    // process config
    matcher = paramPattern.matcher(builder.toString());

    // look up all the parameters
    while (matcher.find()) {
      String parameter = matcher.group(1);

      // replace the param place holder with the value
      String replaced = matcher.replaceFirst(acquireParamValue(parameter));
      builder.replace(0, builder.length() - 1, replaced);
      matcher = paramPattern.matcher(builder.toString());
    }

    return builder.toString();
  }

  /**
   * Get the parameters from request or session.
   *
   * @param parameter
   * @return
   */
  private String acquireParamValue(String parameter) {
    HttpServletRequest request = ContextHolder.REQUEST_HOLDER.get();

    // attempt to get the parameter from the request first
    String paramValue = request.getParameter(parameter);

    if (paramValue == null) {
      // not a request param, now attempt to get it from session
      paramValue = (String) request.getSession().getAttribute(parameter);
    }

    // as last resort will set the value as the parameter name
    if (paramValue == null) {
      paramValue = parameter;
    }

    return paramValue;
  }

  /**
   * @param type {@link ResourceType} object.
   * @return content type depending on resourceType.
   */
  private String getContentType(final ResourceType type) {
    String contentType = null;
    if (ResourceType.CSS == type) {
      contentType = "text/css";
    } else if (ResourceType.JS == type) {
      contentType = "text/javascript";
    }
    return contentType;
  }

  /**
   * Check if all dependencies are set.
   */
  private void validate() {
    try {
      if (this.uriProcessor == null) {
        throw new IllegalStateException("UriProcessor was not set!");
      }
      if (this.modelFactory == null) {
        throw new IllegalStateException("ModelFactory was not set!");
      }
      if (this.groupsProcessor == null) {
        throw new IllegalStateException("GroupProcessor was not set!");
      }
      if (this.uriLocatorFactory == null) {
        throw new IllegalStateException("uriLocatorFactory was not set!");
      }
      if (this.cacheStrategy == null) {
        throw new IllegalStateException("cacheStrategy was not set!");
      }
    } catch (final Exception e) {
      throw new WroRuntimeException(e);
    }
  }

  /**
   * @param uriProcessor the uriProcessor to set
   */
  public final void setUriProcessor(final UriProcessor uriProcessor) {
    this.uriProcessor = uriProcessor;
  }

  /**
   * @param groupsProcessor the groupsProcessor to set
   */
  public final void setGroupsProcessor(final GroupsProcessor groupsProcessor) {
    this.groupsProcessor = groupsProcessor;
  }

  /**
   * @param modelFactory the modelFactory to set
   */
  public final void setModelFactory(final WroModelFactory modelFactory) {
    this.modelFactory = modelFactory;
  }

  /**
   * @param uriLocatorFactory the resourceLocatorFactory to set
   */
  public final void setUriLocatorFactory(final UriLocatorFactory uriLocatorFactory) {
    this.uriLocatorFactory = uriLocatorFactory;
  }

  /**
   * @param cacheStrategy the cache to set
   */
  public final void setCacheStrategy(final CacheStrategy<String, String> cacheStrategy) {
    this.cacheStrategy = cacheStrategy;
  }

  /**
   * @return the uriLocatorFactory
   */
  public final UriLocatorFactory getUriLocatorFactory() {
    return uriLocatorFactory;
  }
}
