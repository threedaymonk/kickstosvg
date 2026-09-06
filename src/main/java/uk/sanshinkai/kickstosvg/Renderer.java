package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import com.google.common.collect.Lists;
import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.Utou;
import org.colston.kicks.document.persistence.DocumentStoreFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

class Renderer implements Callable<Boolean> {
    private String inputPath;
    private String outputDir;
    private Metrics metrics;

    Renderer(String inputPath, String outputDir) {
        this.inputPath = inputPath;
        this.outputDir = outputDir;
        this.metrics = new Metrics();
    }

    private KicksDocument loadDocument(File file) throws Exception {
        if (!file.exists() || !file.canRead())
            throw new Exception("Cannot read file: " + file.getAbsolutePath());

        var store = DocumentStoreFactory.create(file);

        if (!store.isPresent())
            throw new Exception("Unknown file: " + file.getAbsolutePath());

        return store.get().load(file);
    }

    private Document loadTemplate() throws Exception {
        var stream = Renderer.class.getResourceAsStream("template.svg");
        try {
            var xml = new String(stream.readAllBytes());
            return DocumentHelper.parseText(xml);
        } finally {
            stream.close();
        }
    }

    @Override
    public Boolean call() throws Exception {
        var music = loadDocument(new File(inputPath));
        var columns = Columnizer.columnize(metrics, music);
        var pages = Lists.partition(columns, metrics.columnsPerPage());

        for (var i = 0; i < pages.size(); i++) {
                var xml = renderPage(pages.get(i));
                var filename = generateFilename(i);
                var out = new PrintWriter(filename);
            try {
                System.err.printf("Writing page %d to %s%n", i + 1, filename);
                out.print(xml);
            } finally {
                out.close();
            }
        }

        return true;
    }

    // page is zero-indexed
    private String generateFilename(int page) throws Exception {
        var suffix = String.format(Locale.ROOT, "-%02d.svg", page + 1);
        var inputFilename = Path.of(inputPath).getFileName();
        if (inputFilename == null)
            throw new Exception("Input path is empty");
        var name = inputFilename.toString()
            .replaceFirst("\\.[^\\.]+$|$", suffix);
        return Path.of(outputDir, name).toString();
    }

    private String renderPage(List<Column> columns) throws Exception {
        var target = loadTemplate();

        configureSvg(target);
        drawColumns(target, columns);
        drawNotes(target, columns);
        drawLyrics(target, columns);
        drawRepeats(target, columns);
        drawSongTitles(target, columns);

        return target.asXML();
    }


    private void configureSvg(Document doc) {
        new Builder(doc.getRootElement())
            .attr("viewBox", "%s %s %s %s",
                    fp(-metrics.marginX()), fp(-metrics.marginY()),
                    fp(metrics.paperWidth()), fp(metrics.paperHeight()))
            .attr("width", "%spt", fp(metrics.paperWidth()))
            .attr("height", "%spt", fp(metrics.paperHeight()));
    }

    private void drawColumns(Document doc, List<Column> columns) {
        var g = new Builder(doc.selectSingleNode("//g[@id='grid']"));
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            if (column.isMusic()) drawColumn(g, i);
        }
    }

    private void drawNotes(Document doc, List<Column> columns) {
        var g = new Builder(doc.selectSingleNode("//g[@id='notes']"));
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            for (var note : column.notes()) drawNote(g, i, note);
            drawChordsAndSlurs(g, i, column.notes());
        }
    }

    private void drawLyrics(Document doc, List<Column> columns) {
        var g = new Builder(doc.selectSingleNode("//g[@id='lyrics']"));
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            for (var lyric : column.lyrics()) drawLyric(g, i, lyric);
        }
    }

    private void drawRepeats(Document doc, List<Column> columns) {
        var g = new Builder(doc.selectSingleNode("//g[@id='repeats']"));
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            for (var repeat : column.repeats()) drawRepeat(g, i, repeat);
        }
    }

    private void drawSongTitles(Document doc, List<Column> columns) {
        var g = new Builder(doc.selectSingleNode("//g[@id='titles']"));
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            if (column.isTitle()) {
                drawTitle(g, i, column.song());
                drawTuning(g, i, column.song());
            }
        }
    }

    private void drawChordsAndSlurs(Builder container, int colNo, List<Note> notes) {
        Note chordStart = null;
        Note slurStart = null;

        for (var n : notes) {
            if ((chordStart == null) == n.isChord()) {
                if (n.isChord()) {
                    chordStart = n;
                } else {
                    drawJoinLine(container, colNo, chordStart, n,
                        metrics.xOffsetChord());
                    chordStart = null;
                }
            }
            if ((slurStart == null) == n.isSlur()) {
                if (n.isSlur()) {
                    slurStart = n;
                } else {
                    drawJoinLine(container, colNo, slurStart, n,
                        metrics.xOffsetSlur());
                    slurStart = null;
                }
            }
        }
        if (chordStart != null) {
            Note end = notes.getLast();
            drawJoinLine(container, colNo, chordStart, end,
                metrics.xOffsetChord());
        }
        if (slurStart != null) {
            Note end = notes.getLast();
            drawJoinLine(container, colNo, slurStart, end,
                metrics.xOffsetSlur());
        }
    }

    private void drawJoinLine(
        Builder container, int colNo, Note start, Note end,
        double xOffset
    ) {
        var cStart = metrics.noteCoords(start, colNo);
        var cEnd = metrics.noteCoords(end, colNo);
        var fsStart = start.isSmall()
            ? metrics.fontSizeNoteSmall()
            : metrics.fontSizeNote();
        var fsEnd = end.isSmall()
            ? metrics.fontSizeNoteSmall()
            : metrics.fontSizeNote();
        var x = cStart.x() + xOffset;
        // TODO: Do something better than this fudged arbitrary / 4
        var y = cStart.y() - fsStart / 4;
        var h = cEnd.y() + fsEnd / 4 - y;
        container.element("path")
            .attr("class", "joinLine")
            .attr("d", "M %s,%s v %s", fp(x), fp(y), fp(h));
    }

    private void drawLyric(Builder container, int colNo, Lyric l) {
        var center = metrics.lyricCoords(l, colNo);

        // Reference point is top centre of first character
        // so shift y up by half the font size
        container.element("text")
            .attr("x", center.x())
            .attr("y", center.y() - metrics.fontSizeLyrics() / 2)
            .text(l.getValue());
    }

    private void drawRepeat(Builder container, int colNo, Repeat repeat) {
        var start = metrics.repeatCoords(repeat, colNo);
        var href = repeat.isBack() ? "#REPEAT_BACK" : "#REPEAT";

        container.use(href, start)
            .style("marker-end", "url(#%s)", repeat.getStyle().name());
    }

    private void drawNote(Builder container, int colNo, Note n) {
        var center = metrics.noteCoords(n, colNo);

        var suffix = n.isSmall() ? "_small" : "";

        container.use(Symbols.noteRef(n) + suffix, center);

        if (n.getAccidental() == Accidental.FLAT)
            container.use("#flat" + suffix, center);

        if (n.getUtou() != Utou.NONE) {
            var href = "#" + n.getUtou().name().toLowerCase(Locale.ROOT)
                + suffix;
            container.use(href, center);
        }
    }

    private void drawTitle(Builder container, int colNo, Song song) {
        var jTitle = new FuriganaString(song.getTitle());
        double jx;
        var x0 = metrics.columnLeft(colNo);

        if (song.getTitleRomaji() == null) {
            jx = x0 + metrics.columnWidth() / 2;
        } else {
            jx = x0 + metrics.fontSizeTitleJapanese() / 2;

            var rx = x0 + metrics.fontSizeTitleJapanese()
                + metrics.fontSizeTitleLatin() / 2
                + metrics.fontSizeTitleFurigana();

            container.element("text")
                    .attr("x", rx)
                    .attr("y", 0)
                    .attr("class", "latin")
                    .text(song.getTitleRomaji());
        }

        var fx = jx + metrics.fontSizeTitleJapanese() / 2
            + metrics.fontSizeTitleFurigana() / 2;

        // Reference point is top centre of first character

        var pos = 0;
        for (var c : jTitle.components()) {
            double sh = c.surface().length() * metrics.fontSizeTitleJapanese();
            double rh = c.reading() != null
                ? c.reading().length() * metrics.fontSizeTitleFurigana()
                : 0;
            double sindent = 0;
            double rindent = 0;
            if (c.reading() != null) {
                if (sh > rh) {
                    rindent = (sh - rh) / (c.reading().length() + 1);
                    rh = sh - rindent * 2;
                } else {
                    sindent = (rh - sh) / (c.surface().length() + 1);
                    sh = rh - sindent * 2;
                }
            }
            container.element("text")
                    .attr("x", jx)
                    .attr("y", pos + sindent)
                    .attr("textLength", sh)
                    .attr("lengthAdjust", "spacing")
                    .text(c.surface());
            if (c.reading() != null) {
                container.element("text")
                    .attr("x", fx)
                    .attr("y", pos + rindent)
                    .attr("textLength", rh)
                    .attr("lengthAdjust", "spacing")
                    .attr("class", "furigana")
                    .text(c.reading());
            }

            pos += Math.max(sh, rh);
        }

    }

    private void drawTuning(Builder container, int colNo, Song song) {
        var tuning = song.getTuning();
        if (tuning == null) return;

        var start = metrics.tuningCoords(colNo);
        container.element("text")
                .attr("x", start.x())
                .attr("y", start.y())
                .attr("class", "tuning")
                .text(tuning.getDisplayName());
    }

    private void drawColumn(Builder container, int colNo) {
        var left = metrics.columnLeft(colNo);
        container.use("#COLUMN", left, 0);
    }

    private String fp(double f) {
        return NumericFormatting.tidy(f);
    }
}
