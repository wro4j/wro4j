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
import java.util.Enumeration;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
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
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;


/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class ResourceTransformerPanel extends Panel {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ResourceTransformerPanel.class);

  private String input;
  private String output;
  private String compressionRate = "N/A";
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
    form.add(new TextArea<String>("input", new PropertyModel<String>(this, "input")));
    form.add(new TextArea<String>("output", new PropertyModel<String>(this, "output")));
    form.add(new AjaxButton("transform") {
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
        try {
          output = null;
          if (input != null) {
            final Writer writer = new StringWriter();
            processor.process(new StringReader(input), writer);
            // output = input.toUpperCase();
            output = writer.toString();
            if (input.length() != 0) {
              final double rate = output.length() * 100 / ((double)input.length());
              final DecimalFormat format = new DecimalFormat("0.00");
              compressionRate = "" + format.format(rate);
            } else {
              compressionRate = "N/A";
            }
          }
          target.addComponent(form);
        } catch (final IOException e) {
          LOG.error("exception occured: " + e);
        }
      }
    });
    add(form);
  }

  public static void main(final String[] args) {
    final double rate = (357 * 100000000) / 999d;
    final DecimalFormat format = new DecimalFormat("0.00");
    System.out.println(rate);
    System.out.println(format.format(rate));
  }

  /**
   * @return
   */
  private static List<? extends ResourcePostProcessor> getProcessors() {
    final List<ResourcePostProcessor> list = new ArrayList<ResourcePostProcessor>();
    try {
      final Class[] classes = getClasses(WroRuntimeException.class.getPackage().getName());
      for (final Class clazz : classes) {
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
  private static Class[] getClasses(final String packageName)
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
    final ArrayList<Class> classes = new ArrayList<Class>();
    for (final File directory : dirs) {
      classes.addAll(findClasses(directory, packageName));
    }
    return classes.toArray(new Class[classes.size()]);
  }


  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory The base directory
   * @param packageName The package name for classes found inside the base directory
   * @return The classes
   * @throws ClassNotFoundException
   */
  private static List<Class> findClasses(final File directory, final String packageName)
    throws ClassNotFoundException {
    final List<Class> classes = new ArrayList<Class>();
    if (!directory.exists()) {
      return classes;
    }
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
