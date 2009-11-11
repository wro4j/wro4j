/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ro.isdc.wro.exception.WroRuntimeException;

/**
 * The resource model encapsulates the information about all existing groups.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Oct 30, 2008
 */
public final class WroModel {
  /**
   * List of groups.
   */
  private List<Group> groups = new ArrayList<Group>();

  /**
   * @return the groups
   */
  public final List<Group> getGroups() {
    return groups;
  }

  /**
   * @param groups
   *          the groups to set
   */
  public final void setGroups(final List<Group> groups) {
    this.groups = groups;
  }

  /**
   * @param groupNames
   */
  public List<Group> getGroupsByNames(final List<String> names) {
    final List<Group> groups = new ArrayList<Group>();
    for (final String name : names) {
      final Group group = getGroupByName(name);
      if (group != null) {
        groups.add(group);
      }
    }
    return groups;
  }

  /**
   * @param name
   *          of group to find.
   * @return group with searched name.
   * @throws runtime
   *           exception if group is not found.
   */
  private Group getGroupByName(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null!");
    }
    for (final Group group : groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }
    throw new WroRuntimeException("There is no such group: " + name);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append(
        "groups", this.groups).toString();
  }
}
