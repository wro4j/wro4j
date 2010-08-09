package ro.isdc.wro.examples.page;


import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

import ro.isdc.wro.examples.panel.TwitterBar;


/**
 * Homepage
 */
public class AbstractBasePage extends WebPage {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor that is invoked when page is invoked without a session.
   *
   * @param parameters Page parameters
   */
  public AbstractBasePage(final PageParameters parameters) {
    add(newSidebar("sidebar"));
  }

  private Component newSidebar(final String id) {
    return new TwitterBar(id);
  }
}
