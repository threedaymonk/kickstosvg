package uk.sanshinkai.kickstosvg;

import org.springframework.ui.ModelMap;

// TODO: Think of a better name for this. It handles dimensions (which I'd like
// to be read from a configuration file) and fonts and computations about
// positioning and a few other presentational concerns.
class Metrics {
    public ModelMap map() {
        return new ModelMap()
            .addAttribute("cellHeight", 36.0)
            .addAttribute("cellsPerCol", 12)
            .addAttribute("columnSpace", 9.0)
            .addAttribute("columnWidth", 56.0)
            .addAttribute("columnsPerPage", 11)
            .addAttribute("fontSizeFinger", 7.0)
            .addAttribute("fontSizeLyrics", 10.0)
            .addAttribute("fontSizeNote", 18.0)
            .addAttribute("fontSizeNoteSmall", 14.0)
            .addAttribute("fontSizeTitleFurigana", 12.0)
            .addAttribute("fontSizeTitleJapanese", 26.0)
            .addAttribute("fontSizeTitleLatin", 14.0)
            .addAttribute("fontSizeTuning", 14.0)
            .addAttribute("paperHeight", 595.0)
            .addAttribute("paperWidth", 842.0)
            .addAttribute("xOffsetChord", 10.0)
            .addAttribute("xOffsetSlur", -10.0);
    }
}
