grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true

    def gebVersion = "0.7.2"
    def seleniumVersion = "2.25.0"

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
      runtime('ro.isdc.wro4j:wro4j-extensions:1.4.5') {
        excludes('slf4j-log4j12', 'spring-web', 'gmaven-runtime-1.6', 'servlet-api', 'ant', 'groovy-all')
      }

      test("org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion",
          "org.codehaus.geb:geb-spock:$gebVersion") {
        exclude 'selenium-server'
        export = false
      }

      provided("org.codehaus.groovy.modules.http-builder:http-builder:0.5.2"){
        export = false
        excludes 'nekohtml', "httpclient", "httpcore","xml-apis","groovy"
      }
      provided('net.sourceforge.nekohtml:nekohtml:1.9.15') {
        export = false
        excludes "xml-apis"
      }

    }
    plugins {
      build(":tomcat:$grailsVersion",
            ":release:2.0.4",
            ":rest-client-builder:1.0.2") {
        excludes 'nekohtml', "httpclient"
        export = false
      }
      test(":spock:0.6", ":geb:$gebVersion") {
        export = false
      }
    }
}
