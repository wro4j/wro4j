package wro4j.grails.plugin

import grails.test.GrailsUnitTestCase
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.junit.Assert

class WroConfigHandlerTests extends GrailsUnitTestCase {
  protected void setUp() {
    super.setUp()
  }

  protected void tearDown() {
    super.tearDown()
  }

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
    Assert.assertEquals(60, WroConfigHandler.config.cacheUpdatePeriod) //Config.groovy
    Assert.assertEquals(0, WroConfigHandler.config.modelUpdatePeriod) //DefaultWroConfig.groovy
    Assert.assertEquals(GrailsWroManagerFactory.name, WroConfigHandler.config.managerFactoryClassName) //default WroManagerFactory
  }
}
