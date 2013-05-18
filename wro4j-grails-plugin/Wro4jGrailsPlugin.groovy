import org.springframework.context.ApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy

import ro.isdc.wro.http.WroContextFilter;
import wro4j.grails.plugin.GrailsWroServletContextListenerInitializer;
import wro4j.grails.plugin.ReloadableWroFilter
import wro4j.grails.plugin.GrailsWroManagerFactory
import wro4j.grails.plugin.WroConfigHandler
import wro4j.grails.plugin.WroDSLHandler

class Wro4jGrailsPlugin {
  // the plugin version == wro4j version
  def version = "1.6.3"
  def grailsVersion = "1.3.7 > *"
  def pluginExcludes = [
      "grails-app/views/index.gsp",
      "grails-app/conf/Wro.groovy",
      "web-app/css/style.css",
      "web-app/js/script.js",
      "web-app/js/jquery.js",
  ]

  def author = "Romain Philibert"
  def authorEmail = "filirom1@gmail.com"
  def title = "Wro4j Grails Plugin"
  def description = 'Web Resource Optimizer for Grails'

  def documentation = "http://code.google.com/p/wro4j/wiki/GrailsPlugin"
  def license = "APACHE"
  def scm = [ url: "https://github.com/alexo/wro4j" ]
  def issueManagement = [ system: "googleCode", url: "https://code.google.com/p/wro4j/issues/list" ]

  def doWithWebDescriptor = { xml ->
    def contextParam = xml.'context-param'
    contextParam[contextParam.size() - 1] + {
      'filter' {
        'filter-name'('wroContextFilter')
        'filter-class'(WroContextFilter.name)
      }
    }
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
        'filter-name'('wroContextFilter')
        'url-pattern'('/*')
      }
    }
    filter[filter.size() - 1] + {
      'filter-mapping' {
        'filter-name'('wroFilter')
        'url-pattern'('/wro/*')
      }
    }
  }

  def doWithSpring = {
	wroManagerFactory(GrailsWroManagerFactory)
    WroConfigHandler.application = application
    def config = WroConfigHandler.getConfig()
    wroFilter(ReloadableWroFilter) {
      properties = config.toProperties()
      wroManagerFactory = wroManagerFactory
    }
	wroServletContextListenerInitializer(GrailsWroServletContextListenerInitializer){
      properties = config.toProperties()
      wroManagerFactory = wroManagerFactory
	}
  }

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
  private void reload(ApplicationContext ctx) {
    WroConfigHandler.reloadConfig()
    ReloadableWroFilter wroFilter = ctx.getBeansOfType(ReloadableWroFilter).wroFilter as ReloadableWroFilter
    wroFilter.reload()
  }
}
