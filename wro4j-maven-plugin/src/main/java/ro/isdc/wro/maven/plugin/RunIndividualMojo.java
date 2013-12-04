package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.group.processor.PreProcessorExecutor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.util.DestroyableLazyInitializer;


/**
 * Responsible for applying processors on each resources from the model individually and put the result content in a
 * predefined location (could also replace the original file with the minimized one).
 *
 * @author Alex Objelean
 * @since 1.7.3
 * @created 4 Dec 2013
 */
public class RunIndividualMojo
    extends AbstractWro4jMojo {
  private final DestroyableLazyInitializer<PreProcessorExecutor> preProcessorExecutorRef = new DestroyableLazyInitializer<PreProcessorExecutor>() {
    @Override
    protected PreProcessorExecutor initialize() {
      final Injector injector = InjectorBuilder.create(getManagerFactory()).build();
      final PreProcessorExecutor executor = new PreProcessorExecutor();
      injector.inject(executor);
      return executor;
    }
    @Override
    public void destroy() {
      if (isInitialized()) {
        get().destroy();
      }
      super.destroy();
    };
  };

  @Override
  protected void doExecute()
      throws Exception {
    try {
      final Collection<String> groupsAsList = getTargetGroupsAsList();

      for (final String groupName : groupsAsList) {
        final WroModel model = getManagerFactory().create().getModelFactory().create();
        final WroModelInspector modelInspector = new WroModelInspector(model);
        final Group group = modelInspector.getGroupByName(groupName);
        final List<Resource> resources = group.getResources();
        for (final Resource resource : resources) {
          processResource(resource);
        }
      }
    } finally {
      preProcessorExecutorRef.destroy();
    }
  }

  private void processResource(final Resource resource) {
    final File destination = computeDestination(resource);
    if (destination != null) {
      try {
        final String result = preProcessorExecutorRef.get().processAndMerge(Arrays.asList(resource), true);
        destination.delete();
        destination.createNewFile();
        final OutputStream outputStream = new FileOutputStream(destination);
        IOUtils.write(result, outputStream);
        IOUtils.closeQuietly(outputStream);
      } catch (final IOException e) {

      }
    }
  }

  /**
   * Compute the destination file, which will contain the processed content of the provided resource. If null is
   * returned, the resource processing will be skipped.
   */
  private File computeDestination(final Resource resource) {
    return null;
  }
}
