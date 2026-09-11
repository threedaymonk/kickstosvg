package uk.sanshinkai.kickstosvg;

import org.springframework.ui.ModelMap;
import java.util.Map;

// TODO: Think of a better name for this. It handles dimensions (which I'd like
// to be read from a configuration file) and fonts and computations about
// positioning and a few other presentational concerns.
class Metrics {
    public double cellHeight() { return 36; }
    public int cellsPerCol() { return 12; }
    public double columnSpace() { return 9; }
    public double columnWidth() { return 56; }
    public int columnsPerPage() { return 11; }
    public double fontSizeLyrics() { return 10; }
    public double fontSizeNote() { return 18; }
    public double fontSizeNoteSmall() { return 14; }
    public double fontSizeTitleFurigana() { return 12; }
    public double fontSizeTitleJapanese() { return 26; }
    public double fontSizeTitleLatin() { return 14; }
    public double fontSizeTuning() { return 14; }
    public double paperHeight() { return 595; }
    public double paperWidth() { return 842; }
    public double chordXOffset() { return 24.5; }
    public double slurXOffset() { return 3.5; }

    public Map<String, Object> map() {
        return new ModelMap()
            .addAttribute("cellHeight", cellHeight())
            .addAttribute("cellsPerCol", cellsPerCol())
            .addAttribute("columnSpace", columnSpace())
            .addAttribute("columnWidth", columnWidth())
            .addAttribute("columnsPerPage", columnsPerPage())
            .addAttribute("fontSizeLyrics", fontSizeLyrics())
            .addAttribute("fontSizeNote", fontSizeNote())
            .addAttribute("fontSizeNoteSmall", fontSizeNoteSmall())
            .addAttribute("fontSizeTitleFurigana", fontSizeTitleFurigana())
            .addAttribute("fontSizeTitleJapanese", fontSizeTitleJapanese())
            .addAttribute("fontSizeTitleLatin", fontSizeTitleLatin())
            .addAttribute("fontSizeTuning", fontSizeTuning())
            .addAttribute("paperHeight", paperHeight())
            .addAttribute("paperWidth", paperWidth())
            .addAttribute("chordXOffset", chordXOffset())
            .addAttribute("slurXOffset", slurXOffset());
    }
}
