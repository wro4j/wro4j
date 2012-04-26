/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
   * Set of groups.
   */
  private Set<Group> groups = new HashSet<Group>();

  /**
   * @return a readonly collection of groups.
   */
  public final Collection<Group> getGroups() {
    return Collections.unmodifiableSet(groups);
  }
  
  /**
   * @return a readonly collection of groups which were used for processing (used flag marked as true).
   */
  public final Collection<Group> getUsedGroups() {
    Set<Group> usedGroups = new HashSet<Group>();
    for (Group group : groups) {
      if (group.isUsed()) {
        usedGroups.add(group);
      }
    }
    return Collections.unmodifiableSet(usedGroups);
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
  public final WroModel setGroups(final Collection<Group> groups) {
    Validate.notNull(groups, "groups cannot be null!");
    LOG.debug("setGroups: {}", groups);
    identifyDuplicateGroupNames(groups);
    this.groups = new HashSet<Group>(groups);
    return this;
  }


  /**
   * Identify duplicate group names.
   *
   * @param groups a collection of group to validate.
   */
  private void identifyDuplicateGroupNames(final Collection<Group> groups) {
    LOG.debug("identifyDuplicateGroupNames");
    final List<String> groupNames = new ArrayList<String>();
    for (final Group group : groups) {
      if (groupNames.contains(group.getName())) {
        throw new WroRuntimeException("Duplicate group name detected: " + group.getName());
      }
      groupNames.add(group.getName());
    }
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
    throw new InvalidGroupNameException(String.format("There is no such group: '%s'. Available groups are: %s", name,
        getGroupNames(groups)));
  }

  /**
   * This implementation would be simpler if java would have closures :).
   *
   * @param groups
   *          a collection of groups to get as string.
   * @return a comma separated list of group names.
   */
  private String getGroupNames(final Collection<Group> groups) {
    final Set<String> groupNames = new HashSet<String>();
    for (final Group group : groups) {
      groupNames.add(group.getName());
    }
    return String.format("[%s]", StringUtils.join(groupNames, ", "));
  }

/**
   * Merge this model with another model. This is useful for supporting model imports.
   *
   * @param importedModel model to import.
   */
  public void merge(final WroModel importedModel) {
    Validate.notNull(importedModel, "imported model cannot be null!");
    LOG.debug("merging importedModel: {}", importedModel);
    for (final String groupName : importedModel.getGroupNames()) {
      if (getGroupNames().contains(groupName)) {
        throw new WroRuntimeException("Duplicate group name detected: " + groupName);
      }
      addGroup(importedModel.getGroupByName(groupName));
    }
  }

  /**
   * Add a single group to the model.
   * @param group a not null {@link Group}.
   */
  public WroModel addGroup(final Group group) {
    Validate.notNull(group);
    groups.add(group);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, true);
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
