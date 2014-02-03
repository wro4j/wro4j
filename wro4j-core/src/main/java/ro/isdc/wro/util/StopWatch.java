/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.util;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;


/**
 * Simple stop watch, allowing for timing of a number of tasks, exposing total running time and running time for each
 * named task.
 *
 * <p>
 * Conceals use of <code>System.currentTimeMillis()</code>, improving the readability of application code and reducing
 * the likelihood of calculation errors.
 *
 * <p>
 * Note that this object is not designed to be thread-safe and does not use synchronization.
 *
 * <p>
 * This class is normally used to verify performance during proof-of-concepts and in development, rather than as part of
 * production applications.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since May 2, 2001
 */
public class StopWatch {

  /**
   * Identifier of this stop watch. Handy when we have output from multiple stop watches and need to distinguish between
   * them in log or console output.
   */
  private final String id;

  private boolean keepTaskList = true;

  /** List of TaskInfo objects */
  private final List taskList = new LinkedList();

  /** Start time of the current task */
  private long startTimeMillis;

  /** Is the stop watch currently running? */
  private boolean running;

  /** Name of the current task */
  private String currentTaskName;

  private TaskInfo lastTaskInfo;

  private int taskCount;

  /** Total running time */
  private long totalTimeMillis;


  /**
   * Construct a new stop watch. Does not start any task.
   */
  public StopWatch() {
    this.id = "";
  }


  /**
   * Construct a new stop watch with the given id. Does not start any task.
   *
   * @param id identifier for this stop watch. Handy when we have output from multiple stop watches and need to
   *        distinguish between them.
   */
  public StopWatch(final String id) {
    this.id = id;
  }


  /**
   * Determine whether the TaskInfo array is built over time. Set this to "false" when using a StopWatch for millions of
   * intervals, or the task info structure will consume excessive memory. Default is "true".
   */
  public void setKeepTaskList(final boolean keepTaskList) {
    this.keepTaskList = keepTaskList;
  }


  /**
   * Start an unnamed task. The results are undefined if {@link #stop()} or timing methods are called without invoking
   * this method.
   *
   * @see #stop()
   */
  public void start()
    throws IllegalStateException {
    start("");
  }


  /**
   * Start a named task. The results are undefined if {@link #stop()} or timing methods are called without invoking this
   * method.
   *
   * @param taskName the name of the task to start
   * @see #stop()
   */
  public void start(final String taskName)
    throws IllegalStateException {
    if (this.running) {
      throw new IllegalStateException("Can't start StopWatch: it's already running");
    }
    this.startTimeMillis = System.currentTimeMillis();
    this.running = true;
    this.currentTaskName = taskName;
  }


  /**
   * Stop the current task. The results are undefined if timing methods are called without invoking at least one pair
   * {@link #start()} / {@link #stop()} methods.
   *
   * @see #start()
   */
  public void stop()
    throws IllegalStateException {
    if (!this.running) {
      throw new IllegalStateException("Can't stop StopWatch: it's not running");
    }
    final long lastTime = System.currentTimeMillis() - this.startTimeMillis;
    this.totalTimeMillis += lastTime;
    this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
    if (this.keepTaskList) {
      this.taskList.add(lastTaskInfo);
    }
    ++this.taskCount;
    this.running = false;
    this.currentTaskName = null;
  }


  /**
   * Return whether the stop watch is currently running.
   */
  public boolean isRunning() {
    return this.running;
  }


  /**
   * Return the time taken by the last task.
   */
  public long getLastTaskTimeMillis()
    throws IllegalStateException {
    if (this.lastTaskInfo == null) {
      throw new IllegalStateException("No tests run: can't get last interval");
    }
    return this.lastTaskInfo.getTimeMillis();
  }


  /**
   * Return the total time in milliseconds for all tasks.
   */
  public long getTotalTimeMillis() {
    return totalTimeMillis;
  }


  /**
   * Return the total time in seconds for all tasks.
   */
  public double getTotalTimeSeconds() {
    return totalTimeMillis / 1000.0;
  }


  /**
   * Return the number of tasks timed.
   */
  public int getTaskCount() {
    return taskCount;
  }


  /**
   * Return an array of the data for tasks performed.
   */
  public TaskInfo[] getTaskInfo() {
    if (!this.keepTaskList) {
      throw new UnsupportedOperationException("Task info is not being kept!");
    }
    return (TaskInfo[])this.taskList.toArray(new TaskInfo[this.taskList.size()]);
  }


  /**
   * Return a short description of the total running time.
   */
  public String shortSummary() {
    return "StopWatch '" + this.id + "': running time (millis) = " + getTotalTimeMillis();
  }


  /**
   * Return a string with a table describing all tasks performed. For custom reporting, call getTaskInfo() and use the
   * task info directly.
   */
  public String prettyPrint() {
    final StringBuffer sb = new StringBuffer(shortSummary());
    sb.append('\n');
    if (!this.keepTaskList) {
      sb.append("No task info kept");
    } else {
      final TaskInfo[] tasks = getTaskInfo();
      sb.append("-----------------------------------------\n");
      sb.append("ms     %     Task name\n");
      sb.append("-----------------------------------------\n");
      final NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMinimumIntegerDigits(5);
      nf.setGroupingUsed(false);
      final NumberFormat pf = NumberFormat.getPercentInstance();
      pf.setMinimumIntegerDigits(3);
      pf.setGroupingUsed(false);
      for (final TaskInfo task : tasks) {
        sb.append(nf.format(task.getTimeMillis()) + "  ");
        final double totalTimeSeconds = getTotalTimeSeconds();
        final double percentage = totalTimeSeconds == 0 ? 0 : task.getTimeSeconds() / totalTimeSeconds;
        sb.append(pf.format(percentage) + "  ");
        sb.append(task.getTaskName() + "\n");
      }
    }
    return sb.toString();
  }

  /**
   * Inner class to hold data about one task executed within the stop watch.
   */
  public static class TaskInfo {

    private final String taskName;

    private final long timeMillis;


    private TaskInfo(final String taskName, final long timeMillis) {
      this.taskName = taskName;
      this.timeMillis = timeMillis;
    }


    /**
     * Return the name of this task.
     */
    public String getTaskName() {
      return taskName;
    }


    /**
     * Return the time in milliseconds this task took.
     */
    public long getTimeMillis() {
      return timeMillis;
    }


    /**
     * Return the time in seconds this task took.
     */
    public double getTimeSeconds() {
      return timeMillis / 1000.0;
    }
  }

}
