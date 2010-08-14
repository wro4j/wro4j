/*
 *  Copyright 2010.
 */
package ro.isdc.wro.extensions.processor.rhino;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import sun.org.mozilla.javascript.internal.ScriptableObject;



/**
 * This class is borrowed from Richard Nichols.
 *
 * @author Alex Objelean
 */
public class AbstractRhinoContext {

  private ScriptableObject scriptableObject;
  private ScriptEngine scriptEngine;

  public AbstractRhinoContext() {

    // create a script engine manager
    final ScriptEngineManager factory = new ScriptEngineManager();
    // create JavaScript engine
    scriptEngine = factory.getEngineByName("JavaScript");
  }

  /**
   * @return the scriptEngine
   */
  public ScriptEngine getScriptEngine() {
    return this.scriptEngine;
  }


  /**
   * @return the scriptableObject
   */
  public final ScriptableObject getScriptableObject() {
    return this.scriptableObject;
  }
}
