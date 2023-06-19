package ro.isdc.wro.maven.plugin.support;

import static org.apache.commons.lang3.Validate.isTrue;

import org.apache.maven.plugin.logging.Log;

import ro.isdc.wro.model.resource.Resource;


/**
 * Responsible for logging progress related details. Useful to find the best balance between "to much" vs "none"
 * details. This implementation will indicate the state of the progress after a given period of time (ex: each 5
 * seconds).
 *
 * @author Alex Objelean
 * @since 1.7.0
 */
public class ProgressIndicator {
  private static final int DEFAULT_TIMEOUT_DELAY = 5000;
  /**
   * Counts total number of processed resources.
   */
  private int totalResources = 0;
  /**
   * Counts total number of resources with errors.
   */
  private int totalResourcesWithErrors = 0;
  /**
   * Counts total number of jshint errors.
   */
  private int totalFoundErrors = 0;
  /**
   * Number of milliseconds indicating how often an info log will be invoked.
   */
  private long timeoutDelay = DEFAULT_TIMEOUT_DELAY;
  private long lastInvocationTimestamp;
  private final Log log;

  public ProgressIndicator(final Log log) {
    this.log = log;
    updateLastInvocation();
  }

  private void updateLastInvocation() {
    lastInvocationTimestamp = System.currentTimeMillis();
  }

  /**
   * resets all counters to zero.
   */
  public void reset() {
    totalFoundErrors = 0;
    totalResources = 0;
    totalResourcesWithErrors = 0;
  }

  /**
   * Logs the summary as it was collected at this point.
   */
  public void logSummary() {
    final String message = totalFoundErrors == 0 ? "No lint errors found." : String.format(
        "Found %s errors in %s files.", totalFoundErrors, totalResourcesWithErrors);
    log.info("----------------------------------------");
    log.info(String.format("Total resources: %s", totalResources));
    log.info(message);
    log.info("----------------------------------------\n");
  }

  /**
   * A method which should be invoked on each new resource processing, having as a side effect an increment of the
   * counter holding the number of total processed resources.
   */
  public synchronized void onProcessingResource(final Resource resource) {
    totalResources++;
    log.debug("processing resource: " + resource.getUri());
    if (isLogRequired()) {
      log.info("Processed until now: " + getTotalResources() + ". Last processed: " + resource.getUri());
      updateLastInvocation();
    }
  }

  private boolean isLogRequired() {
    return System.currentTimeMillis() - lastInvocationTimestamp > timeoutDelay;
  }

  /**
   * This method has a side effect of incrementing the number of resources containing errors.
   *
   * @param errorsToAdd
   *          number of errors found during processing. This number will be added to the counter holding total number of
   *          found errors.
   */
  public void addFoundErrors(final int errorsToAdd) {
    isTrue(errorsToAdd > 0, "Cannot add negative number of errors");
    totalResourcesWithErrors++;
    totalFoundErrors += errorsToAdd;
  }

  /**
   * @return a total number of found errors.
   */
  public final int getTotalFoundErrors() {
    return totalFoundErrors;
  }

  final int getTotalResources() {
    return totalResources;
  }

  final int getTotalResourcesWithErrors() {
    return totalResourcesWithErrors;
  }

  /**
   * @param timeoutDelay
   *          Number of milliseconds to not log info to avoid verbose logging. Useful when running the plugin in large
   *          projects.
   */
  final void setTimeoutDelay(final long timeoutDelay) {
    this.timeoutDelay = timeoutDelay;
  }
}
