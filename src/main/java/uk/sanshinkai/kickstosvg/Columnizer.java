package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Locatable;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;
import org.springframework.ui.ModelMap;

class Columnizer {
    private ModelMap metrics;

    Columnizer(ModelMap metrics) {
        this.metrics = metrics;
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

        var noteses = new ArrayList<List<Note>>(colCount);
        var lyricses = new ArrayList<List<Lyric>>(colCount);
        var repeatses = new ArrayList<List<Repeat>>(colCount);
        var songs = new ArrayList<Song>(colCount);

        for (var i = 0; i < colCount; i++) {
            noteses.add(i, new ArrayList<Note>());
            lyricses.add(i, new ArrayList<Lyric>());
            repeatses.add(i, new ArrayList<Repeat>());
            songs.add(i, null);
        }

        for (var n : music.getNotes())
            noteses.get(columnNumber(n)).add(n);
        for (var l : music.getLyrics())
            lyricses.get(columnNumber(l)).add(l);
        for (var r : music.getRepeats())
            repeatses.get(columnNumber(r)).add(r);
        for (var song : music.getSongs())
            songs.add(columnNumber(song), song);

        var columns = new ArrayList<Column>(colCount);
        for (var i = 0; i < colCount ; i++) {
            columns.add(new Column(
                noteses.get(i),
                lyricses.get(i),
                repeatses.get(i),
                songs.get(i)
            ));
        }

        return columns;
    }

    private int columnNumber(Locatable a) {
        return a.getIndex() / ((int) metrics.getAttribute("cellsPerCol"));
    }
}
