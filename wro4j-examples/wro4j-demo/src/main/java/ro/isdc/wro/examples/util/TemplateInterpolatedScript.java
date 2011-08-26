package ro.isdc.wro.examples.util;

import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.apache.wicket.util.template.TextTemplate;


/**
 * An abstract class for all scripts to be interpolated from a template.
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public abstract class TemplateInterpolatedScript implements IHeaderContributor {
  /**
   * {@inheritDoc}
   */
  public void renderHead(final IHeaderResponse response) {
    try {
      final String scriptId = getScriptId();
      response.renderJavaScript(interpolateScript(), scriptId);
    } catch (final Exception e) {
      //should never happen
      throw new RuntimeException("Exception occured while rendering interpolated script", e);
    }
  }

  /**
   * @return a unique script id value.
   */
  protected String getScriptId() {
    return "script-" + Session.get().nextSequenceValue();
  }

  /**
   * Reads a script resource & interpolates it.
   * @return interpolated script.
   * @throws Exception if script cannot be found.
   */
  private String interpolateScript()
    throws Exception {
    final Map<String, Object> map = buildMap();
    final String content = getTemplate().asString(map);
    final String script = MapVariableInterpolator.interpolate(content, map);
    return script;
  }

  /**
   * @return a map with values used for script interpolation.
   */
  protected abstract Map<String, Object> buildMap();

  /**
   * @return {@link TextTemplate} of the script used for interpolation.
   */
  protected abstract TextTemplate getTemplate();
}