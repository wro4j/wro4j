package ro.isdc.wro.model.resource.locator;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.manager.factory.standalone.StandaloneContext;


/**
 * @author Alex Objelean
 */
public class TestStandaloneServletContextUriLocator {
  private StandaloneServletContextUriLocator victim;
  private StandaloneContext standaloneContext;

  @Before
  public void setUp() {
    standaloneContext = new StandaloneContext();
    final String contextFolder = TestStandaloneServletContextUriLocator.class.getResource("").getFile();
    standaloneContext.setContextFoldersAsCSV(contextFolder);
    victim = new StandaloneServletContextUriLocator();
    victim.initialize(standaloneContext);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailWhenStandaloneContextIsNotInitialized() throws Exception {
    victim = new StandaloneServletContextUriLocator();
    victim.locate("");
  }

  @Test(expected = IOException.class)
  public void cannotLocateInvalidResource()
      throws Exception {
    victim.locate("invalid");
  }

  @Test
  public void shouldLocateValidResource()
      throws Exception {
    final String validResource = TestStandaloneServletContextUriLocator.class.getSimpleName() + ".class";
    assertNotNull(victim.locate(validResource));
  }

  @Test
  public void shouldLocateValidResourceWhenMultipleContextFoldersProvided()
      throws Exception {
    final String defaultContextFolder = standaloneContext.getContextFoldersAsCSV();
    standaloneContext.setContextFoldersAsCSV("invalid," + defaultContextFolder);
    final String validResource = TestStandaloneServletContextUriLocator.class.getSimpleName() + ".class";
    assertNotNull(victim.locate(validResource));
  }

  @Test(expected = IOException.class)
  public void cannotLocateInvalidResourceWhenMultipleContextFoldersProvided()
      throws Exception {
    final String defaultContextFolder = standaloneContext.getContextFoldersAsCSV();
    standaloneContext.setContextFoldersAsCSV("invalid," + defaultContextFolder);
    assertNotNull(victim.locate("invalid"));
  }
}
