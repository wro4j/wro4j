package ro.isdc.wro.maven.plugin.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonatype.plexus.build.incremental.BuildContext;

import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestBuildContextHolder {
  private static final String KEY = "key";
  private static final String VALUE = "value";
  private BuildContextHolder victim;

  @Mock
  private BuildContext buildContext;

  @Before
  public void setUp() {
    victim = new BuildContextHolder();
    initMocks(this);
  }

  @After
  public void tearDown() {
    victim.destroy();
  }

  private void initVictimWithBuildContext() {
    victim = new BuildContextHolder(buildContext, null);
  }

  @Test
  public void shouldUseBuildContextIncrementalFlag() {
    initVictimWithBuildContext();
    when(buildContext.isIncremental()).thenReturn(false);
    assertFalse(victim.isIncrementalBuild());
  }

  @Test
  public void shouldInvokeSetValueOnBuildContext() {
    initVictimWithBuildContext();
    victim.setValue(KEY, VALUE);
    verify(buildContext).setValue(KEY, VALUE);
  }

  @Test
  public void shouldInvokeGetValueOnBuildContext() {
    initVictimWithBuildContext();
    victim.getValue(KEY);
    verify(buildContext).getValue(KEY);
  }

  @Test
  public void shouldDisableIncrementalBuildByDefault() {
    assertFalse(victim.isIncrementalBuild());
  }

  @Test
  public void shouldEnableIncrementalBuildWhenConfigured() {
    victim.setIncrementalBuildEnabled(true);
    assertTrue(victim.isIncrementalBuild());
  }

  @Test
  public void shouldLoadExistingDefaultValue()
      throws IOException {
    final File tempFile = WroUtil.createTempFile();
    try {
      final Properties props = new Properties();
      props.setProperty(KEY, VALUE);
      props.store(new FileOutputStream(tempFile), null);

      victim = new BuildContextHolder() {
        @Override
        File newFallbackStorageFile(final File rootFolder) {
          return tempFile;
        }
      };

      assertEquals(VALUE, victim.getValue(KEY));
    } finally {
      FileUtils.deleteQuietly(tempFile);
    }
  }

  @Test
  public void shouldPersisteSavedValue()
      throws Exception {
    victim.setValue(KEY, VALUE);
    victim.persist();
    final File storage = victim.getFallbackStorageFile();

    final Properties props = new Properties();
    props.load(new FileInputStream(storage));

    assertEquals(VALUE, props.getProperty(KEY));
  }

  @Test
  public void shouldNotHaveAnyValueByDefault() {
    assertNull(victim.getValue(KEY));
  }

  @Test
  public void shouldFindPreviouslyPersistedValue() {
    victim.setValue(KEY, VALUE);
    assertEquals(VALUE, victim.getValue(KEY));
  }

  @Test
  public void shouldRemoveWhenNullValueProvided() {
    victim.setValue(KEY, null);
    assertNull(victim.getValue(KEY));
  }

  @Test
  public void shouldNotStoreValueAssociatedWithNullKey() {
    victim.setValue(null, VALUE);
    assertNull(victim.getValue(null));
  }

  @Test
  public void shouldRememberStoredValueAfterPersist() throws Exception {
    final File tempFile = WroUtil.createTempFile();
    try {
      victim = new BuildContextHolder();
      victim.setValue(KEY, VALUE);
      victim.persist();

      final BuildContextHolder secondVictim = new BuildContextHolder() {
        @Override
        File newFallbackStorageFile(final File rootFolder) {
          return victim.getFallbackStorageFile();
        };
      };

      assertEquals(VALUE, secondVictim.getValue(KEY));
    } finally {
      FileUtils.deleteQuietly(tempFile);
    }
  }

  @Test
  public void shouldForgetStoredValueIfPersistNotInvoked() {
    final File tempFile = WroUtil.createTempFile();
    try {
      victim = new BuildContextHolder();
      victim.setValue(KEY, VALUE);

      final BuildContextHolder secondVictim = new BuildContextHolder() {
        @Override
        File newFallbackStorageFile(final File rootFolder) {
          return victim.getFallbackStorageFile();
        };
      };

      assertEquals(null, secondVictim.getValue(KEY));
    } finally {
      FileUtils.deleteQuietly(tempFile);
    }
  }
}
