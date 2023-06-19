package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;


/**
 * Helper for querying a snapshot of a model. Any changes of the model performed after model inspector instantiation
 * will not be reflected.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class WroModelInspector {
  private final Map<String, Group> map = new HashMap<String, Group>();
  public WroModelInspector(final WroModel model) {
    Validate.notNull(model);
    for (final Group group : model.getGroups()) {
      map.put(group.getName(), group);
    }
  }

  /**
   * @param name
   *          of group to find.
   * @return group with searched name (if available) or null otherwise.
   *
   */
  public Group getGroupByName(final String name) {
    return map.get(name);
  }

  /**
   * @param resourceUri
   *          the {@link Resource} to search in all available groups.
   * @return t collection of group names containing provided resource. If the resource is not availalbe, an empty collection
   *         will be returned.
   */
  public Collection<String> getGroupNamesContainingResource(final String resourceUri) {
    Validate.notNull(resourceUri);
    final Set<String> groupNames = new TreeSet<String>();
    for (final Group group : map.values()) {
      if (group.hasResource(resourceUri)) {
        groupNames.add(group.getName());
      }
    }
    return groupNames;
  }

  /**
   * @return a set of group names.
   */
  public final List<String> getGroupNames() {
    //decorate with tree set to keep them sorted (for jdk5).
    return new ArrayList<String>(new TreeSet<String>(map.keySet()));
  }

  /**
   * @param groupName the nam of the group to check.
   * @return true if the provided groupName is available.
   */
  public boolean hasGroup(final String groupName) {
    return map.containsKey(groupName);
  }

  /**
   * This implementation would be simpler if java would have closures :).
   *
   * @return a comma separated list of group names.
   */
  public String getGroupNamesAsString() {
    return String.format("%s", StringUtils.join(getGroupNames(), ", "));
  }

  /**
   * @return the set of all unique resources from all the groups of the model (no particular order).
   */
  public Collection<Resource> getAllUniqueResources() {
    return collectResources(new HashSet<Resource>());
  }

  /**
   * @return the set of all resources from all the groups of the model (no particular order).
   */
  public Collection<Resource> getAllResources() {
    return collectResources(new ArrayList<Resource>());
  }

  private Collection<Resource> collectResources(final Collection<Resource> resources) {
    for (final Group group : map.values()) {
      resources.addAll(group.getResources());
    }
    return resources;
  }
}
