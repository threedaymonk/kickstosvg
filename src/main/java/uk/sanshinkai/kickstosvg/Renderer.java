package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.TreeSet;

import com.google.common.collect.Lists;
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
import uk.sanshinkai.kickstosvg.Metrics;
import uk.sanshinkai.kickstosvg.FuriganaString;

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
        var suffix = String.format("-%02d.svg", page + 1);
        var name = Path.of(inputPath).getFileName().toString()
            .replaceFirst("\\.[^\\.]+$|$", suffix);
        return Path.of(outputDir, name).toString();
    }

    private String renderPage(List<Column> columns, int page) {
        var target = DocumentHelper.createDocument();
        var svg = buildSvg(target);

        writeDefinitions(svg);
        drawColumns(svg, columns);
        drawNotes(svg, columns);
        drawLyrics(svg, columns);
        drawRepeats(svg, columns);
        // TODO: drawTuning(svg, music);
        drawSongTitles(svg, columns);

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

    private Builder buildSvg(org.dom4j.Document xmlDoc) {
        return new Builder(xmlDoc.addElement("svg"))
            .attr("viewBox", "%d %d %d %d",
                    -metrics.marginX(), -metrics.marginY(),
                    metrics.paperWidth(), metrics.paperHeight())
            .attr("width", "%dpt", metrics.paperWidth())
            .attr("height", "%dpt", metrics.paperHeight())
            .attr("version", "1.1")
            .style("stroke-linecap", "square")
            .style("stroke-linejoin", "miter")
            .style("font-family", "'%s'", metrics.fontFaceJapanese());
    }

    private void drawColumns(Builder svg, List<Column> columns) {
        var g = svg.element("g")
            .attr("id", "columns1")
            .style("fill", "none")
            .style("stroke", metrics.gridColor())
            .style("stroke-width", metrics.gridStrokeWidth());
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            if (column.isMusic()) drawColumn(g, i);
        }
    }

    private void drawNotes(Builder svg, List<Column> columns) {
        var g = svg.element("g")
            .attr("id", "notes1")
            .style("text-align", "center")
            .style("text-anchor", "middle");
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            for (var note : column.notes()) drawNote(g, i, note);
        }
    }

    private void drawLyrics(Builder svg, List<Column> columns) {
        var g = svg.element("g")
            .attr("id", "lyrics1")
            .style("font-size", metrics.fontSizeLyrics())
            .style("writing-mode", "tb-rl")
            .style("letter-spacing", metrics.lyricSpaceAdjustment());

        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            for (var lyric : column.lyrics()) drawLyric(g, i, lyric);
        }
    }

    private void drawRepeats(Builder svg, List<Column> columns) {
        var g = svg.element("g")
            .attr("id", "repeats1")
            .style("fill", "none")
            .style("stroke", "black")
            .style("stroke-width", metrics.graphicStrokeWidth());

        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            for (var repeat : column.repeats()) drawRepeat(g, i, repeat);
        }
    }

    private void drawSongTitles(Builder svg, List<Column> columns) {
        var g = svg.element("g")
            .attr("id", "title1")
            .style("writing-mode", "tb-rl");
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
            .attr("d", "M %d,%d h %d v %d",
                start.x(), start.y(), metrics.repeatWidth(), height)
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

        container.element("text")
            .attr("x", x0 + metrics.fontSizeTitleJapanese() / 2)
            .attr("y", 0)
            .style("font-size", metrics.fontSizeTitleJapanese())
            .text(jTitle.surface());

        var pos = 0;
        for (var c : jTitle.components()) {
            if (c.reading() != null) {
                var x = x0
                    + metrics.fontSizeTitleJapanese()
                    + metrics.fontSizeTitleFurigana() / 2;
                var h = c.surface().length() * metrics.fontSizeTitleJapanese();
                var y = metrics.fontSizeTitleJapanese() * pos
                    + metrics.fontSizeTitleJapanese() * c.surface().length() / 2
                    - h / 2;
                container.element("text")
                    .attr("x", x)
                    .attr("y", y)
                    .attr("textLength", h)
                    .attr("lengthAdjust", "spacing")
                    .style("font-size", metrics.fontSizeTitleFurigana())
                    .text(c.reading());
            }
            pos += c.surface().length();
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
            .attr("d", "M %d,%d v %d",
                left + metrics.cellWidth(), 0, metrics.canvasHeight());
        for(var i = 1; i < metrics.cellsPerCol(); i++) {
            container.element("path")
                .attr("d", "M %d,%d h %d",
                    left, metrics.cellTop(i), metrics.cellWidth());
        }
    }

    private void writeDefinitions(Builder container) {
        var defs = container.element("defs");

        defs.element("marker")
            .attr("id", "TRIANGLE_FILLED")
            .attr("orient", "auto")
            .attr("refX", 0)
            .attr("refY", 0)
            .attr("markerUnits", "strokeWidth")
            .element("path")
            .style("fill", "black")
            .style("stroke", "black")
            .style("stroke-width", 1)
            .attr("d", "M 4,0 L -4,-4 L -4,4 z");
    }
}
