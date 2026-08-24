package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.importer.Importer;
import org.colston.kicks.document.importer.ImporterFactory;
import org.colston.kicks.render.RendererResources;


public class Renderer implements Callable<Boolean> {
    String inputPath, outputDir;

    public Renderer(String inputPath, String outputDir) {
        this.inputPath = inputPath;
        this.outputDir = outputDir;
    }

    public Boolean call() throws Exception {
        var file = new File(inputPath);
        var importer = ImporterFactory.getImporter(file);
        if (!importer.isPresent()) return false;

        // TODO: handle XML imports, maybe?
        
        var document = importer.get().importFile(file);

        for (var song : document.getSongs()) {
            System.out.println(song.getTitle());
            System.out.println(song.getTitleRomaji());
            var tuning = song.getTuning();
            if (tuning != null) {
                System.out.println(tuning.getDisplayName());
            }
        }

        for (var note : document.getNotes()) {
            drawNote(note);
        }

        // draw the repeats
        // for (Repeat r : doc.getRepeats(pageRange)) {
        //     cursorStartHighlight(g2, r, true, null);
        //     drawRepeat(g2, r.isBack(), r.getIndex(), r.getOffset());
        //     cursorEndHighlight(g2, null);
        // }

        // draw the lyrics
        // g2.setFont(lyricFont);
        // fm = g2.getFontMetrics();
        // for (Lyric l : doc.getLyrics(pageRange)) {
        //     if (settings != null && settings.isRomaji()) {
        //         drawRomajiLyric(g2, l.getValue(), l.getIndex(), l.getOffset(), fm);
        //     } else {
        //         char[] ch = l.getValue().toCharArray();
        //         for (int i = 0; i < ch.length; i++) {
        //             cursorStartHighlight(g2, l, false, null);
        //             drawLyric(g2, ch, i, l.getIndex(), l.getOffset(), fm);
        //             cursorEndHighlight(g2, null);

        //         }
        //     }
        // }

        return true;
    }

    private String noteHead(Note n) {
        var accidental = (n.getAccidental() == Accidental.FLAT) ? "♭" : "";
        var note = RendererResources.getNoteText(n.getString(), n.getPlacement());
        return String.format("%s%s", accidental, note);
    }

    private String noteFeatures(Note n) {
        var features = new ArrayList<String>();
        if (n.isSmall()) features.add("s");
        if (n.isChord()) features.add("c");
        if (n.isSlur()) features.add("~");

        if (features.isEmpty()) return "";

        return String.format("(%s)", String.join(",", features));
    }

    private void drawNote(Note n) {
        System.out.printf(
            "[%d,%d]%s%s",
            n.getIndex(),
            n.getOffset(),
            noteFeatures(n),
            noteHead(n)
        );

        switch (n.getUtou()) {
            case KAKI -> {
                System.out.printf("⏋");
            }
            case UCHI -> {
                System.out.printf("`");
            }
            case NONE -> {
                // do nothing
            }
        }
    }

}
