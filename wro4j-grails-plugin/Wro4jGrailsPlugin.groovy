import org.springframework.context.ApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy
import wro4j.grails.plugin.ReloadableWroFilter
import wro4j.grails.plugin.GrailsWroManagerFactory
import wro4j.grails.plugin.WroConfigHandler
import wro4j.grails.plugin.WroDSLHandler

class Wro4jGrailsPlugin {
  // the plugin version == wro4j version
  def version = "1.6.3"
  // the version or versions of Grails the plugin is designed for
  def grailsVersion = "1.3.7 > *"
  // the other plugins this plugin depends on
  def dependsOn = [:]
  // resources that are excluded from plugin packaging
  def pluginExcludes = [
      "grails-app/views/error.gsp",
      "grails-app/views/index.gsp",
      "grails-app/conf/Wro.groovy",
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
  def license = "APACHE"
  def scm = [ url: "https://github.com/alexo/wro4j" ]
  def issueManagement = [ system: "googleCode", url: "https://code.google.com/p/wro4j/issues/list" ]

  def doWithWebDescriptor = { xml ->
    def contextParam = xml.'context-param'
    contextParam[contextParam.size() - 1] + {
      'filter' {
        'filter-name'('wroFilter')
        'filter-class'(DelegatingFilterProxy.name)
        'init-param' {
          'param-name'('targetFilterLifecycle')
          'param-value'(true)
        }
      }
    }
    def filter = xml.'filter'
    filter[filter.size() - 1] + {
      'filter-mapping' {
        'filter-name'('wroFilter')
        'url-pattern'('/wro/*')
      }
    }
  }

  def doWithSpring = {
    WroConfigHandler.application = application
    def config = WroConfigHandler.getConfig()
    wroFilter(ReloadableWroFilter) {
      properties = config.toProperties()
      wroManagerFactory = new GrailsWroManagerFactory()
    }
  }

  def doWithDynamicMethods = { ctx ->
    // TODO Implement registering dynamic methods to classes (optional)
  }

  def doWithApplicationContext = { applicationContext ->
    // TODO Implement post initialization spring config (optional)
  }

  /** File to watch to trigger onChange  */
  def watchedResources = "file:./grails-app/conf/Wro.groovy"

  /** Detect Wro.groovy changes     */
  def onChange = { event ->
    if (event.source && event.source instanceof Class && event.source.name == "Wro") {
      Class clazz = event.source
      WroDSLHandler.dsl = clazz.newInstance()
      reload(event.ctx)
    }
  }

  /** Detect Config.groovy changes     */
  def onConfigChange = { event ->
    WroDSLHandler.dsl = null
    reload(event.ctx)
  }

  /** Reload Wro4J Filter    */
  void reload(ApplicationContext ctx) {
    WroConfigHandler.reloadConfig()
    ReloadableWroFilter wroFilter = ctx.getBeansOfType(ReloadableWroFilter).wroFilter as ReloadableWroFilter
    wroFilter.reload()
  }
}
