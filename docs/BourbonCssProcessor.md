# Introduction

Support for Sass and the mixin library [Bourbon](http://bourbon.io).  Since 1.4.7.
Bourbon provides a library of sass mixins that are simple and easy to use.  There are too many to list here, so check out the [bourbon [documentation]](ht(tp://bourbon.io/docs/).

# Details

The BourbonCssProcessor is an extension to the [RubySassCssProcessor](RubySassCssProcessor).  If you're using the BourbonCssProcessor there is no need to also configure the [RubySassCssProcessor](RubySassCssProcessor) as it's included.

As per the RubySassCssProcessor all SassCss syntax is supported, however the @import directive has impaired functionality.  Due to either (I'm not sure which) jRuby or the Sass implementation of @import, it uses the JVMs working directory as the starting point for import paths.  This is usually not the directory where your style sheets are located.  Using absolute file paths may work, but again is not usually practical. A better work around is to avoid using @import, and instead use the RubySassCssProcessor as a post processor and let wro4j perform the merging of your sass stylesheets before they are run through the processor.

Because bourbon is a mixture of ruby, and scss, both need to be available to wro4j.  The ruby part is not problem as it's contained in the bourbon-gem-jar dependency and is loaded directly from there.  However the scss files are a little more complicated.  As described above the @import directive does not work from wro4j, so we need to load  and merge Bourbons Sass stylesheets  using wro4j. See below for a sample wro.xml snippet that will load the Bourbon scss files.  The scss files themselves can be found in the  bourbon-gem-jar.jar, and will need to be copied to your source tree.

# Integration

Like all of the wro4j extension processors the RubySassCssProcessor can be configured in a variety of ways.  It should be aliased as 'rubySassCss', and as described above it's recommended to use it as a post processor.


## Example wro.xml resource group to load / merge bourbon scss reources
```xml 
<group name="app">
   <group-ref>bourbon</group-ref>
   <css> /resources/header.css</css>
   ...
</group>

<group name="bourbon" abstract="true">
    <!--Custom Functions-->

<css>/resources/sass/bourbon/functions/deprecated-webkit-gradient.scss</css>
    <css>/resources/sass/bourbon/functions/flex-grid.scss</css>
    <css>/resources/sass/bourbon/functions/grid-width.scss</css>
    <css>/resources/sass/bourbon/functions/linear-gradient.scss</css>
    <css>/resources/sass/bourbon/functions/modular-scale.scss</css>
    <css>/resources/sass/bourbon/functions/radial-gradient.scss</css>
    <css>/resources/sass/bourbon/functions/render-gradients.scss</css>
    <css>/resources/sass/bourbon/functions/tint-shade.scss</css>

<css>/resources/sass/bourbon/functions/transition-property-name.scss</css>
…

<!--CSS3 Mixins-->
    <css>/resources/sass/bourbon/css3/animation.scss</css>
    <css>/resources/sass/bourbon/css3/appearance.scss</css>
    <css>/resources/sass/bourbon/css3/background-image.scss</css>
    <css>/resources/sass/bourbon/css3/background-size.scss</css>
    <css>/resources/sass/bourbon/css3/border-image.scss</css>
    <css>/resources/sass/bourbon/css3/border-radius.scss</css>
    <css>/resources/sass/bourbon/css3/box-shadow.scss</css>
    <css>/resources/sass/bourbon/css3/box-sizing.scss</css>
    <css>/resources/sass/bourbon/css3/columns.scss</css>
    <css>/resources/sass/bourbon/css3/flex-box.scss</css>
    <css>/resources/sass/bourbon/css3/inline-block.scss</css>
    <css>/resources/sass/bourbon/css3/linear-gradient.scss</css>
    <css>/resources/sass/bourbon/css3/prefixer.scss</css>
    <css>/resources/sass/bourbon/css3/radial-gradient.scss</css>
    <css>/resources/sass/bourbon/css3/transform.scss</css>
    <css>/resources/sass/bourbon/css3/transition.scss</css>
    <css>/resources/sass/bourbon/css3/user-select.scss</css>

<!--Addons & other mixins-->
    <css>/resources/sass/bourbon/addons/button.scss</css>
    <css>/resources/sass/bourbon/addons/clearfix.scss</css>
    <css>/resources/sass/bourbon/addons/font-face.scss</css>
    <css>/resources/sass/bourbon/addons/font-family.scss</css>
    <css>/resources/sass/bourbon/addons/hide-text.scss</css>
    <css>/resources/sass/bourbon/addons/html5-input-types.scss</css>
    <css>/resources/sass/bourbon/addons/position.scss</css>
    <css>/resources/sass/bourbon/addons/timing-functions.scss</css>
  </group>
``` 