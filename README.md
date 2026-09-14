# kickstosvg

Render a [Kicks] kunkunshi document to Inkscape-compatible SVG.

[Kicks]: https://github.com/simoncolston/kicks

## Prerequisites

### kicks-core

    git clone https://github.com/simoncolston/kicks.git
    cd kicks
    mvn clean install

## Build

    git clone [this repo URL]
    mvn clean verify

## Run

    java -jar target/kickstosvg.jar SOURCE

Source can be a `.kicks` or `.kicksabc` file.

For command-line options, run

    java -jar target/kickstosvg.jar --help

Refer to the [template file] for template parameters and CSS classes.

## SVG components for notes etc.

These come from the [kunkunshi symbols] project; the combined file is copied
into this repository and packaged into the jar file.

[template file]: ./src/main/resources/uk/sanshinkai/kickstosvg/template.ftlx
[kunkunshi symbols]: https://github.com/threedaymonk/kunkunshi-symbols
