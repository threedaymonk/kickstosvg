package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;

class FuriganaStringParser {
    // Given a string like A{B}{C}D
    private enum ParseState {
        NONE, // before A, or after C}
        SURFACE_ONLY, // A or D
        SURFACE_WITH_READING, // B
        AWAITING_READING, // after B}
        READING // C
    }

    private ParseState state;
    private StringBuilder accumulator;
    private String surface;
    private String reading;
    private List<FuriganaComponent> components;

    public List<FuriganaComponent> parse(String raw) {
        reset();

        for (var c : raw.toCharArray()) {
            switch (c) {
                case '{' -> parseOpen(c);
                case '}' -> parseClose(c);
                default -> parseOther(c);
            }
        }

        parseEnd();

        return components;
    }

    private void reset() {
        state = ParseState.NONE;
        surface = null;
        reading = null;
        components = new ArrayList<FuriganaComponent>();
        accumulator = new StringBuilder(255);
    }

    private void parseOpen(char c) {
        switch (state) {
            case ParseState.NONE -> state = ParseState.SURFACE_WITH_READING;
            case ParseState.AWAITING_READING -> state = ParseState.READING;
            case ParseState.SURFACE_ONLY -> {
                surface = flushAccumulator();
                finishPart();
                state = ParseState.SURFACE_WITH_READING;
            }
            default -> consume(c);
        }
    }

    private void parseClose(char c) {
        switch (state) {
            case ParseState.SURFACE_WITH_READING -> {
                surface = flushAccumulator();
                state = ParseState.AWAITING_READING;
            }
            case ParseState.READING -> {
                reading = flushAccumulator();
                finishPart();
                state = ParseState.NONE;
            }
            default -> consume(c);
        }
    }

    private void parseOther(char c) {
        switch (state) {
            case ParseState.NONE -> {
                state = ParseState.SURFACE_ONLY;
                consume(c);
            }
            default -> consume(c);
        }
    }

    private void parseEnd() {
        switch (state) {
            case ParseState.SURFACE_ONLY -> {
                surface = flushAccumulator();
                finishPart();
            }
            default -> {}
        }
    }

    private void consume(char c) {
        accumulator.append(c);
    }

    private String flushAccumulator() {
        var str = accumulator.toString();
        accumulator = new StringBuilder(255);
        return str;
    }

    private void finishPart() {
        components.add(new FuriganaComponent(surface, reading));
        surface = null;
        reading = null;
    }
}
