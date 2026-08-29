package uk.sanshinkai.kickstosvg;

// TODO: Think of a better name for this. It handles dimensions (which I'd like
// to be read from a configuration file) and fonts and computations about
// positioning.
public class Metrics {
    public int cellHeight() { return 36; }
    public int cellsPerCol() { return 12; }
    public int columnSpace() { return 9; }
    public int columnWidth() { return 56; }
    public int columnsPerPage() { return 11; }
    public String fontFaceJapanese() { return "EPSON 教科書体Ｍ"; }
    public String fontFaceLatin() { return "FreeSans"; }
    public int fontSizeLarge() { return 14; }
    public int fontSizeLyrics() { return 8; }
    public int fontSizeSmall() { return 10; }
    public int fontSizeTitle() { return 16; }
    public int fontSizeTitleFurigana() { return 10; }
    public int marginLyrics() { return 3; }
    public int paperHeight() { return 595; }
    public int paperWidth() { return 842; }
    public int ticksPerCell() { return 12; }
    public int titleMargin() { return 9; }

    public int canvasWidth() {
        return columnWidth() * columnsPerPage()
            + columnSpace() * (columnsPerPage() - 1);
    }
    public int canvasHeight() {
        return cellHeight() * cellsPerCol();
    }
    public int cellWidth() {
        return columnWidth() / 2;
    }
    public int marginX() {
        return (paperWidth() - canvasWidth()) / 2;
    }
    public int marginY() {
        return (paperHeight() - canvasHeight()) / 2;
    }

    public int columnNumber(int noteIndex) {
        return (noteIndex / cellsPerCol()) % columnsPerPage();
    }

    public int cellNumber(int noteIndex) {
        return noteIndex % cellsPerCol();
    }

    public int columnLeft(int colNo) {
        return canvasWidth()
            - columnWidth() * (colNo + 1)
            - columnSpace() * colNo;
    }

    public int cellTop(int cellNo) {
        return cellNo * cellHeight();
    }

    public int cellOffset(int offset) {
        return (offset * cellHeight()) / ticksPerCell();
    }
}
