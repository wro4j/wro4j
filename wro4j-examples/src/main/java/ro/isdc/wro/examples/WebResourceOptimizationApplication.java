package ro.isdc.wro.examples;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.session.ISessionStore;

import ro.isdc.wro.examples.page.HomePage;


/**
 * Application object for your web application. If you want to run this application without deploying, run the Start
 * class.
 *
 * @see wicket.myproject.Start#main(String[])
 */
public class WebResourceOptimizationApplication
    extends WebApplication {
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
    if (isDeploy()) {
      getResourceSettings().setResourcePollFrequency(null);
    }
    //settings
    getMarkupSettings().setStripWicketTags(true);
    getMarkupSettings().setStripComments(true);
    getMarkupSettings().setCompressWhitespace(true);
    getPageSettings().setAutomaticMultiWindowSupport(false);
    getDebugSettings().setDevelopmentUtilitiesEnabled(true);
//    setPageManagerProvider(IPageManagerProvider)

    //mounts
    mountBookmarkablePage("/home", HomePage.class);
    //getRootRequestMapper().mapHandler(new BookmarkablePageRequestHandler(new PageProvider(HomePage.class)));
//    getRootRequestMapper().mapHandler(new RenderPageRequestHandler(new PageProvider(HomePage.class)));

  }


  public static WebResourceOptimizationApplication get() {
    return (WebResourceOptimizationApplication) Application.get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getConfigurationType() {
    return isDeploy() ? DEPLOYMENT: DEVELOPMENT;
  }

  /**
   * @return true if application is to be deployed on GAE.
   */
  public boolean isDeploy() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ISessionStore newSessionStore() {
    //wouldn't work with SecondLevelCacheSessionStore on GAE
    return new HttpSessionStore(this);
  }
}

