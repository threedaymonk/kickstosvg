package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.importer.Importer;
import org.colston.kicks.document.importer.ImporterFactory;
import org.colston.kicks.document.persistence.DocumentStore;
import org.colston.kicks.render.RendererResources;


public class Renderer implements Callable<Boolean> {
    private static final int TITLE_MARGIN = 9;
    private static final int COLUMN_WIDTH = 56;
    private static final int COLUMN_SPACE = 9;
    private static final int CELL_HEIGHT = 36;
    private static final int CELL_WIDTH = COLUMN_WIDTH / 2;
    private static final int BORDER_WIDTH = 20;
    private static final int COLUMNS_PER_PAGE = 11;
    private static final int CELLS_PER_COL = 12;
    private static final int CELLS_PER_PAGE = COLUMNS_PER_PAGE * CELLS_PER_COL;
    private static final int CANVAS_WIDTH = COLUMN_WIDTH * COLUMNS_PER_PAGE + COLUMN_SPACE * COLUMNS_PER_PAGE;
    private static final int CANVAS_HEIGHT = CELL_HEIGHT * CELLS_PER_COL;
    private static final int REPEAT_HEAD_WIDTH = 6;
    private static final int REPEAT_HEAD_HEIGHT = 8;
    private static final int X_OFFSET_CHORD = COLUMN_WIDTH / 2 - 4;
    private static final int X_OFFSET_SLUR = 4;
    private static final int FONT_SIZE_LARGE = 14;
    private static final int FONT_SIZE_SMALL = 12;
    private static final int FONT_SIZE_LYRICS = 8;
    private static final int MARGIN_X = 102;
    private static final int MARGIN_Y = 102;

    String inputPath, outputDir;

    public Renderer(String inputPath, String outputDir) {
        this.inputPath = inputPath;
        this.outputDir = outputDir;
    }

    private KicksDocument loadDocument(File file) throws Exception {
        if (!file.exists() || !file.canRead())
            throw new Exception("Cannot read file: " + file.getAbsolutePath());

        var importer = ImporterFactory.getImporter(file);

        if (importer.isPresent()) return importer.get().importFile(file);

        return DocumentStore.create().load(file);
    }

    public Boolean call() throws Exception {
        var document = loadDocument(new File(inputPath));

        System.out.println("<svg viewBox=\"0 0 842 595\" height=\"210mm\" width=\"297mm\" version=\"1.1\">");

        //for (var song : document.getSongs()) {
        //    System.out.println(song.getTitle());
        //    System.out.println(song.getTitleRomaji());
        //    var tuning = song.getTuning();
        //    if (tuning != null) {
        //        System.out.println(tuning.getDisplayName());
        //    }
        //}

        System.out.printf("<g style=\"text-align: center; text-anchor: middle\">\n");
        for (var note : document.getNotes()) drawNote(note);
        System.out.println("</g>");

        System.out.printf("<g style=\"font-size: %d\">\n", FONT_SIZE_LYRICS);
        for (var lyric : document.getLyrics()) drawLyric(lyric);
        System.out.println("</g>");

        // draw the repeats
        // for (Repeat r : doc.getRepeats(pageRange)) {
        //     cursorStartHighlight(g2, r, true, null);
        //     drawRepeat(g2, r.isBack(), r.getIndex(), r.getOffset());
        //     cursorEndHighlight(g2, null);
        // }

        System.out.println("</svg>");
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

    private void drawLyric(Lyric l) {
        var x = columnLeft(l.getIndex()) + COLUMN_WIDTH / 2;
        var y = cellTop(l.getIndex()) + (l.getOffset() * CELL_HEIGHT) / 12 - FONT_SIZE_LYRICS / 2;

        System.out.printf(
            "<text x=\"%d\" y=\"%d\" style=\"writing-mode: tb-rl\">%s</text>\n",
            x, y, l.getValue()
        );
    }

    private void drawNote(Note n) {
        var fontSize = n.isSmall() ? FONT_SIZE_SMALL : FONT_SIZE_LARGE;
        var x = columnLeft(n.getIndex()) + COLUMN_WIDTH / 4;
        var y = cellTop(n.getIndex()) + (n.getOffset() * CELL_HEIGHT) / 12 - fontSize / 2;

        System.out.printf(
            "<text x=\"%d\" y=\"%d\" style=\"font-size: %d\">%s</text>\n",
            x, y, fontSize,
            RendererResources.getNoteText(n.getString(), n.getPlacement())
        );

        // var articulation = "";

        // switch (n.getUtou()) {
        //     case KAKI -> articulation = "⏋";
        //     case UCHI -> articulation = "`";
        //     case NONE -> articulation = "";
        // }
    }

    private int columnLeft(int index) {
        return MARGIN_X + CANVAS_WIDTH + COLUMN_SPACE - (COLUMN_WIDTH + COLUMN_SPACE) * (index / CELLS_PER_COL);
    }

    private int cellTop(int index) {
        return MARGIN_Y + (index % CELLS_PER_COL) * CELL_HEIGHT;
    }
}
