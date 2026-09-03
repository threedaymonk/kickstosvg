package uk.sanshinkai.kickstosvg;

import org.colston.kicks.document.Locatable;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;

import uk.sanshinkai.kickstosvg.XYCoordinate;

// TODO: Think of a better name for this. It handles dimensions (which I'd like
// to be read from a configuration file) and fonts and computations about
// positioning and a few other presentational concerns.
class Metrics {
    public double cellHeight() { return 36; }
    public int cellsPerCol() { return 12; }
    public double columnSpace() { return 9; }
    public double columnWidth() { return 56; }
    public int columnsPerPage() { return 11; }
    public double fontSizeLarge() { return 16; }
    public double fontSizeLyrics() { return 10; }
    public double fontSizeSmall() { return 12; }
    public double fontSizeTitleJapanese() { return 20; }
    public double fontSizeTitleLatin() { return 16; }
    public double fontSizeTitleFurigana() { return 8; }
    public double graphicStrokeWidth() { return 0.5; }
    public double lyricSpaceAdjustment() { return -4; }
    public double marginLyrics() { return 3; }
    public double paperHeight() { return 595; }
    public double paperWidth() { return 842; }
    public int ticksPerCell() { return 12; }
    public double titleMargin() { return 9; }

    public double canvasWidth() {
        return columnWidth() * columnsPerPage()
            + columnSpace() * (columnsPerPage() - 1);
    }
    public double canvasHeight() {
        return cellHeight() * cellsPerCol();
    }
    public double cellWidth() {
        return columnWidth() / 2;
    }
    public double marginX() {
        return (paperWidth() - canvasWidth()) / 2;
    }
    public double marginY() {
        return (paperHeight() - canvasHeight()) / 2;
    }

    public int columnNumber(int noteIndex) {
        return (noteIndex / cellsPerCol());
    }

    public int columnNumber(Locatable l) {
      return columnNumber(l.getIndex());
    }

    public int columnNumber(Song s) {
      return columnNumber(s.getIndex());
    }

    public int cellNumber(int noteIndex) {
        return noteIndex % cellsPerCol();
    }

    public int cellNumber(Locatable l) {
        return cellNumber(l.getIndex());
    }

    public double columnLeft(int colNo) {
        return canvasWidth()
            - columnWidth() * (colNo + 1)
            - columnSpace() * colNo;
    }

    public double cellTop(int cellNo) {
        return cellNo * cellHeight();
    }

    public double cellOffset(int offset) {
        return (offset * cellHeight()) / ticksPerCell();
    }

    public double cellOffset(Locatable l) {
        return cellOffset(l.getOffset());
    }

    // Returns the centre co-ordinates of the note
    public XYCoordinate noteCoords(Note n, int colNo) {
        return new XYCoordinate(
            columnLeft(colNo) + cellWidth() / 2,
            cellTop(cellNumber(n)) + cellOffset(n)
        );
    }

    // Returns the centre co-ordinates of the first character of
    // the lyrics.
    public XYCoordinate lyricCoords(Lyric l, int colNo) {
        return new XYCoordinate(
            columnLeft(colNo) + cellWidth() + cellWidth() / 4,
            cellTop(cellNumber(l)) + cellOffset(l)
        );
    }

    // Returns the start (i.e. tail) co-ordinates of the repeat arrow.
    public XYCoordinate repeatCoords(Repeat r, int colNo) {
        return new XYCoordinate(
            columnLeft(colNo) + columnWidth() / 2,
            cellTop(cellNumber(r)) + cellOffset(r)
        );
    }
}
