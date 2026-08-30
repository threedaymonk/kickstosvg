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
    mvn package

## Run

    java -jar target/kickstosvg.jar SOURCE > OUTPUT.svg

Source can be a `.kicks` or `.kicksabc` file.
