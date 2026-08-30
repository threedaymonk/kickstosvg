# kickstosvg

Render a [Kicks] kunkunshi document to Inkscape-compatible SVG.

[Kicks]: https://github.com/simoncolston/kicks

## Preparation

    git clone --recurse-submodules [this repo URL]

## Build

    mvn package

## Run

    java -jar target/kickstosvg.jar SOURCE > OUTPUT.svg

Source can be a .kicks or .kicksabc file.
