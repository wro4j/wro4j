/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;

/**
 * The resource model encapsulates the information about all existing groups.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public final class WroModel {
  /**
   * List of groups.
   */
  private Set<Group> groups = new HashSet<Group>();

  /**
   * @return the groups
   */
  public final Set<Group> getGroups() {
    return groups;
  }

  /**
   * @param groups
   *          the groups to set
   */
  public final void setGroups(final Set<Group> groups) {
    this.groups = groups;
  }

  /**
   * @param name
   *          of group to find.
   * @return group with searched name.
   * @throws runtime
   *           exception if group is not found.
   */
  public Group getGroupByName(final String name) {
    for (final Group group : groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }
    throw new InvalidGroupNameException("There is no such group: '" + name + "'. Available groups are: " + groups);
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
