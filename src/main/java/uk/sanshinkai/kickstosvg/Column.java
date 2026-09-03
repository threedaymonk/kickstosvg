package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;

record Column (
    List<Note> notes,
    List<Lyric> lyrics,
    List<Repeat> repeats,
    Song song
) {
    boolean isMusic() { return song == null; }
    boolean isTitle() { return !isMusic(); }
} 
