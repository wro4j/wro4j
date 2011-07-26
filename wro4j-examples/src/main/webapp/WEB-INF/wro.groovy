groups {
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
    js("/resource/redirect.js")
    js("/external/resource.js")
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
    css("/static/css/NOTEXIST.css")
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
  encoding {
    js("https://raw.github.com/yui/yui3/3.3.0/build/text/text-data-wordbreak.js")
  }
  "jquery-mobile" {
    css("http://code.jquery.com/mobile/1.0a2/jquery.mobile-1.0a2.css")
    js("http://code.jquery.com/mobile/1.0a1/jquery.mobile-1.0a1.js")
  }
  jquery {
    js("http://code.jquery.com/jquery-1.5.js")
    groupRef("jquery-mobile")
  }
  syntaxHighlighter {
    js("/static/module/syntaxHighlighter/scripts/shCore.js")
    js("/static/module/syntaxHighlighter/scripts/bootstrap.js")
    js("/static/module/syntaxHighlighter/scripts/*.js")
    css("/static/module/syntaxHighlighter/styles/shCoreRDark.css")
  }
  all {
    jquery()
    /*
    twitterBar()
    dynamicResource()
    uniform()
    groupRef('WEBINF-css')
    dwr()
    */
  }
}
