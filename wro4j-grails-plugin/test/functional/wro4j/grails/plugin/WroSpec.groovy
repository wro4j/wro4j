package wro4j.grails.plugin

import geb.spock.GebReportingSpec

class WroSpec extends GebReportingSpec {
  def setupSpec() {
  }

  def testWroPluginWorks() {
    when:
    go('/wro4j-grails-plugin/')
    then:
    $('h1').text() == 'Page optimized by Wro4j Grails Plugin'
    //js.exec("""return jQuery('h1').css('color');""") == 'rgb(255, 0, 0)'
  }

  def testSubDirs() {
    when:
    go('/wro4j-grails-plugin/subDir/subSubDir/')
    then:
    $('h1').text() == 'Page optimized by Wro4j Grails Plugin'
    //js.exec("""return jQuery('h1').css('color');""") == 'rgb(255, 0, 0)'
  }

  def testPreAndPostProcessorsCanBeDefined() {
    when:
    go('/wro4j-grails-plugin/subDir/subSubDir/')
    then:
    $('h2').text() == 'Enhanced with Coffee Script'
    //js.exec("""return jQuery('h1').css('color');""") == 'rgb(255, 0, 0)'
  }
}