package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Locatable;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Phrase;
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
            var c = colNumber(n) + 1;
            if (c > colCount) colCount = c;
        }
        for (var l : music.getLyrics()) {
            var c = colNumber(l) + 1;
            if (c > colCount) colCount = c;
        }
        for (var r : music.getRepeats()) {
            var c = colNumber(r) + 1;
            if (c > colCount) colCount = c;
        }

        var colLyrics = new ArrayList<List<Lyric>>(colCount);
        var colNotes = new ArrayList<List<Note>>(colCount);
        var colPhrases = new ArrayList<List<Phrase>>(colCount);
        var colRepeats = new ArrayList<List<Repeat>>(colCount);
        var colSong = new ArrayList<Song>(colCount);

        for (var i = 0; i < colCount; i++) {
            colLyrics.add(i, new ArrayList<Lyric>());
            colNotes.add(i, new ArrayList<Note>());
            colPhrases.add(i, new ArrayList<Phrase>());
            colRepeats.add(i, new ArrayList<Repeat>());
            colSong.add(i, null);
        }

        for (var l : music.getLyrics()) colLyrics.get(colNumber(l)).add(l);
        for (var n : music.getNotes()) colNotes.get(colNumber(n)).add(n);
        for (var p : music.getPhrases()) colPhrases.get(colNumber(p)).add(p);
        for (var r : music.getRepeats()) colRepeats.get(colNumber(r)).add(r);
        for (var song : music.getSongs()) colSong.add(colNumber(song), song);

        var columns = new ArrayList<Column>(colCount);
        for (var i = 0; i < colCount ; i++) {
            var column = new Column(
                colLyrics.get(i),
                colNotes.get(i),
                colPhrases.get(i),
                colRepeats.get(i),
                colSong.get(i)
            );
            if (column.isMusic() || options.showTitles())
                columns.add(column);
        }

        return columns;
    }

    private int colNumber(Locatable a) {
        return a.getIndex() / Page.CELLS_PER_COL;
    }
}
