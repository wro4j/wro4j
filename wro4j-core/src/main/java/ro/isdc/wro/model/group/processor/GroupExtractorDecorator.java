/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.group.processor;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * A simple decorator for GroupsProcessor.
 *
 * @author Alex Objelean
 */
public class GroupExtractorDecorator
  implements GroupExtractor {
  /**
   * {@link GroupExtractor} to decorate.
   */
  private final GroupExtractor decorated;


  /**
   * Decorates a {@link GroupExtractor} implementation.
   *
   * @param decorated {@link GroupExtractor} to decorate.
   */
  public GroupExtractorDecorator(final GroupExtractor decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }


  /**
   * {@inheritDoc}
   */
  public String getGroupName(final HttpServletRequest request) {
    return decorated.getGroupName(request);
  }


  /**
   * {@inheritDoc}
   */
  public ResourceType getResourceType(final HttpServletRequest request) {
    return decorated.getResourceType(request);
  }


  /**
   * {@inheritDoc}
   */
  public boolean isMinimized(final HttpServletRequest request) {
    return decorated.isMinimized(request);
  }

  /**
   * {@inheritDoc}
   */
  public String encodeGroupUrl(final String groupName, final ResourceType resourceType, final boolean minimize) {
    return decorated.encodeGroupUrl(groupName, resourceType, minimize);
  }
}
