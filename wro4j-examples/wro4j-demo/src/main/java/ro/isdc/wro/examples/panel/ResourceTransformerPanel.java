/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.examples.panel;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.css.RhinoLessCssProcessor;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.BeautifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.DojoShrinksafeCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.PackerJsProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.extensions.processor.js.YUIJsCompressorProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.CommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.MultiLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.SingleLineCommentStripperProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.ConformColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssCompressorProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.VariablizeColorsCssProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;

import com.google.javascript.jscomp.CompilationLevel;


/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class ResourceTransformerPanel extends Panel {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceTransformerPanel.class);
  private static final String VALUE_NOT_AVAILABLE = "N/A";
  private String input;
  private String output;
  private String compressionRate = VALUE_NOT_AVAILABLE;
  private String originalSize = VALUE_NOT_AVAILABLE;
  private String compressedSize = VALUE_NOT_AVAILABLE;
  private String processingTime = VALUE_NOT_AVAILABLE;
  private transient ResourcePostProcessor processor;


  public ResourceTransformerPanel(final String id) {
    super(id);
    addComponents();
  }


  private void addComponents() {
    final Form<?> form = new Form<Void>("form");
    form.setOutputMarkupId(true);
    form.add(getProcessorSelect());
    form.add(new Label("compressionRate", new PropertyModel<String>(this, "compressionRate")));
    form.add(new Label("originalSize", new PropertyModel<String>(this, "originalSize")));
    form.add(new Label("compressedSize", new PropertyModel<String>(this, "compressedSize")));
    form.add(new Label("processingTime", new PropertyModel<Long>(this, "processingTime")));
    form.add(new TextArea<String>("input", new PropertyModel<String>(this, "input")));
    form.add(new TextArea<String>("output", new PropertyModel<String>(this, "output")));
    form.add(getTransformButton());
    add(form);
  }


  private Component getTransformButton() {
    //IndicatingAjax
    final Component c = new Button("transform") {
//      @Override
//      protected void onError(final AjaxRequestTarget target, final Form<?> form) {
//        target.appendJavascript("alert('Unexpected error occured');");
//      }

      @Override
      public void onSubmit() {
        if (processor == null) {
          return;
        }
        try {
          compressionRate = VALUE_NOT_AVAILABLE;
          compressedSize = VALUE_NOT_AVAILABLE;
          originalSize = VALUE_NOT_AVAILABLE;
          final long processingTimeAsLong = new Date().getTime();
          output = null;
          if (input != null) {
            final Writer writer = new StringWriter();
            processor.process(new StringReader(input), writer);
            // output = input.toUpperCase();
            output = writer.toString();
            final DecimalFormat format = new DecimalFormat("0.00");
            if (input.length() != 0) {
              final double rate = 100 - output.length() * 100 / ((double)input.length());
              compressionRate = "" + format.format(rate);
            } else {
              compressionRate = "N/A";
            }
            originalSize = format.format((double)input.length()/1024);
            compressedSize = format.format((double)output.length()/1024);
            processingTime = String.valueOf(new Date().getTime() - processingTimeAsLong);
          }
//          target.addComponent(form);
        } catch (final Exception e) {
          info("Cannot process the input: " + e.getMessage() + "");
//          target.prependJavascript("alert('Cannot process the input: " + e.getMessage() + "');");
          LOG.error("exception occured: " + e);
        }
      }
    };
    return c;
  }

  /**
   * @return a list of resource post processors
   */
  private static List<? extends ResourcePostProcessor> getProcessors() {
    final List<ResourcePostProcessor> list = new ArrayList<ResourcePostProcessor>();
    //hardcode the list:
    if (true) {
      list.add(new CommentStripperProcessor());
      list.add(new MultiLineCommentStripperProcessor());
      list.add(new SingleLineCommentStripperProcessor());

      list.add(new ConformColorsCssProcessor());
      list.add(new CssVariablesProcessor());
      list.add(new VariablizeColorsCssProcessor());

      list.add(new RhinoLessCssProcessor());
      list.add(new SassCssProcessor());
      list.add(new YUICssCompressorProcessor());
      list.add(new JawrCssMinifierProcessor());
      list.add(new CssCompressorProcessor());

      list.add(new PackerJsProcessor());
      list.add(new BeautifyJsProcessor());
      list.add(new DojoShrinksafeCompressorProcessor());
      list.add(new UglifyJsProcessor());
      list.add(new JSMinProcessor());
      list.add(new GoogleClosureCompressorProcessor());
      list.add(new GoogleClosureCompressorProcessor(CompilationLevel.ADVANCED_OPTIMIZATIONS));
      list.add(YUIJsCompressorProcessor.doMungeCompressor());
      list.add(YUIJsCompressorProcessor.noMungeCompressor());
      return list;
    }
    //inspect the classpath
    try {
      final Class<?>[] classes = getClasses(WroRuntimeException.class.getPackage().getName());
      for (final Class<?> clazz : classes) {
        if (ResourcePostProcessor.class.isAssignableFrom(clazz)) {
          try {
            final ResourcePostProcessor processor = (ResourcePostProcessor)clazz.newInstance();
            list.add(processor);
          } catch (final Exception e) {
            LOG.warn("Could not instantiate class: " + clazz);
          }
        }
      }
      return list;
    } catch (final Exception e) {
      LOG.error("Exception occured", e);
      return Collections.EMPTY_LIST;
    }
  }


  private Component getProcessorSelect() {
    final IModel<List<? extends ResourcePostProcessor>> listModel = new LoadableDetachableModel<List<? extends ResourcePostProcessor>>() {
      @Override
      protected List<? extends ResourcePostProcessor> load() {
        return getProcessors();
      }
    };
    final IChoiceRenderer<ResourcePostProcessor> renderer = new ChoiceRenderer<ResourcePostProcessor>() {
      @Override
      public Object getDisplayValue(final ResourcePostProcessor object) {
        return object.getClass().getSimpleName();
      }
    };
    final Component component = new DropDownChoice<ResourcePostProcessor>(
      "selectProcessor", new PropertyModel<ResourcePostProcessor>(this, "processor"), listModel, renderer);
    return component;
  }


  /**
   * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
   *
   * @param packageName The base package
   * @return The classes
   * @throws ClassNotFoundException
   * @throws IOException
   */
  private static Class<?>[] getClasses(final String packageName)
    throws ClassNotFoundException, IOException {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assert classLoader != null;
    final String path = packageName.replace('.', '/');
    final Enumeration<URL> resources = classLoader.getResources(path);
    final List<File> dirs = new ArrayList<File>();
    while (resources.hasMoreElements()) {
      final URL resource = resources.nextElement();
      dirs.add(new File(resource.getFile()));
    }
    final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    for (final File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }
    return classes.toArray(new Class[classes.size()]);
  }


  /**
   * Recursive method used to find all classes in a given directory and sub-directories.
   *
   * @param directory The base directory
   * @param packageName The package name for classes found inside the base directory
   * @return The classes
   * @throws ClassNotFoundException
   */
  private static List<Class<?>> findClasses(final File directory, final String packageName)
    throws ClassNotFoundException {
    final List<Class<?>> classes = new ArrayList<Class<?>>();
    //does not play well with GAE
//    if (!directory.exists()) {
//      return classes;
//    }
    final File[] files = directory.listFiles();
    for (final File file : files) {
      if (file.isDirectory()) {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file, packageName + "." + file.getName()));
      } else if (file.getName().endsWith(".class")) {
        classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
      }
    }
    return classes;
  }

}
