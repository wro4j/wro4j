package ro.isdc.wro.http.support;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class TestContentTypeResolver {
  
  @Test
  public void shouldResolveCSSExtenstion() {
    assertThat(ContentTypeResolver.get("somefile.css"), is("text/css"));
  }
  
  @Test
  public void shouldResolveJPGExtenstion() {
    assertThat(ContentTypeResolver.get("s/bvews/omefile.jpg"), is("image/jpeg"));
  }

  @Test
   public void shouldResolveJPGExtenstionWithoutCharset() {
     assertThat(ContentTypeResolver.get("s/bvews/omefile.jpg", "UTF-8"), is("image/jpeg"));
   }
  
  @Test
  public void shouldResolveHTMLExtenstion() {
    assertThat(ContentTypeResolver.get("mefile.html"), is("text/html"));
  }

  @Test
  public void shouldResolveHTMLExtenstionWitCharset() {
    assertThat(ContentTypeResolver.get("mefile.html", "UTF-8"), is("text/html; charset=UTF-8"));
  }
  
  @Test
  public void shouldResolveJSExtenstion() {
    assertThat(ContentTypeResolver.get("/ad/df/mefile.js"), is("application/javascript"));
  }
  
  @Test
  public void shouldResolveUnknownExtenstion() {
    assertThat(ContentTypeResolver.get("/ad/df/mefile.unknown"), is("application/octet-stream"));
  }

  @Test
  public void shouldOnlyUseLastDot() {
    assertThat(ContentTypeResolver.get("somefile.js.png"), is("image/png"));
  }

  @Test
   public void shouldResolveHTMLUpperCaseExtenstion() {
     assertThat(ContentTypeResolver.get("mefile.CSS"), is("text/css"));
   }
}
