package ro.isdc.wro.http.support;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class TestContentTypeResolver {

  @Test
  public void shouldResolveCSSExtenstion() {
    assertEquals("text/css", ContentTypeResolver.get("somefile.css"));
  }

  @Test
  public void shouldResolveJPGExtenstion() {
    assertEquals("image/jpeg", ContentTypeResolver.get("s/bvews/omefile.jpg"));
  }

  @Test
   public void shouldResolveJPGExtenstionWithoutCharset() {
    assertEquals("image/jpeg", ContentTypeResolver.get("s/bvews/omefile.jpg", "UTF-8"));
   }

  @Test
  public void shouldResolveHTMLExtenstion() {
    assertEquals("text/html", ContentTypeResolver.get("mefile.html"));
  }

  @Test
  public void shouldResolveHTMLExtenstionWitCharset() {
    assertEquals("text/html; charset=UTF-8", ContentTypeResolver.get("mefile.html", "UTF-8"));
  }

  @Test
  public void shouldResolveJSExtenstion() {
    assertEquals("application/javascript", ContentTypeResolver.get("/ad/df/mefile.js"));
  }

  @Test
  public void shouldResolveUnknownExtenstion() {
    assertEquals("application/octet-stream", ContentTypeResolver.get("/ad/df/mefile.unknown"));
  }

  @Test
  public void shouldOnlyUseLastDot() {
    assertEquals("image/png", ContentTypeResolver.get("somefile.js.png"));
  }

  @Test
   public void shouldResolveHTMLUpperCaseExtenstion() {
    assertEquals("text/css", ContentTypeResolver.get("mefile.CSS"));
   }

  @Test
  public void shouldResolveFontExtensionEot() {
    assertEquals("application/vnd.ms-fontobject", ContentTypeResolver.get("font.eot"));
  }

  @Test
  public void shouldResolveFontExtensionOtf() {
    assertEquals("application/x-font-opentype", ContentTypeResolver.get("font.otf"));
  }

  @Test
  public void shouldResolveFontExtensionTtf() {
    assertEquals("application/octet-stream", ContentTypeResolver.get("font.ttf"));
  }

  @Test
  public void shouldResolveSvg() {
    assertEquals("image/svg+xml", ContentTypeResolver.get("graphic.svg"));
  }
}
