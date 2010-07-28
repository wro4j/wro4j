/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro4j.demo.panel;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssMinProcessor;


/**
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class ResourceTransformerPanel extends Panel {
  private String input;
  private String output;


  public ResourceTransformerPanel(final String id) {
    super(id);
    addComponents();
  }


  private void addComponents() {
    final Form<?> form = new Form<Void>("form");
    form.setOutputMarkupId(true);
    form.add(new TextArea<String>("input", new PropertyModel<String>(this, "input")));
    form.add(new TextArea<String>("output", new PropertyModel<String>(this, "output")));
    form.add(new AjaxButton("transform") {
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
        try {
          output = null;
          if (input != null) {
            final ResourcePostProcessor processor = new CssMinProcessor();
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
}
