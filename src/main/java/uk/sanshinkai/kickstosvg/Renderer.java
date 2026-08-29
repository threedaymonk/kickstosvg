package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.TreeSet;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.persistence.DocumentStoreFactory;
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
    private static final int PAPER_WIDTH = 842;
    private static final int PAPER_HEIGHT = 595;
    private static final int REPEAT_HEAD_WIDTH = 6;
    private static final int REPEAT_HEAD_HEIGHT = 8;
    private static final int X_OFFSET_CHORD = COLUMN_WIDTH / 2 - 4;
    private static final int X_OFFSET_SLUR = 4;
    private static final int FONT_SIZE_LARGE = 14;
    private static final int FONT_SIZE_SMALL = 10;
    private static final int FONT_SIZE_LYRICS = 8;
    private static final int FONT_SIZE_TITLE = 16;
    private static final int FONT_SIZE_TITLE_FURIGANA = 10;
    private static final int MARGIN_X = (PAPER_WIDTH - CANVAS_WIDTH) / 2;
    private static final int MARGIN_Y = (PAPER_HEIGHT - CANVAS_HEIGHT) / 2;
    private static final int MARGIN_LYRICS = 3;
    private static final String JAPANESE_FONT = "EPSON 教科書体Ｍ";
    private static final String LATIN_FONT = "FreeSans";

    String inputPath, outputDir;

    public Renderer(String inputPath, String outputDir) {
        this.inputPath = inputPath;
        this.outputDir = outputDir;
    }

    private KicksDocument loadDocument(File file) throws Exception {
        if (!file.exists() || !file.canRead())
            throw new Exception("Cannot read file: " + file.getAbsolutePath());

        var store = DocumentStoreFactory.create(file);

        if (!store.isPresent())
            throw new Exception("Unknown file: " + file.getAbsolutePath());

        return store.get().load(file);
    }

    public Boolean call() throws Exception {
        var document = loadDocument(new File(inputPath));
        var svgDoc = DocumentHelper.createDocument();
        var svg = svgDoc.addElement("svg")
            .addAttribute("viewBox", String.format("%d %d %d %d", 0, 0, PAPER_WIDTH, PAPER_HEIGHT))
            .addAttribute("width", String.format("%dpt", PAPER_WIDTH))
            .addAttribute("height", String.format("%dpt", PAPER_HEIGHT))
            .addAttribute("version", "1.1")
            .addAttribute("style", String.format("stroke-linecap: square; stroke-linejoin: miter; font-family: '%s'", JAPANESE_FONT));

        var g = svg.addElement("g")
            .addAttribute("id", "title1")
            .addAttribute("style", String.format("writing-mode: tb-rl"));
        for (var song : document.getSongs()) drawTitle(g, song);
        
        g = svg.addElement("g")
            .addAttribute("id", "columns1")
            .addAttribute("style", "fill: none; stroke: #969696; stroke-width: 0.5");
        for (var i : enumerateColumns(document.getNotes())) drawColumn(g, i);

        g = svg.addElement("g")
            .addAttribute("id", "notes1")
            .addAttribute("style", "text-align: center; text-anchor: middle");
        for (var note : document.getNotes()) drawNote(g, note);

        g = svg.addElement("g")
            .addAttribute("id", "lyrics1")
            .addAttribute("style", String.format("font-size: %dpt; writing-mode: tb-rl; letter-spacing: -4", FONT_SIZE_LYRICS));
        for (var lyric : document.getLyrics()) drawLyric(g, lyric);

        // draw the repeats
        // for (Repeat r : doc.getRepeats(pageRange)) {
        //     cursorStartHighlight(g2, r, true, null);
        //     drawRepeat(g2, r.isBack(), r.getIndex(), r.getOffset());
        //     cursorEndHighlight(g2, null);
        // }

        System.out.println(svgDoc.asXML());
        return true;
    }

    private int[] enumerateColumns(List<Note> notes) {
        var columnNos = new TreeSet<Integer>();
        for(var n : notes) columnNos.add(columnNumber(n.getIndex()));
        return columnNos.stream().mapToInt(Integer::intValue).toArray();
    }

    private void drawLyric(Element container, Lyric l) {
        // Reference point is top centre of character
        var x = columnLeft(columnNumber(l.getIndex())) + COLUMN_WIDTH / 2 + FONT_SIZE_LYRICS / 2 + MARGIN_LYRICS;
        var y = cellTop(cellNumber(l.getIndex())) + cellOffset(l.getOffset()) - FONT_SIZE_LYRICS / 2;

        container.addElement("text")
            .addAttribute("x", String.valueOf(x))
            .addAttribute("y", String.valueOf(y))
            .addText(l.getValue());
    }

    private void drawNote(Element container, Note n) {
        var fontSize = n.isSmall() ? FONT_SIZE_SMALL : FONT_SIZE_LARGE;
        // Reference point is centre of baseline
        var x = columnLeft(columnNumber(n.getIndex())) + COLUMN_WIDTH / 4;
        var y = cellTop(cellNumber(n.getIndex())) + cellOffset(n.getOffset()) + fontSize / 2;

        container.addElement("text")
            .addAttribute("x", String.valueOf(x))
            .addAttribute("y", String.valueOf(y))
            .addAttribute("style", String.format("font-size: %dpt", fontSize))
            .addText(RendererResources.getNoteText(n.getString(), n.getPlacement()));

        // var articulation = "";

        // switch (n.getUtou()) {
        //     case KAKI -> articulation = "⏋";
        //     case UCHI -> articulation = "`";
        //     case NONE -> articulation = "";
        // }
    }

    private void drawTitle(Element container, Song song) {
        var x = columnLeft(columnNumber(song.getIndex()));
        container.addElement("text")
            .addAttribute("x", String.valueOf(x + COLUMN_WIDTH / 4))
            .addAttribute("y", String.valueOf(MARGIN_Y))
            .addAttribute("style", String.format("font-size: %dpt", FONT_SIZE_TITLE))
            .addText(song.getTitle());
        container.addElement("text")
            .addAttribute("x", String.valueOf(x + COLUMN_WIDTH - FONT_SIZE_TITLE))
            .addAttribute("y", String.valueOf(MARGIN_Y))
            .addAttribute("style", String.format("font-size: %dpt; font-family: '%s'", FONT_SIZE_TITLE, LATIN_FONT))
            .addText(song.getTitleRomaji());
    }

    private void drawColumn(Element container, int colNo) {
        var left = columnLeft(colNo);
        container.addElement("rect")
            .addAttribute("x", String.valueOf(columnLeft(colNo)))
            .addAttribute("y", String.valueOf(MARGIN_Y))
            .addAttribute("width", String.valueOf(COLUMN_WIDTH))
            .addAttribute("height", String.valueOf(CANVAS_HEIGHT));
        container.addElement("path")
            .addAttribute("d", String.format("M %s,%s %s,%s",
                left + COLUMN_WIDTH / 2, MARGIN_Y,
                left + COLUMN_WIDTH / 2, MARGIN_Y + CANVAS_HEIGHT));
        for(var i = 1; i < CELLS_PER_COL; i++) {
            container.addElement("path")
                .addAttribute("d", String.format("M %d,%d %d,%d",
                    left, cellTop(i), left + COLUMN_WIDTH / 2, cellTop(i)));
        }
    }

    private int columnNumber(int index) {
        return (index / CELLS_PER_COL) % COLUMNS_PER_PAGE;
    }

    private int cellNumber(int index) {
        return index % CELLS_PER_COL;
    }

    private int columnLeft(int colNo) {
        return MARGIN_X + CANVAS_WIDTH - COLUMN_WIDTH * (colNo + 1) - COLUMN_SPACE * colNo;
    }

    private int cellTop(int cellNo) {
        return MARGIN_Y + cellNo * CELL_HEIGHT;
    }

    private int cellOffset(int offset) {
        return (offset * CELL_HEIGHT) / 12;
    }
}
