package uk.sanshinkai.kickstosvg;

import java.io.StringWriter;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.Locatable;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.Utou;
import org.springframework.ui.ModelMap;
import org.w3c.dom.Document;

class Page {
    interface ColumnOperation {
      public void call(List<ModelMap> acc, Column column, int colNo);
    }

    private ModelMap metrics;
    private List<Column> columns;

    Page(ModelMap metrics, List<Column> columns) {
        this.metrics = metrics;
        this.columns = columns;
    }

    private Document loadXML(String name) throws Exception {
        var stream = Page.class.getResourceAsStream(name);
        try {
            if (stream == null)
                throw new Exception("Couldn't open resource: " + name);
            return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(stream);
        } finally {
            if (stream != null) stream.close();
        }
    }

    public String render() throws Exception {
        var template = FreeMarker.getTemplate("template.ftlx");
        var root = new ModelMap()
            .addAllAttributes(metrics)
            .addAttribute("definitions", readDefinitions())
            .addAttribute("columns", mapMusicColumns(columns))
            .addAttribute("notes", mapNotes(columns))
            .addAttribute("lyrics", mapLyrics(columns))
            .addAttribute("repeats", mapRepeats(columns))
            .addAttribute("chords", mapLines(columns, n -> n.isChord()))
            .addAttribute("slurs", mapLines(columns, n -> n.isSlur()))
            .addAttribute("songs", mapSongs(columns));

        var out = new StringWriter();
        template.process(root, out);

        return out.toString();
    }

    private String readDefinitions() throws Exception {
        var xml = loadXML("kunkunshi-all.svg");
        return DefinitionExtractor.extract(xml);
    }

    private List<ModelMap> processColumn(List<Column> columns, ColumnOperation op) {
        var ret = new ArrayList<ModelMap>();
        for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            op.call(ret, column, i);
        }
        return ret;
    }

    private List<ModelMap> mapMusicColumns(List<Column> columns) {
        return processColumn(columns, (acc, column, colNo) -> {
            if (column.isMusic())
                acc.add(new ModelMap("x", columnLeft(colNo)));
        });
    }

    private List<ModelMap> mapSongs(List<Column> columns) {
        return processColumn(columns, (acc, column, colNo) -> {
            if (column.isTitle()) {
                acc.add(mapSong(colNo, column.song()));
            }
        });
    }

    private List<ModelMap> mapNotes(List<Column> columns) {
        return processColumn(columns, (acc, column, colNo) -> {
            for (var note : column.notes())
                acc.add(mapNote(colNo, note));
        });
    }

    private List<ModelMap> mapLyrics(List<Column> columns) {
        return processColumn(columns, (acc, column, colNo) -> {
            for (var lyric : column.lyrics())
                acc.add(mapLyric(colNo, lyric));
        });
    }

    private List<ModelMap> mapRepeats(List<Column> columns) {
        return processColumn(columns, (acc, column, colNo) -> {
            for (var repeat : column.repeats())
                acc.add(mapRepeat(colNo, repeat));
        });
    }

    private List<ModelMap> mapLines(List<Column> columns, Predicate<Note> predicate) {
        return processColumn(columns, (acc, column, colNo) -> {
            Note start = null;
            Note end = null;

            for (var note : column.notes()) {
                if ((start == null) == predicate.test(note)) {
                    if (predicate.test(note)) {
                        start = note;
                    } else {
                        acc.add(mapJoinLine(colNo, start, note));
                        start = null;
                    }
                }
                end = note;
            }
            if (start != null)
                acc.add(mapJoinLine(colNo, start, end));
        });
    }

    private ModelMap mapJoinLine(int colNo, Note start, Note end) {
        return new ModelMap("x", columnLeft(colNo))
            .addAttribute("startY", yPos(start))
            .addAttribute("endY", yPos(end))
            .addAttribute("startSmall", start.isSmall())
            .addAttribute("endSmall", end.isSmall());
    }

    private ModelMap mapLyric(int colNo, Lyric lyric) {
        return new ModelMap("x", columnLeft(colNo))
            .addAttribute("y", yPos(lyric))
            .addAttribute("text", lyric.getValue());
    }

    private ModelMap mapRepeat(int colNo, Repeat repeat) {
        return new ModelMap("x", columnLeft(colNo))
            .addAttribute("y", yPos(repeat))
            .addAttribute("back", repeat.isBack())
            .addAttribute("style", repeat.getStyle().name().toLowerCase());
    }

    private ModelMap mapNote(int colNo, Note note) {
        var map = new ModelMap("x", columnLeft(colNo))
            .addAttribute("y", yPos(note))
            .addAttribute("name", Symbols.noteRef(note))
            .addAttribute("small", note.isSmall());
        if (note.getAccidental() != Accidental.NONE) {
            map.addAttribute("accidental",
                note.getAccidental().name().toLowerCase(Locale.ROOT));
        }
        if (note.getUtou() != Utou.NONE) {
            map.addAttribute("articulation",
                note.getUtou().name().toLowerCase(Locale.ROOT));
        }
        return map;
    }

    private ModelMap mapSong(int colNo, Song song) {
        var map = new ModelMap("x", columnLeft(colNo));

        if (song.getTuning() != null)
            map.addAttribute("tuning", song.getTuning().getDisplayName());

        if (song.getTitleRomaji() != null)
            map.addAttribute("romaji", song.getTitleRomaji());

        map.addAttribute("title", mapJapaneseTitle(song));

        return map;
    }

    private List<ModelMap> mapJapaneseTitle(Song song) {
        var jTitle = new FuriganaString(song.getTitle());
        var parts = new ArrayList();
        for (var component : jTitle.components()) {
            var part = new ModelMap("surface", component.surface());
            if (component.reading() != null)
                part.addAttribute("reading", component.reading());
            parts.add(part);
        }

        return parts;
    }

    private double columnLeft(int colNo) {
        // Columns start from the right, but the SVG origin is top left
        int i = (int) metrics.getAttribute("columnsPerPage") - 1 - colNo;
        double width = (double) metrics.getAttribute("columnWidth")
            + (double) metrics.getAttribute("columnSpace");
        return i * width;
    }

    public double yPos(Locatable l) {
        double cellHeight = (double) metrics.getAttribute("cellHeight");
        int cellsPerCol = (int) metrics.getAttribute("cellsPerCol");
        double cellTop = cellHeight * (l.getIndex() % cellsPerCol);
        double offset = (double) l.getOffset() / (double) Locatable.CELL_TICKS * cellHeight;
        return cellTop + offset;
    }
}
