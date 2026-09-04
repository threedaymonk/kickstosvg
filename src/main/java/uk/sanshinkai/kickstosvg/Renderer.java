package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import com.google.common.collect.Lists;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.persistence.DocumentStoreFactory;
import org.colston.kicks.render.RendererResources;
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
        var columns = processIntoColumns(music);
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
        // TODO: drawTuning(target, columns);

        return target.asXML();
    }

    private List<Column> processIntoColumns(KicksDocument music) {
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
            if (column.isTitle()) drawTitle(g, i, column.song());
        }
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

        container.element("use")
            .attr("href", href)
            .attr("x", start.x())
            .attr("y", start.y())
            .style("marker-end", "url(#%s)", repeat.getStyle().name());
    }

    private void drawNote(Builder container, int colNo, Note n) {
        var fontSize = n.isSmall()
            ? metrics.fontSizeSmall()
            : metrics.fontSizeLarge();
        var center = metrics.noteCoords(n, colNo);

        // Reference point is centre of baseline
        // so shift y down by half the font size
        var elem = container.element("text")
            .attr("x", center.x())
            .attr("y", center.y() + fontSize / 2)
            .text(RendererResources.getNoteText(n.getString(), n.getPlacement()));

        if (n.isSmall())
            elem.attr("class", "small");

        // TODO: articulations
    }

    private void drawTitle(Builder container, int colNo, Song song) {
        var jTitle = new FuriganaString(song.getTitle());

        var x0 = metrics.columnLeft(colNo);

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
                    .attr("x", x0 + metrics.fontSizeTitleJapanese() / 2)
                    .attr("y", pos + sindent)
                    .attr("textLength", sh)
                    .attr("lengthAdjust", "spacing")
                    .text(c.surface());
            if (c.reading() != null) {
                var x = x0
                    + metrics.fontSizeTitleJapanese()
                    + metrics.fontSizeTitleFurigana() / 2;
                container.element("text")
                    .attr("x", x)
                    .attr("y", pos + rindent)
                    .attr("textLength", rh)
                    .attr("lengthAdjust", "spacing")
                    .attr("class", "furigana")
                    .text(c.reading());
            }

            pos += Math.max(sh, rh);
        }

        container.element("text")
                .attr("x", x0 + metrics.fontSizeTitleJapanese()
                        + metrics.fontSizeTitleLatin() / 2
                        + metrics.fontSizeTitleFurigana())
                .attr("y", 0)
                .attr("class", "latin")
                .text(song.getTitleRomaji());

    }

    private void drawColumn(Builder container, int colNo) {
        var left = metrics.columnLeft(colNo);
        container.element("use")
            .attr("href", "#COLUMN")
            .attr("x", left)
            .attr("y", 0);
    }

    private String fp(double f) {
        return NumericFormatting.tidy(f);
    }
}
