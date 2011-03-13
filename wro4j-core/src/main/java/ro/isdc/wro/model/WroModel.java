/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;

/**
 * The resource model encapsulates the information about all existing groups.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public final class WroModel {
  private static final Logger LOG = LoggerFactory.getLogger(WroModel.class);
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
   * @return a set of group names.
   */
  public final List<String> getGroupNames() {
    final List<String> groupNames = new ArrayList<String>();
    for (final Group group : getGroups()) {
      groupNames.add(group.getName());
    }
    return groupNames;
  }

  /**
   * @param groups
   *          the groups to set
   */
  public final void setGroups(final Set<Group> groups) {
    this.groups = groups;
    // TODO validate the model and ensure there is only one group with the same name.
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
   * Merge this model with another model. This is useful for supporting model imports.
   *
   * @param importedModel model to import.
   */
  public void merge(final WroModel importedModel) {
    if (importedModel == null) {
      throw new IllegalArgumentException("imported model cannot be null!");
    }
    LOG.debug("merging importedModel: " + importedModel);
    for (final String groupName : importedModel.getGroupNames()) {
      if (getGroupNames().contains(groupName)) {
        throw new WroRuntimeException("Duplicate group name detected: " + groupName);
      }
      getGroups().add(importedModel.getGroupByName(groupName));
    }
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
