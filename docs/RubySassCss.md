# Introduction 

Support for Sass [Syntactically awesome style sheets](http://sass-lang.com).  Since 1.4.6.
This extension differs from the SassCss plugin in that it uses jRuby to compile the scss to css, as opposed a javascript based clone. 

# Details 

Sass support is provided by the RubySassCssProcessor, it can be used as a pre or post processor.  Its purpose is to convert Sass css syntax in to css syntax. The underlying implementation uses jRbuy and the Sass gem packaged in a jar to preform the processing.

Sass does a lot of cool stuff, the best place to see that is [sass-lang.com](http://sass-lang.com].

All SassCSs syntax is supported, however the @import directive has impaired functionality.  Due to either (I'm not sure which) jRuby or the ruby implementation of @import, it uses the JVMs working directory as the starting point for import paths.  This is usually not the directory where your style sheets are located.  Using absolute file paths may work, but again is not usually practical. A better work around is to avoid using @import, and instead use the RubySassCssProcessor as a post processor and let wro4j perform the merging of your sass stylesheets before they are run through the processor.

# Integration

Like all of the wro4j extension processors the RubySassCssProcessor can be configured in a variety of ways.  It should be aliased as 'rubySassCss', and as described above it's recommended to use it as a post processor.