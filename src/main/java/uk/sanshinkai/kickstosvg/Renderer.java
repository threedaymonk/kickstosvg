package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import com.google.common.collect.Lists;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.persistence.DocumentStoreFactory;
import org.colston.kicks.render.RendererResources;

import uk.sanshinkai.kickstosvg.Builder;
import uk.sanshinkai.kickstosvg.FuriganaString;
import uk.sanshinkai.kickstosvg.Metrics;

class Renderer implements Callable<Boolean> {
    private String inputPath, outputDir;
    private Metrics metrics;

    public Renderer(String inputPath, String outputDir) {
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
        var xml = new String(
            Renderer.class.getResourceAsStream("template.svg").readAllBytes()
        );
        return DocumentHelper.parseText(xml);
    }

    public Boolean call() throws Exception {
        var music = loadDocument(new File(inputPath));
        var columns = processIntoColumns(music);
        var pages = Lists.partition(columns, metrics.columnsPerPage());

        for (var i = 0; i < pages.size(); i++) {
            var xml = renderPage(pages.get(i), i);
            var filename = generateFilename(i);
            System.err.printf("Writing page %d to %s\n", i + 1, filename);
            var out = new PrintWriter(filename);
            out.print(xml);
            out.close();
        }

        return true;
    }

    // page is zero-indexed
    private String generateFilename(int page) {
        var suffix = String.format(Locale.ROOT, "-%02d.svg", page + 1);
        var name = Path.of(inputPath).getFileName().toString()
            .replaceFirst("\\.[^\\.]+$|$", suffix);
        return Path.of(outputDir, name).toString();
    }

    private String renderPage(List<Column> columns, int page) throws Exception {
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

    private void configureSvg(org.dom4j.Document doc) {
        new Builder(doc.getRootElement())
            .attr("viewBox", "%s %s %s %s",
                    f(-metrics.marginX()), f(-metrics.marginY()),
                    f(metrics.paperWidth()), f(metrics.paperHeight()))
            .attr("width", "%spt", f(metrics.paperWidth()))
            .attr("height", "%spt", f(metrics.paperHeight()));
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
        var height = metrics.repeatLength() * (repeat.isBack() ? -1 : 1);

        container.element("path")
            .attr("d", "M %s,%s h %s v %s",
                f(start.x()), f(start.y()), f(metrics.repeatWidth()), f(height))
            .style("marker-end", "url(#%s)", repeat.getStyle().name());
    }

    private void drawNote(Builder container, int colNo, Note n) {
        var fontSize = n.isSmall()
            ? metrics.fontSizeSmall()
            : metrics.fontSizeLarge();
        var center = metrics.noteCoords(n, colNo);

        // Reference point is centre of baseline
        // so shift y down by half the font size
        container.element("text")
            .attr("x", center.x())
            .attr("y", center.y() + fontSize / 2)
            .style("font-size", fontSize)
            .text(RendererResources.getNoteText(n.getString(), n.getPlacement()));

        // TODO: articulations
    }

    private void drawTitle(Builder container, int colNo, Song song) {
        var jTitle = new FuriganaString(song.getTitle());

        var x0 = metrics.columnLeft(colNo);

        // Reference point is top centre of first character

        var pos = 0;
        for (var c : jTitle.components()) {
            double sh = c.surface().length() * metrics.fontSizeTitleJapanese();
            double rh = c.reading() != null ? c.reading().length() * metrics.fontSizeTitleFurigana() : 0;
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
                    .style("font-size", metrics.fontSizeTitleJapanese())
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
                    .style("font-size", metrics.fontSizeTitleFurigana())
                    .text(c.reading());
            }

            pos += Math.max(sh, rh);
        }

        container.element("text")
                .attr("x", x0 + metrics.fontSizeTitleJapanese()
                        + metrics.fontSizeTitleLatin() / 2
                        + metrics.fontSizeTitleFurigana())
                .attr("y", 0)
                .style("font-size", metrics.fontSizeTitleLatin())
                .style("font-family", "'%s'", metrics.fontFaceLatin())
                .text(song.getTitleRomaji());

    }

    private void drawColumn(Builder container, int colNo) {
        var left = metrics.columnLeft(colNo);
        container.element("rect")
            .attr("x", metrics.columnLeft(colNo))
            .attr("y", 0)
            .attr("width", metrics.columnWidth())
            .attr("height", metrics.canvasHeight());
        container.element("path")
            .attr("d", "M %s,0 v %s",
                f(left + metrics.cellWidth()), f(metrics.canvasHeight()));
        for(var i = 1; i < metrics.cellsPerCol(); i++) {
            container.element("path")
                .attr("d", "M %s,%s h %s",
                    f(left), f(metrics.cellTop(i)), f(metrics.cellWidth()));
        }
    }

    private String f(double f) {
        return NumericFormatting.tidy(f);
    }
}
