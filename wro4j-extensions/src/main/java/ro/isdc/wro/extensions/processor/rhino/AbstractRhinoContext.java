/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;


/**
 * This class is borrowed from Richard Nichols.
 *
 * @author Alex Objelean
 */
public class AbstractRhinoContext {

  private final ContextFactory cf;
  private final Context context;
  private final ScriptableObject scriptableObject;


  public AbstractRhinoContext() {
    cf = new ContextFactory();
    context = cf.enter();
    scriptableObject = context.initStandardObjects();
    context.setOptimizationLevel(9);
  }


  protected final String removeNewLines(final String data) {
    //replace("\"", "\\\"").
    return data.replace("\n", "\\n").replace("\r", "\\r");
  }

  /**
   * @return the context
   */
  public final Context getContext() {
    return this.context;
  }

  /**
   * @return the scriptableObject
   */
  public final ScriptableObject getScriptableObject() {
    return this.scriptableObject;
  }
}
