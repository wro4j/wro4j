groups {

  dynamic {
    js("/resource/dynamic.js")
  }
  dynamicRandom {
    css("/resource/random.css")
  }
  
  testPage {
    modules()
  }
  
  modules {
    dynamicRandom()  
    font()
    wildcard()
    moduleImport()
    /*
    jar()
    */
  }
  
  moduleImport {
      css("/module/import/parent.css")
  }
  
  wildcard {
   
    css("/static/**.css")
   /*
    js("/static/js/*.js")
    */
    css("/WEB-INF/css/*.css")
    css("classpath:ro/isdc/wro/resources/**.cs?")
  }
  font {
    css("/module/font/**.css")
  }
  
  encoding {
    js("/static/js/encoding/*.js")
  }
  bom {
    js("/static/js/bom/*.js")
  }
  
  skeleton {
    css("/static/css/skeleton/*.css")
  }
  classpath {
    css("classpath:com/mysite/myapplication/web/resources/grids.css")
    css("classpath:com/mysite/myapplication/web/resources/*.css")
    js("classpath:com/mysite/myapplication/web/resources/*.js")
  }
  jsp {
    css("/WEB-INF/jsp/css.jsp")
  }
  placeholder {
    css("/static/css/placeholder/*.css")
  }
  problem {
    js("/static/js/problem.js")
  }
  invalid {
    js("/invalid/resource.js")
  }
  twitterBar {
    js("http://widgets.twimg.com/j/2/widget.js")
  }
  "WEBINF-css" {
    css("/WEB-INF/css/webinf.css")
  }
  uniform {
    css("/static/css/webapp.css")
    css("/static/css/css1.css")
    css("/static/css/css2.css")
    css("classpath:ro/isdc/wro/resources/*.css")
  }
  otherGroup {
    css("/static/css/variablesSupport.css")
  }
  wicket {
    js("classpath:org/apache/wicket/ajax/wicket-ajax-debug.js")
  }
  dwr {
    js("/dwr/engine.js")
    js("/dwr/interface/DWRFacade.js")
  }
  chineseEncoding {
    js("http://wro4j.googlecode.com/svn/wiki/static/encoding/chinese.js")
  }
  "jquery-mobile" {
    groupRef("jquery")
    css("http://code.jquery.com/mobile/1.0a2/jquery.mobile-1.0a2.css")
    js("http://code.jquery.com/mobile/1.0a1/jquery.mobile-1.0a1.js")
  }
  jquery {
    js("http://code.jquery.com/jquery-1.6.js")
  }
  all {
    uniform()
    groupRef("jquery-mobile")
    groupRef('WEBINF-css')
  }
  test {
    css("/static/css/webapp.css")
  }
  redirect {
    js("/resource/redirect.js")
  }
  dispatch {
    js("/resource/dispatch.js")
  }
  external {
    js("/external/any.js")
  }
  
  
  jar {
    css("classpath:com/app/**.css")
  }
  coffeeScript {
     js("/static/coffee/**.coffee")
  }
  
  cssUrlRewriting {
    css("/css/index.css");
  }
  bootstrap {
    css("/bootstrap/less/bootstrap.less")
    js("/bootstrap/js/*.js");
  }
  googleCodePrettify {
    css("/module/bootstrapDemo/google-code-prettify/*.css")
    js("/module/bootstrapDemo/google-code-prettify/*.js")
  }
  bootstrapDemo {
    js("/module/bootstrapDemo/jquery.js")
    bootstrap()
    googleCodePrettify()
  }
  missingResources {
    dynamic()
    css("/some/invalid.css")
    js("/some/invalid.css")
  }
}