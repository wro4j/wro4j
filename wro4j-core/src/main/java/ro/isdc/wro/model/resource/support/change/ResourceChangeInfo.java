package ro.isdc.wro.model.resource.support.change;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds details about hashes of watched resources and the group which were detected as changed.
 */
public final class ResourceChangeInfo {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceChangeInfo.class);
  /**
   * The hash of the resource retrieving during current request cycle.
   */
  private String currentHash;
  /**
   * Persisted hash of the resource retrieved during previous request cycle.
   */
  private String prevHash;
  /**
   * A set of groups for which changes were detected. This set is required to handle the situations when the same
   * resource is contained in two different groups and only the first group is notified about the change.
   */
  private final Set<String> groups = Collections.synchronizedSet(new HashSet<String>());

  /**
   * Updates the hash associated with the resource for a give groupName.
   *
   * @param hash
   *          the most recent computed hash.
   * @param groupName
   *          name of the group for which the change of resource is detected.
   */
  public void updateHashForGroup(final String hash, final String groupName) {
    notNull(groupName);
    this.currentHash = hash;
    if (isChangedHash()) {
      LOG.debug("Group {} has changed", groupName);
      //remove all persisted groups. Starting over..
      groups.clear();
    }
  }

  private boolean isChangedHash() {
    return currentHash != null ? !currentHash.equals(prevHash) : prevHash != null;
  }

  /**
   * Resets the state of this {@link ResourceChangeInfo} by copying the current hash to previous hash and reseting
   * current hash to null. This operation should be invoked after a change cycle completes and a new one is prepared.
   */
  public void reset() {
    // This is important to avoid false positives when reset is called concurrently.
    if (currentHash != null) {
      this.prevHash = currentHash;
    }
    this.currentHash = null;
  }

  /**
   * @param groupName
   *          associated with the current change check.
   * @return true if the change is detected for provided group.
   */
  public boolean isChanged(final String groupName) {
    notNull(groupName);
    final boolean result = isChangedHash() ? true : !groups.contains(groupName);
    if (result) {
      groups.add(groupName);
    }
    return result;
  }

  /**
   * Checks if the current hash was set previously and persist the groupName in the set of the groups who are interested
   * about the change.
   *
   * @param groupName
   *          the name of the group which contains the resource for which check is performed.
   * @return true if the change is required.
   */
  public boolean isCheckRequiredForGroup(final String groupName) {
    notNull(groupName);
    return currentHash == null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}