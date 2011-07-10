import ro.isdc.wro.http.WroFilter

class Wro4jGrailsPlugin {
  // the plugin version == wro4j version
  def version = "1.3.8"
  // the version or versions of Grails the plugin is designed for
  def grailsVersion = "1.3.7 > *"
  // the other plugins this plugin depends on
  def dependsOn = [:]
  // resources that are excluded from plugin packaging
  def pluginExcludes = [
      "grails-app/views/error.gsp",
      "grails-app/views/index.gsp",
      "web-app/css/style.css",
      "web-app/js/script.js",
      "web-app/js/jquery.js",
  ]

  // TODO Fill in these fields
  def author = "Romain Philibert"
  def authorEmail = "filirom1@gmail.com"
  def title = "Wro4j Grails Plugin"
  def description = '''\\
Web Resource Optimizer for Grails
'''

  // URL to the plugin's documentation
  def documentation = "http://code.google.com/p/wro4j/wiki/GrailsPlugin"

  def doWithWebDescriptor = { xml ->
    def contextParam = xml.'context-param'
    contextParam[contextParam.size() - 1] + {
      'filter' {
        'filter-name'('WebResourceOptimizer')
        'filter-class'(WroFilter.name)
      }
    }
    def filter = xml.'filter'
    filter[filter.size() - 1] + {
      'filter-mapping' {
        'filter-name'('WebResourceOptimizer')
        'url-pattern'('/wro/*')
      }
    }
  }

  def doWithSpring = {
    // TODO Implement runtime spring config (optional)
  }

  def doWithDynamicMethods = { ctx ->
    // TODO Implement registering dynamic methods to classes (optional)
  }

  def doWithApplicationContext = { applicationContext ->
    // TODO Implement post initialization spring config (optional)
  }

  def onChange = { event ->
    // TODO Implement code that is executed when any artefact that this plugin is
    // watching is modified and reloaded. The event contains: event.source,
    // event.application, event.manager, event.ctx, and event.plugin.
  }

  def onConfigChange = { event ->
    // TODO Implement code that is executed when the project configuration changes.
    // The event is the same as for 'onChange'.
  }
}
