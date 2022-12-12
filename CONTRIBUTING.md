# Contributing to GrooCSS

Contributions welcome!
If you'd like to contribute (and we hope you do) please take a look at the following guidelines and instructions.

- Please open an issue if you have a positive suggestion or found a bug.
- Please contact me if you have any questions.
- Please submit a Pull Request if you have coded some improvement. 

Thanks!

## Build Environment

GrooCSS builds with [Gradle](http://www.gradle.org/).
You do not need to have Gradle installed to work with the GrooCSS build as Gradle provides an executable wrapper that you use to drive the build.

On UNIX type environments this is `gradlew` and is `gradlew.bat` on Windows.

For example to run all the automated tests and quality checks for the entire project you would run…

    ./gradlew check

### The Guide

The guide project can be found at `docs/guide` within the project tree.
The [AsciiDoc](http://asciidoc.org/) source files, CSS and images that make up the manual can be found at `docs/guide/src` (the manual is compiled using a tool called [AsciiDoctor](http://asciidoctor.org/)).
Most documentation contributions are simply modifications to these files.

To compile the manual in or to see any changes made, simply run (from the root of the GrooCSS project)…

    ./gradlew :guide:asciidoctor

You will then find the compiled HTML in the `docs/guide/build/docs/asciidoc` directory.

### The API reference

The API reference is made up of the Groovydoc (like Javadoc) that annotates the Groovy files for the different modules in `modules/`.
To make a change to the reference API documentation, find the corresponding file in `modules/«module»/src/main/groovy` and make the change.

You can then generate the API reference HTML by running…

    ./gradlew :groocss:groovydoc

You will then find the compiled HTML in the directory `modules/groocss/build/docs/groovydoc`

> Note that you can build the manual chapters and reference API in one go with `./gradlew doc:manual:packageManual`

## Contributing features/patches

The source code for the modules is contained in the `modules/` directory.

To run the tests and quality checks after making your change to a module, you can run…

    ./gradlew :«module-name»:check

To run the entire test suite and quality checks, run…

    ./gradlew check

Please remember to include relevant updates to the manual with your changes.

## Coding style guidelines

The following are some general guidelines to observe when contributing code:

1. All source files must have the appropriate ASLv2 license header
1. All source files use an indent of 4 spaces
1. Everything needs to be tested
1. Documentation needs to be updated appropriately to the change

The build processes checks that most of the above conditions have been met.

## Code changes

Code can be submitted via GitHub pull requests.
When a pull request is send it triggers a CI build to verify the the test and quality checks still pass.

## Proposing new features

If you would like to implement a new feature, please [raise an issue](https://github.com/adamldavis/groocss/issues) before sending a pull request so the feature can be discussed.
This is to avoid you spending your valuable time working on a feature that the project developers are not willing to accept into the codebase.

## Fixing bugs

If you would like to fix a bug, please [raise an issue](https://github.com/adamldavis/groocss/issues) before sending a pull request so it can be discussed.
If the fix is trivial or non controversial then this is not usually necessary.

## Licensing and attribution

GrooCSS is licensed under [ASLv2](http://www.apache.org/licenses/LICENSE-2.0). All source code falls under this license.

The source code will not contain attribution information (e.g. Javadoc) for contributed code.
Contributions will be recognised elsewhere in the project documentation.