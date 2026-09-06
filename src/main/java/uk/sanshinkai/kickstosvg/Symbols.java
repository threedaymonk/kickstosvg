package uk.sanshinkai.kickstosvg;

import org.colston.kicks.document.Note;

public class Symbols {
    private static final String[][] VALUES = {
        {"maru", "", "", "", "", "", "", "", ""},
        {"ai", "otsu", "rou", "gerou", "koujou", "kounaka", "koushaku", "iai", "iotsu"},
        {"yon", "jou", "naka", "shaku", "geshaku", "kougo", "irou", "iyon", "ijou"},
        {"kou", "go", "roku", "shichi", "hachi", "kyuu", "ishaku", "ikou", "igo"}
    };
    private static final String[] FINGER_VALUES = {"", "①", "②", "③", "④"};

    public static String noteRef(Note note) {
        return "#" + VALUES[note.getString()][note.getPlacement()];
    }

    public static String getNoteFingerText(Note note) {
        return FINGER_VALUES[note.getFinger()];
    }
}
