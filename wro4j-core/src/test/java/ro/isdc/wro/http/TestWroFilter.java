/**
 *
 */
package ro.isdc.wro.http;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.exception.WroRuntimeException;

/**
 * TestWroFilter.java.
 *
 * @author alexandru.objelean
 * @created Created on Jul 13, 2009
 */
public class TestWroFilter {
	private WroFilter filter;
	@Before
	public void initFilter() throws Exception {
		filter = new WroFilter();
		final FilterConfig config = Mockito.mock(FilterConfig.class);
		filter.init(config);
	}

  @Test(expected=WroRuntimeException.class)
  public void cannotProcessInvalidUri() throws Exception {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final FilterChain chain = Mockito.mock(FilterChain.class);

    Mockito.when(request.getRequestURI()).thenReturn("");
    filter.doFilter(request, response, chain);
  }

//  @Test
//  public void test() throws Exception {
//  	final String packageName = this.getClass().getPackage().getName().replace('.', '/');
//  	System.out.println(this.getClass().getPackage().getName());
//  	IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("ro/isdc/wro/manager/wro.xml"), System.out);
//  	filter = new WroFilter() {
//  		@Override
//  		protected WroManagerFactory getWroManagerFactory() {
//  			return new ServletContextAwareWroManagerFactory() {
//  				@Override
//  				protected WroModelFactory newModelFactory() {
//  					return new XmlModelFactory() {
//  	  	      @Override
//  	  	      protected InputStream getConfigResourceAsStream() {
//  	  	      	return TestWroFilter.class.getResourceAsStream("wro.xml");
//  	  	      }
//  					};
//  				}
//  	    };
//  		}
//  	};
//    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
//    final FilterChain chain = Mockito.mock(FilterChain.class);
//    final FilterConfig config = Mockito.mock(FilterConfig.class);
//  	filter.init(config);
//
//
//    Mockito.when(request.getRequestURI()).thenReturn("/caca/maca.js");
//    filter.doFilter(request, response, chain);
//  }
}
