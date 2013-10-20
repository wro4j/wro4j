package ro.isdc.wro.model.resource.processor.support;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.LessCssImportPreProcessor;


/**
 * @author Alex Objelean
 */
public class TestDefaultProcessorProvider {
  private DefaultProcessorProvider victim;

  @Before
  public void setUp() {
    victim = new DefaultProcessorProvider();
  }

  @Test
  public void shouldContainLessCssImportPreProcessor()
      throws Exception {
    assertProccessorWithAliasAvailable(LessCssImportPreProcessor.class, LessCssImportPreProcessor.ALIAS);
  }

  @Test
  public void shouldHaveSinglineStripperProvided() {
    assertProccessorWithAliasAvailable(SingleLineCommentStripperProcessor.class, SingleLineCommentStripperProcessor.ALIAS);
  }

  private void assertProccessorWithAliasAvailable(final Class<?> processorClass, final String alias) {
    final Map<String, ResourcePreProcessor> map = victim.providePreProcessors();
    final Class<?> actual = map.get(alias).getClass();
    assertEquals(processorClass, actual);
  }
}
