/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ro.isdc.wro.resource.Resource;

/**
 * A group is an entity which gather a list of resources.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public final class Group {
  /**
   * Group name.
   */
  private String name;

  /**
   * Resources of the group.
   */
  private List<Resource> resources = new ArrayList<Resource>();

  /**
   * @return the name
   */
  public final String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public final void setName(final String name) {
    this.name = name;
  }

  /**
   * @return the resources
   */
  public final List<Resource> getResources() {
    return resources;
  }

  /**
   * @param resources
   *          the resources to set
   */
  public final void setResources(final List<Resource> resources) {
    this.resources = resources;
    for (final Resource resource : resources) {
      resource.setGroup(this);
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
  }

}
