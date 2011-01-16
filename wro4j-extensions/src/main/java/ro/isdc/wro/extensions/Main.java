/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


/**
 * @author Alex Objelean
 */
public class Main {
  public static class MyOptions {
    @Option(name="-minimize")
    private boolean minimize;

    /**
     * @return the minimize
     */
    public boolean isMinimize() {
      return this.minimize;
    }

    /**
     * @param minimize the minimize to set
     */
    public void setMinimize(final boolean minimize) {
      this.minimize = minimize;
    }
  }
  public static void main(final String[] args1) throws Exception {
    final InputStreamReader converter = new InputStreamReader(System.in);
    final BufferedReader in = new BufferedReader(converter);
    final String[] args = new String[] { in.readLine() };

    final MyOptions bean = new MyOptions();
    final CmdLineParser parser = new CmdLineParser(bean);
    try {
      parser.parseArgument(args);
    } catch (final CmdLineException e) {
      System.err.println(e.getMessage());
      System.err.println("java -jar myprogram.jar [options...] arguments...");
      parser.printUsage(System.err);
      return;
    }
  }
}
