package wro4j.grails.plugin

class WroTagLib {
  static namespace = 'wro'

  def css = { attrs ->
    def group = attrs.group
    out << """<link rel="stylesheet" type="text/css" href="${g.resource(dir: '/')}wro/${group}.css" />"""
  }

  def js = { attrs ->
    def group = attrs.group
    out << """<script type="text/javascript" src="${g.resource(dir: '/')}wro/${group}.js"></script>"""
  }
}
