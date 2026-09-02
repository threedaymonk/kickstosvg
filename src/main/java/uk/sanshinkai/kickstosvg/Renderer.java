package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.TreeSet;

import org.dom4j.DocumentHelper;

import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.persistence.DocumentStoreFactory;
import org.colston.kicks.render.RendererResources;

import uk.sanshinkai.kickstosvg.Builder;
import uk.sanshinkai.kickstosvg.Metrics;
import uk.sanshinkai.kickstosvg.FuriganaString;

public class Renderer implements Callable<Boolean> {
    String inputPath, outputDir;
    Metrics metrics;

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
        var target = DocumentHelper.createDocument();
        var svg = buildSvg(target);

        drawColumns(svg, music);
        drawNotes(svg, music);
        drawLyrics(svg, music);
        // TODO: drawRepeats(svg, music);
        // TODO: drawTuning(svg, music);
        drawSongTitles(svg, music);

        // TODO: write to the output file, instead of stdout!
        System.out.println(target.asXML());
        return true;
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

    private void drawColumns(Builder svg, KicksDocument music) {
        var set = new TreeSet<Integer>();
        for(var n : music.getNotes())
            set.add(metrics.columnNumber(n));

        var colsWithNotes = set.stream()
            .mapToInt(Integer::intValue).toArray();

        var g = svg.element("g")
            .attr("id", "columns1")
            .style("fill", "none")
            .style("stroke", metrics.gridColor())
            .style("stroke-width", metrics.gridStrokeWidth());
        for (var i : colsWithNotes) drawColumn(g, i);
    }

    private void drawNotes(Builder svg, KicksDocument music) {
        var g = svg.element("g")
            .attr("id", "notes1")
            .style("text-align", "center")
            .style("text-anchor", "middle");
        for (var note : music.getNotes()) drawNote(g, note);
    }

    private void drawLyrics(Builder svg, KicksDocument music) {
        var g = svg.element("g")
            .attr("id", "lyrics1")
            .style("font-size", metrics.fontSizeLyrics())
            .style("writing-mode", "tb-rl")
            .style("letter-spacing", metrics.lyricSpaceAdjustment());

        for (var lyric : music.getLyrics()) drawLyric(g, lyric);
    }

    private void drawSongTitles(Builder svg, KicksDocument music) {
        var g = svg.element("g")
            .attr("id", "title1")
            .style("writing-mode", "tb-rl");
        for (var song : music.getSongs()) drawTitle(g, song);
    }

    private void drawLyric(Builder container, Lyric l) {
        var center = metrics.lyricCoords(l);

        // Reference point is top centre of first character
        // so shift y up by half the font size
        container.element("text")
            .attr("x", center.x())
            .attr("y", center.y() - metrics.fontSizeLyrics() / 2)
            .text(l.getValue());
    }

    private void drawNote(Builder container, Note n) {
        var fontSize = n.isSmall()
            ? metrics.fontSizeSmall()
            : metrics.fontSizeLarge();
        var center = metrics.noteCoords(n);

        // Reference point is centre of baseline
        // so shift y down by half the font size
        container.element("text")
            .attr("x", center.x())
            .attr("y", center.y() + fontSize / 2)
            .style("font-size", fontSize)
            .text(RendererResources.getNoteText(n.getString(), n.getPlacement()));

        // TODO: articulations
    }

    private void drawTitle(Builder container, Song song) {
        var jTitle = new FuriganaString(song.getTitle());
        var x0 = metrics.columnLeft(metrics.columnNumber(song));

        // Reference point is top centre of first character

        container.element("text")
            .attr("x", x0 + metrics.fontSizeTitleJapanese() / 2)
            .attr("y", 0)
            .style("font-size", metrics.fontSizeTitleJapanese())
            .text(jTitle.surface());

        var pos = 0;
        for (var c : jTitle.components) {
            if (c.reading != null) {
                var x = x0
                    + metrics.fontSizeTitleJapanese()
                    + metrics.fontSizeTitleFurigana() / 2;
                var y = metrics.fontSizeTitleJapanese() * pos
                    + metrics.fontSizeTitleJapanese() * c.surface.length() / 2
                    - metrics.fontSizeTitleFurigana() * c.reading.length() / 2;
                container.element("text")
                    .attr("x", x)
                    .attr("y", y)
                    .style("font-size", metrics.fontSizeTitleFurigana())
                    .text(c.reading);
            }
            pos += c.surface.length();
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
            .attr("d", "M %s,%s %s,%s",
                left + metrics.cellWidth(), 0,
                left + metrics.cellWidth(), metrics.canvasHeight());
        for(var i = 1; i < metrics.cellsPerCol(); i++) {
            container.element("path")
                .attr("d", "M %d,%d %d,%d",
                    left, metrics.cellTop(i),
                    left + metrics.cellWidth(), metrics.cellTop(i));
        }
    }
}
