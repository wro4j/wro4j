package ro.isdc.wro.http;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.mockito.Mockito.when;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

import javax.management.MBeanServer;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.support.ResourceAuthorizationManager;
import ro.isdc.wro.util.WroTestUtils;
import ro.isdc.wro.util.WroUtil;
import ro.isdc.wro.util.io.NullOutputStream;


public class JMHTest {
  private static final int REQ_COUNT = 10;
  private static final int WARMUP_COUNT = 10;


  @State(Benchmark)
  public static class BenchmarkState {
    @Mock
    private FilterConfig mockFilterConfig;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    @Mock
    private ServletContext mockServletContext;
    @Mock
    private WroManagerFactory mockManagerFactory;
    @Mock
    private ResourceAuthorizationManager mockAuthorizationManager;
    @Mock
    private UriLocatorFactory mockUriLocatorFactory;
    @Mock
    private MBeanServer mockMBeanServer;
    @Mock
    private UriLocator mockUriLocator;
    WroFilter victim;

    public BenchmarkState() {
      try {
        MockitoAnnotations.initMocks(this);

        Context.set(Context.standaloneContext());

        when(mockUriLocatorFactory.getInstance(Mockito.anyString())).thenReturn(mockUriLocator);
        when(mockUriLocator.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);
        when(mockUriLocatorFactory.locate(Mockito.anyString())).thenReturn(WroUtil.EMPTY_STREAM);

        when(mockRequest.getAttribute(Mockito.anyString())).thenReturn(null);
        final WroManagerFactory managerFactory = WroTestUtils.simpleManagerFactory();
        when(mockManagerFactory.create()).thenReturn(managerFactory.create());
        when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);
        when(mockResponse.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new NullOutputStream()));
        when(mockRequest.getRequestURI()).thenReturn("/wro/test.js");
        when(mockFilterConfig.getServletContext()).thenReturn(mockServletContext);

        victim = new WroFilter();
        final WroConfiguration configuration = new WroConfiguration();
        configuration.setJmxEnabled(false);
        victim.setConfiguration(configuration);
        victim.init(mockFilterConfig);
        victim.setWroManagerFactory(managerFactory);

      } catch (final Exception e) {
        throw WroRuntimeException.wrap(e);
      }
    }
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(AverageTime)
  @OutputTimeUnit(NANOSECONDS)
  public void avgBenchmark(final BenchmarkState state)
      throws Exception {
    state.victim.doFilter(state.mockRequest, state.mockResponse, state.mockFilterChain);
  }

  public static void main(final String[] args)
      throws Exception {
    final Options opt = new OptionsBuilder().include(".*" + JMHTest.class.getName() + ".*").warmupIterations(
        WARMUP_COUNT).measurementIterations(REQ_COUNT).threads(8).forks(1).build();
    new Runner(opt).run();
  }

}
