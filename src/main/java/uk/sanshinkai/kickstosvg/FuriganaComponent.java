package uk.sanshinkai.kickstosvg;

record FuriganaComponent(String surface, String reading) {
    boolean hasReading() {
        return reading != null;
    }
}
