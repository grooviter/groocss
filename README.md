
![GitHub](https://img.shields.io/github/license/grooviter/groocss)
![Maven Central](https://img.shields.io/maven-central/v/com.github.grooviter/groocss)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.github.grooviter/groocss?server=https%3A%2F%2Fs01.oss.sonatype.org)

# GrooCSS
[GrooCSS](https://grooviter.github.io/groocss/) lets you code your CSS in Groovy, using a natural Groovy DSL.

- DSL similar to CSS but with camel-case and some modifications to make it valid Groovy code.
- Keyframes, media, charset, and font-face support.
- Automatically adds -webkit, -ms, -moz, -o extensions! (configurable)
- Color support with rgb, rgba, hex, named colors
- Several color changing methods (mix, tint, shade, saturate, etc.)
- Minimization (compress)
- Support for transforms directly (transformX, etc),
- Math functions (sqrt, sin, cos, toRadians, etc.) and built-in Measurement math.
- Unit methods (unit, getUnit, convert)
- Ability to extend style-groups and add internal groups.
- Pseudo-classes in DSL (nthChild, etc.)
- Multiple ways to configure: Config.builder() or using withConfig
- Translator to convert from existing CSS.
- Available pretty print (using Config)
- Ability to create and reuse groups of styles using styles{} syntax.
- Methods for getting an image's width, height, or size.
- Validates some values by default and can be configured with custom validators and/or processors.

This project is a fork of the original [GrooCSS by Adam L. Davis](https://github.com/adamldavis/groocss)

## Documentation

Check out the [website](https://grooviter.github.io/groocss/) for more info.

## How to contribute

Please see [contribution page](./CONTRIBUTING.md) for contribution guidelines.

## License

GrooCSS is licensed under [ASLv2](http://www.apache.org/licenses/LICENSE-2.0). All source code falls under this license.

