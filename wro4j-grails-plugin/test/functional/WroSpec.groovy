import grails.plugin.geb.GebSpec

class WroSpec extends GebSpec {
  def setupSpec() {
  }

  def testWroPluginWorks() {
    when:
    go('/wro4j-grails-plugin/')
    then:
    $('h1').text() == 'Page optimized by Wro4j Grails Plugin'
    //js.exec("""return jQuery('h1').css('color');""") == 'rgb(255, 0, 0)'
  }
}