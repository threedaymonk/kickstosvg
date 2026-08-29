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

import uk.sanshinkai.kickstosvg.Metrics;

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
        var document = loadDocument(new File(inputPath));
        var svgDoc = DocumentHelper.createDocument();
        var svg = svgDoc.addElement("svg")
            .addAttribute("viewBox", String.format("%d %d %d %d", -metrics.marginX(), -metrics.marginY(), metrics.paperWidth(), metrics.paperHeight()))
            .addAttribute("width", String.format("%dpt", metrics.paperWidth()))
            .addAttribute("height", String.format("%dpt", metrics.paperHeight()))
            .addAttribute("version", "1.1")
            .addAttribute("style", String.format("stroke-linecap: square; stroke-linejoin: miter; font-family: '%s'", metrics.fontFaceJapanese()));

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
            .addAttribute("style", String.format("font-size: %dpt; writing-mode: tb-rl; letter-spacing: -4", metrics.fontSizeLyrics()));
        for (var lyric : document.getLyrics()) drawLyric(g, lyric);

        // TODO: repeats
        
        // TODO: tuning

        // TODO: write to the output file, instead of stdout!
        System.out.println(svgDoc.asXML());
        return true;
    }

    private int[] enumerateColumns(List<Note> notes) {
        var columnNos = new TreeSet<Integer>();
        for(var n : notes) columnNos.add(metrics.columnNumber(n.getIndex()));
        return columnNos.stream().mapToInt(Integer::intValue).toArray();
    }

    private void drawLyric(Element container, Lyric l) {
        // Reference point is top centre of character
        var x = metrics.columnLeft(metrics.columnNumber(l.getIndex()))
            + metrics.cellWidth()
            + metrics.fontSizeLyrics() / 2
            + metrics.marginLyrics();
        var y = metrics.cellTop(metrics.cellNumber(l.getIndex()))
            + metrics.cellOffset(l.getOffset())
            - metrics.fontSizeLyrics() / 2;

        container.addElement("text")
            .addAttribute("x", String.valueOf(x))
            .addAttribute("y", String.valueOf(y))
            .addText(l.getValue());
    }

    private void drawNote(Element container, Note n) {
        var fontSize = n.isSmall() ? metrics.fontSizeSmall() : metrics.fontSizeLarge();
        // Reference point is centre of baseline
        var x = metrics.columnLeft(metrics.columnNumber(n.getIndex()))
            + metrics.cellWidth() / 2;
        var y = metrics.cellTop(metrics.cellNumber(n.getIndex()))
            + metrics.cellOffset(n.getOffset())
            + fontSize / 2;

        container.addElement("text")
            .addAttribute("x", String.valueOf(x))
            .addAttribute("y", String.valueOf(y))
            .addAttribute("style", String.format("font-size: %dpt", fontSize))
            .addText(RendererResources.getNoteText(n.getString(), n.getPlacement()));

        // TODO: articulations
    }

    private void drawTitle(Element container, Song song) {
        var x = metrics.columnLeft(metrics.columnNumber(song.getIndex()));
        container.addElement("text")
            .addAttribute("x", String.valueOf(x + metrics.cellWidth() / 2))
            .addAttribute("y", "0")
            .addAttribute("style", String.format("font-size: %dpt", metrics.fontSizeTitle()))
            .addText(song.getTitle());
        container.addElement("text")
            .addAttribute("x", String.valueOf(x + metrics.columnWidth() - metrics.fontSizeTitle()))
            .addAttribute("y", "0")
            .addAttribute("style", String.format("font-size: %dpt; font-family: '%s'", metrics.fontSizeTitle(), metrics.fontFaceLatin()))
            .addText(song.getTitleRomaji());
    }

    private void drawColumn(Element container, int colNo) {
        var left = metrics.columnLeft(colNo);
        container.addElement("rect")
            .addAttribute("x", String.valueOf(metrics.columnLeft(colNo)))
            .addAttribute("y", "0")
            .addAttribute("width", String.valueOf(metrics.columnWidth()))
            .addAttribute("height", String.valueOf(metrics.canvasHeight()));
        container.addElement("path")
            .addAttribute("d", String.format("M %s,%s %s,%s",
                left + metrics.cellWidth(), 0,
                left + metrics.cellWidth(), metrics.canvasHeight()));
        for(var i = 1; i < metrics.cellsPerCol(); i++) {
            container.addElement("path")
                .addAttribute("d", String.format("M %d,%d %d,%d",
                    left, metrics.cellTop(i),
                    left + metrics.cellWidth(), metrics.cellTop(i)));
        }
    }
}
