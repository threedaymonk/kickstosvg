package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;

final class Columnizer {
    private Columnizer() {}

    public static List<Column> columnize(Metrics metrics, KicksDocument music) {
        var colCount = 0;
        for (var n : music.getNotes()) {
            var c = metrics.columnNumber(n) + 1;
            if (c > colCount) colCount = c;
        }
        for (var l : music.getLyrics()) {
            var c = metrics.columnNumber(l) + 1;
            if (c > colCount) colCount = c;
        }
        for (var r : music.getRepeats()) {
            var c = metrics.columnNumber(r) + 1;
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
            noteses.get(metrics.columnNumber(n)).add(n);
        for (var l : music.getLyrics())
            lyricses.get(metrics.columnNumber(l)).add(l);
        for (var r : music.getRepeats())
            repeatses.get(metrics.columnNumber(r)).add(r);
        for (var song : music.getSongs())
            songs.add(metrics.columnNumber(song), song);

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
}
