package ro.isdc.wro.examples;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.MountedMapper;

import ro.isdc.wro.examples.page.HomePage;


/**
 * Application object for your web application. If you want to run this application without deploying, run the Start
 * class.
 *
 * @see wicket.myproject.Start#main(String[])
 */
public class WebResourceOptimizationApplication
    extends WebApplication {
  private final boolean deploy = false;

  /**
   * @see wicket.Application#getHomePage()
   */
  @Override
  public Class<? extends WebPage> getHomePage() {
    return HomePage.class;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void init() {
    // for Google App Engine
    if (deploy) {
      getResourceSettings().setResourcePollFrequency(null);
    }
    //settings
    getMarkupSettings().setStripWicketTags(true);
    getMarkupSettings().setStripComments(true);
    getMarkupSettings().setCompressWhitespace(true);
    getPageSettings().setAutomaticMultiWindowSupport(false);
    getDebugSettings().setDevelopmentUtilitiesEnabled(true);

    //mounts
    getRootRequestMapperAsCompound().add(new MountedMapper("/home", HomePage.class));
    //getRootRequestMapper().mapHandler(new BookmarkablePageRequestHandler(new PageProvider(HomePage.class)));
//    getRootRequestMapper().mapHandler(new RenderPageRequestHandler(new PageProvider(HomePage.class)));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getConfigurationType() {
    if (deploy) {
      return DEPLOYMENT;
    }
    return DEVELOPMENT;
  }
}

