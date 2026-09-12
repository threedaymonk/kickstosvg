package uk.sanshinkai.kickstosvg;

import java.util.Locale;
import org.colston.kicks.document.Note;

public final class Symbols {
    private Symbols() {}

    private static final String[][] VALUES = {
        {"maru", "", "", "", "", "", "", "", ""},
        {"ai", "otsu", "rou", "gerou", "koujou", "kounaka", "koushaku", "iai", "iotsu"},
        {"yon", "jou", "naka", "shaku", "geshaku", "kougo", "irou", "iyon", "ijou"},
        {"kou", "go", "roku", "shichi", "hachi", "kyuu", "ishaku", "ikou", "igo"}
    };

    public static String noteRef(Note note) {
        return "note_" + VALUES[note.getString()][note.getPlacement()];
    }

    public static String accidentalRef(Note note) {
        return "mark_" + note.getAccidental().name().toLowerCase(Locale.ROOT);
    }

    public static String articulationRef(Note note) {
        return "mark_" + note.getUtou().name().toLowerCase(Locale.ROOT);
    }

    public static String fingerRef(Note note) {
        return String.format(Locale.ROOT, "mark_f%d", note.getFinger());
    }
}
