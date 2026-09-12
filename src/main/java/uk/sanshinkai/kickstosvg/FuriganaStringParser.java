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

    private class Session {
        private ParseState state;
        private StringBuilder accumulator;
        private String surface;
        private String reading;
        private List<FuriganaComponent> components;

        Session() {
            state = ParseState.NONE;
            components = new ArrayList<FuriganaComponent>();
            resetSR();
            resetAccumulator();
        }

        public ParseState state() {
            return state;
        }

        public List<FuriganaComponent> components() {
            return components;
        }

        public Session consume(char c) {
            accumulator.append(c);
            return this;
        }

        public Session finishPart() {
            components.add(new FuriganaComponent(surface, reading));
            resetSR();
            return this;
        }

        public Session flushToSurface() {
            surface = flushAccumulator();
            return this;
        }

        public Session flushToReading() {
            reading = flushAccumulator();
            return this;
        }

        public Session transitionTo(ParseState newState) {
            state = newState;
            return this;
        }

        private String flushAccumulator() {
            var str = accumulator.toString();
            resetAccumulator();
            return str;
        }

        private void resetAccumulator() {
            accumulator = new StringBuilder(255);
        }

        private void resetSR() {
            surface = null;
            reading = null;
        }
    }

    public List<FuriganaComponent> parse(String raw) {
        var s = new Session();

        for (var c : raw.toCharArray()) {
            switch (c) {
                case '{' -> parseOpen(s, c);
                case '}' -> parseClose(s, c);
                default -> parseOther(s, c);
            }
        }

        parseEnd(s);

        return s.components();
    }

    private void parseOpen(Session s, char c) {
        switch (s.state()) {
            case ParseState.NONE -> s.transitionTo(ParseState.SURFACE_WITH_READING);
            case ParseState.AWAITING_READING -> s.transitionTo(ParseState.READING);
            case ParseState.SURFACE_ONLY -> {
                s.flushToSurface()
                    .finishPart()
                    .transitionTo(ParseState.SURFACE_WITH_READING);
            }
            default -> s.consume(c);
        }
    }

    private void parseClose(Session s, char c) {
        switch (s.state()) {
            case ParseState.SURFACE_WITH_READING -> {
                s.flushToSurface()
                    .transitionTo(ParseState.AWAITING_READING);
            }
            case ParseState.READING -> {
                s.flushToReading()
                    .finishPart()
                    .transitionTo(ParseState.NONE);
            }
            default -> s.consume(c);
        }
    }

    private void parseOther(Session s, char c) {
        switch (s.state()) {
            case ParseState.NONE -> {
                s.transitionTo(ParseState.SURFACE_ONLY)
                    .consume(c);
            }
            default -> s.consume(c);
        }
    }

    private void parseEnd(Session s) {
        switch (s.state()) {
            case ParseState.SURFACE_ONLY -> {
                s.flushToSurface()
                    .finishPart();
            }
            default -> {}
        }
    }
}
