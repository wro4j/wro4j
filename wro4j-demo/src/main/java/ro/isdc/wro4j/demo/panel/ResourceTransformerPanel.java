/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro4j.demo.panel;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.JawrCssMinifierProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;


/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class ResourceTransformerPanel extends Panel {
  private String input;
  private String output;
  private ResourcePostProcessor processor;


  public ResourceTransformerPanel(final String id) {
    super(id);
    addComponents();
  }


  private void addComponents() {
    final Form<?> form = new Form<Void>("form");
    form.setOutputMarkupId(true);
    form.add(getProcessorSelect());
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
            //output = input.toUpperCase();
            output = writer.toString();
          }
          target.addComponent(form);
        } catch (final IOException e) {
          System.out.println("exception occured: " + e);
        }
      }
    });
    add(form);
  }


  /**
   * @return
   */
  private Component getProcessorSelect() {
    final List<? extends ResourcePostProcessor> list = Arrays.asList(new CssMinProcessor(), new JSMinProcessor(),
      new CssVariablesProcessor(), new JawrCssMinifierProcessor());
    final IChoiceRenderer<ResourcePostProcessor> renderer = new ChoiceRenderer<ResourcePostProcessor>() {
      @Override
      public Object getDisplayValue(final ResourcePostProcessor object) {
        return object.getClass().getSimpleName();
      }
    };
    final Component component = new DropDownChoice<ResourcePostProcessor>("selectProcessor", new PropertyModel<ResourcePostProcessor>(this, "processor"), list, renderer);
    return component;
  }
}
