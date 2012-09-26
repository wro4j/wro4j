import ro.isdc.wro.extensions.processor.css.LessCssProcessor
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor

//Dummy Wro Config for test only
wro.header = "Toto: toto"
wro.cacheUpdatePeriod = 60
wro.grailsWroManagerFactory.preProcessors = [
    new CoffeeScriptProcessor(),
    new SemicolonAppenderPreProcessor(),
]
wro.grailsWroManagerFactory.postProcessors = [
    new LessCssProcessor(),
]
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
