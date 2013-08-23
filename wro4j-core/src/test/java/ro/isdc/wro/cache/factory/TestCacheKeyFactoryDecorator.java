package ro.isdc.wro.cache.factory;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


/**
 * @author Alex Objelean
 */
public class TestCacheKeyFactoryDecorator {
  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private CacheKeyFactory mockCacheKeyFactory;
  private CacheKeyFactoryDecorator victim;

  @Before
  public void setUp() {
    initMocks(this);
    victim = new CacheKeyFactoryDecorator(mockCacheKeyFactory);
  }

  @Test(expected = NullPointerException.class)
  public void cannotDecorateNullObject() {
    new CacheKeyFactoryDecorator(null);
  }

  @Test
  public void shouldReturnCorrectDecorateObject() {
    assertSame(mockCacheKeyFactory, victim.getDecoratedObject());
  }

  @Test
  public void shouldInvokeCreateOnDecoratedObject() {
    victim.create(mockRequest);
    verify(mockCacheKeyFactory).create(mockRequest);
  }
}
