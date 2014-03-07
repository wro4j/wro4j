package ro.isdc.wro.integration;

import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class Issue853Test {

  private static Server server;
  private static int port;

  @BeforeClass
  public static void initTest() throws Exception {
    ServerSocket s = null;
    try {
      s = new ServerSocket(0);
      port = s.getLocalPort();
    } finally {
      s.close();
    }
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.WARN);

    server = new Server(port);
    final WebAppContext context = new WebAppContext();
    context.setDescriptor("src/test/resources/issue853/WEB-INF/web.xml");
    context.setResourceBase("src/test/resources/issue853/");
    context.setContextPath("/");
    context.setParentLoaderPriority(false);
    //context.setClassLoader(Thread.currentThread().getContextClassLoader());
    server.setHandler(context);
    server.start();

    System.out.println("Started server on port: " + port);
  }

  @Test
  public void testNormalJSP() throws IOException {
    final String contents = getURL(getBaseURL() + "example.jsp");

    assertFalse(contents.trim().isEmpty());
    assertFalse("The JSP should not have this token, as it should be processed by JSP", contents.contains("out.println"));
  }

  @Test
  public void testCSS() throws IOException {
    final String contents = getURL(getBaseURL() + "wro/all.css");

    assertFalse(contents.trim().isEmpty());
    assertFalse("The CSS file should not have this token, as the include should be processed by JSP", contents.contains("out.println"));
  }

  private String getURL(final String location) throws IOException {
    final URL url = new URL(location);

    BufferedReader in = null;
    final StringBuilder result = new StringBuilder();
    try {
      in = new BufferedReader(new InputStreamReader(url.openStream()));

      String line = null;
      while ((line = in.readLine()) != null) {
        result.append(line).append("\r\n");
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }

    return result.toString();
  }

  private String getBaseURL() {
    return "http://127.0.0.1:" + port + "/";
  }

  @AfterClass
  public static void closeTest() throws Exception {
    server.stop();
  }
}
