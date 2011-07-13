package wro4j.grails.plugin

import grails.test.*
import org.junit.Assert
import wro4j.grails.plugin.WroUtils
import ro.isdc.wro.config.Context
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseDecorator

class WroUtilsTests extends GrailsUnitTestCase {
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
    assert resp.containsHeader('toto')

  }
}
