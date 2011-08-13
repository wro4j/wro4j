groups {
  g1 {
    js("/static/app.js")
    css("/static/app.css")
  }
  g2 {
    js(minimize: true, "classpath:com/application/static/app.js")
    css(minimize: false, "http://www.site.com/static/app.css")
  }
}