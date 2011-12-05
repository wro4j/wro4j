groups {
  /*
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
  dynamicResource {
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
  wildcard {
    css("/static/**.css")
    js("/static/js/*.js")
    css("/WEB-INF/css/*.css")
    css("classpath:ro/isdc/wro/resources/**.cs?")
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
    dynamicResource()
    uniform()
    groupRef("jquery-mobile")
    groupRef('WEBINF-css')
  }
  */
  all {
    css("/static/**.css")
    /*
    css("/static/css/css1.css")
    css("/static/css/css2.css")
    /*css("classpath:ro/isdc/wro/resources/*.css")*/
  }
}
