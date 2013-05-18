package wro4j.grails.plugin

import static org.junit.Assert.assertEquals
import grails.test.GrailsUnitTestCase
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

class WroConfigHandlerTests extends GrailsUnitTestCase {

  def testConfigIsLoaded() {
    //when:
    def http = new RESTClient('http://localhost:8080/wro4j-grails-plugin/')
    HttpResponseDecorator resp = http.head(path: 'wro/all.css')

    //then:
    resp.headers.each {
      println it.name + ':' + it.value
    }

    //Define in Config.groovy
    assert resp.containsHeader('toto')
  }

  void testConfigOverrideDefaultWroConfig() {
    //expect
    assertEquals(60, WroConfigHandler.config.cacheUpdatePeriod) //Config.groovy
    assertEquals(0, WroConfigHandler.config.modelUpdatePeriod) //DefaultWroConfig.groovy
    assertEquals(GrailsWroManagerFactory.name, WroConfigHandler.config.managerFactoryClassName) //default WroManagerFactory
  }
}
