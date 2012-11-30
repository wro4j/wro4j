import ro.isdc.wro.extensions.processor.css.LessCssProcessor
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor
import ro.isdc.wro.model.resource.processor.decorator.ExtensionsAwareProcessorDecorator
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor

//Dummy Wro Config for test only
wro.header = "Toto: toto"
wro.cacheUpdatePeriod = 60
wro.grailsWroManagerFactory.preProcessors = [
    ExtensionsAwareProcessorDecorator.decorate(new CoffeeScriptProcessor()).addExtension("coffee"),
    new SemicolonAppenderPreProcessor(),
]
wro.grailsWroManagerFactory.postProcessors = [
    new LessCssProcessor(),
]
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"

log4j = {

    // Set level for all application artifacts
    debug 'ro.isdc.wro'
    debug 'wro4j'

    environments {
        development {
            console name: 'stdout', layout: pattern(conversionPattern: '[%d{ISO8601}] %c{4}    %m%n')
            root {
                info 'stdout'
            }
        }
    }
}
