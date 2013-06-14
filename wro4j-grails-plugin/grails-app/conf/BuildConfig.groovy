grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

  inherits 'global'
  log 'warn'
  
  pom true
  repositories {
    mavenLocal()
    grailsCentral()
    mavenCentral()
    mavenRepo "http://repository.codehaus.org"
  }
/*
  repositories {
    mavenLocal()
    mavenCentral()
    mavenRepo "http://repository.codehaus.org"
  }

  def gebVersion = "0.7.2"
  def seleniumVersion = "2.31.0"

  dependencies {
    runtime("ro.isdc.wro4j:wro4j-extensions:1.6.3") {
      excludes('slf4j-log4j12', 'slf4j-api', 'spring-web', 'gmaven-runtime-1.6', 'servlet-api', 'ant', 'groovy-all')
    }

    test("org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion",
         "org.codehaus.geb:geb-spock:$gebVersion") {
      exclude 'selenium-server'
      export = false
    }

    provided("org.codehaus.groovy.modules.http-builder:http-builder:0.5.2") {
      export = false
      excludes 'nekohtml', "httpclient", "httpcore","xml-apis","groovy"
    }

    provided('net.sourceforge.nekohtml:nekohtml:1.9.15') {
      export = false
      excludes "xml-apis"
    }
  }

  plugins {
    build ':release:2.2.1', ':rest-client-builder:1.0.3', {
      export = false
    }

    test(":spock:0.6", ":geb:$gebVersion") {
      export = false
    }
  }
  */
}
