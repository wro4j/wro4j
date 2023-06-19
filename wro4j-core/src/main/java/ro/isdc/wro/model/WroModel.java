/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;

/**
 * The resource model encapsulates the information about all existing groups.
 *
 * @author Alex Objelean
 */
public final class WroModel {
  private static final Logger LOG = LoggerFactory.getLogger(WroModel.class);
  /**
   * Set of groups.
   */
  private Set<Group> groups = new TreeSet<Group>();

  /**
   * @return a readonly collection of groups.
   */
  public final Collection<Group> getGroups() {
    return Collections.unmodifiableSet(groups);
  }

  /**
   * @param groups
   *          the groups to set
   */
  public final WroModel setGroups(final Collection<Group> groups) {
    notNull(groups, "groups cannot be null!");
    LOG.debug("setGroups: {}", groups);
    identifyDuplicateGroupNames(groups);
    this.groups = new TreeSet<Group>(groups);
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
   * Merge this model with another model. This is useful for supporting model imports.
   *
   * @param importedModel model to import.
   */
  public void merge(final WroModel importedModel) {
    Validate.notNull(importedModel, "imported model cannot be null!");
    LOG.debug("merging importedModel: {}", importedModel);
    for (final String groupName : new WroModelInspector(importedModel).getGroupNames()) {
      if (new WroModelInspector(this).getGroupNames().contains(groupName)) {
        throw new WroRuntimeException("Duplicate group name detected: " + groupName);
      }
      final Group importedGroup = new WroModelInspector(importedModel).getGroupByName(groupName);
      addGroup(importedGroup);
    }
  }

  /**
   * Add a single group to the model.
   * @param group a not null {@link Group}.
   */
  public WroModel addGroup(final Group group) {
    notNull(group);
    groups.add(group);
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, true);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append(
        "groups", this.groups).toString();
  }
}
