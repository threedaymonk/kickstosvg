package uk.sanshinkai.kickstosvg;

import java.util.ArrayList;
import java.util.List;
import org.colston.kicks.document.*;

record Column (List<Note> notes, List<Lyric> lyrics, Song song) {
    boolean isMusic() { return song == null; }
    boolean isTitle() { return !isMusic(); }
} 
