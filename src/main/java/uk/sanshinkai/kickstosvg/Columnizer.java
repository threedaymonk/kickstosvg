package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Locatable;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;

class Columnizer {
    private App options;

    Columnizer(App options) {
        this.options = options;
    }

    public List<Column> columnize(KicksDocument music) {
        var colCount = 0;
        for (var n : music.getNotes()) {
            var c = columnNumber(n) + 1;
            if (c > colCount) colCount = c;
        }
        for (var l : music.getLyrics()) {
            var c = columnNumber(l) + 1;
            if (c > colCount) colCount = c;
        }
        for (var r : music.getRepeats()) {
            var c = columnNumber(r) + 1;
            if (c > colCount) colCount = c;
        }

        var columnNotes = new ArrayList<List<Note>>(colCount);
        var columnLyrics = new ArrayList<List<Lyric>>(colCount);
        var columnRepeats = new ArrayList<List<Repeat>>(colCount);
        var columnSong = new ArrayList<Song>(colCount);

        for (var i = 0; i < colCount; i++) {
            columnNotes.add(i, new ArrayList<Note>());
            columnLyrics.add(i, new ArrayList<Lyric>());
            columnRepeats.add(i, new ArrayList<Repeat>());
            columnSong.add(i, null);
        }

        for (var n : music.getNotes())
            columnNotes.get(columnNumber(n)).add(n);
        for (var l : music.getLyrics())
            columnLyrics.get(columnNumber(l)).add(l);
        for (var r : music.getRepeats())
            columnRepeats.get(columnNumber(r)).add(r);
        for (var song : music.getSongs())
            columnSong.add(columnNumber(song), song);

        var columns = new ArrayList<Column>(colCount);
        for (var i = 0; i < colCount ; i++) {
            var column = new Column(
                columnNotes.get(i),
                columnLyrics.get(i),
                columnRepeats.get(i),
                columnSong.get(i)
            );
            if (column.isMusic() || options.showTitles())
                columns.add(column);
        }

        return columns;
    }

    private int columnNumber(Locatable a) {
        return a.getIndex() / Page.CELLS_PER_COL;
    }
}
