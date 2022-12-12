
![GitHub](https://img.shields.io/github/license/adamldavis/groocss)
![Maven Central](https://img.shields.io/maven-central/v/org.groocss/groocss)
![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/org.groocss.groocss-gradle-plugin)

<img width="200" style="border-radius:100%" src="gradle/groocss2_256.png" />

# GrooCSS
Like [Less](http://lesscss.org/) but without inventing a new language.

[GrooCSS](http://groocss.org) lets you code your CSS in Groovy, using a natural Groovy DSL.

It was created by Adam L. Davis (@adamldavis) and inspired by the many other Groovy-based projects out there, like 
[Gradle](gradle.org), [Grails](https://grails.org/), 
[Spock](https://github.com/spockframework/spock), [Ratpack](https://ratpack.io/), and [grooscript](http://grooscript.org/).

- DSL similar to CSS but with camel-case and some modifications to make it valid Groovy.
- Keyframes, media, charset, and font-face support.
- Automatically adds -webkit, -ms, -moz, -o extensions! (configurable)
- Color support with rgb, rgba, hex, named colors, and several color changing methods (mix, tint, shade, saturate, etc.)
- Minimization (compress)
- Support for transforms directly (transformX, etc),
- Math functions (sqrt, sin, cos, toRadians, etc.) and built-in Measurement math.
- Unit methods (unit, getUnit, convert)
- Ability to extend style-groups and add internal groups.
- Pseudo-classes in DSL (nthChild, etc.)
- Multiple ways to configure: Config.builder() or using withConfig
- Close to CSS syntax using getAt, putAt, operator-overloading, underscore, methodMissing, and propertyMissing
- Translator to convert from existing CSS.
- Available pretty print (using Config)
- Ability to create and reuse groups of styles using styles{} syntax.
- Methods for getting an image's width, height, or size.
- Validates some values by default and can be configured with custom validators and/or processors.
- Uses Groovy extension modules

## How to contribute

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for contribution guidelines.

## Tested

Uses Spock for testing. Well tested.

## Open Source

Apache 2 licensed. Open Source. 

## Website

_Check out the [website](http://www.groocss.org/) for more info._

