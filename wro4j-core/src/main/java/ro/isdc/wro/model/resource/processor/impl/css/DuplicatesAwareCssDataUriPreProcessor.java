/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Similar to {@link CssDataUriPreProcessor} which instead of replacing a url blindly with dataUri, is is smart enough to
 * detect duplicated image url and avoid replacing it with dataUri.
 *
 * @author Alex Objelean
 */
public class DuplicatesAwareCssDataUriPreProcessor
  extends CssDataUriPreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(DuplicatesAwareCssDataUriPreProcessor.class);
  public static final String ALIAS_DUPLICATE = "duplicateAwareCssDataUri";
  private final List<String> imageUrls = new ArrayList<String>();

  /**
   * Replace provided url with the new url if needed.
   *
   * @param imageUrl to replace.
   * @param cssUri Uri of the parsed css.
   * @return replaced url.
   */
  @Override
  protected final String replaceImageUrl(final String cssUri, final String imageUrl) {
    if (!imageUrls.contains(imageUrl)) {
      imageUrls.add(imageUrl);
      return super.replaceImageUrl(cssUri, imageUrl);
    }
    LOG.debug("duplicate Image url detected: '{}', skipping dataUri replacement", imageUrl);
    return imageUrl;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onProcessCompleted() {
    imageUrls.clear();
  }
}
