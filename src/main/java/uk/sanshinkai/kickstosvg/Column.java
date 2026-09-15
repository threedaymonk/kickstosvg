package uk.sanshinkai.kickstosvg;

import java.util.List;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Phrase;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;

record Column (
    List<Lyric> lyrics,
    List<Note> notes,
    List<Phrase> phrases,
    List<Repeat> repeats,
    Song song
) {
    boolean isMusic() {
        return (lyrics.size() + notes.size() + phrases.size() + repeats.size()) > 0;
    }

    boolean isTitle() {
        return song != null;
    }
}
