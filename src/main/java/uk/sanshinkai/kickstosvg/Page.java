package uk.sanshinkai.kickstosvg;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import javax.xml.parsers.DocumentBuilderFactory;
import org.colston.kicks.document.Accidental;
import org.colston.kicks.document.Locatable;
import org.colston.kicks.document.Lyric;
import org.colston.kicks.document.Note;
import org.colston.kicks.document.Repeat;
import org.colston.kicks.document.Song;
import org.colston.kicks.document.Utou;
import org.colston.utils.KanaConverter;
import org.w3c.dom.Document;

class Page {
    private App options;
    private List<Column> columns;
    public static final int CELLS_PER_COL = 12;

    Page(App options, List<Column> columns) {
        this.options = options;
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
        var root = new TemplateMap()
            .addAttribute("cellsPerCol", CELLS_PER_COL)
            .addAttribute("columnsPerPage", options.columnsPerPage())
            .addAttribute("cropToFit", options.cropToFit())
            .addAttribute("romajiLyrics", options.romajiLyrics())
            .addAttribute("definitions", readDefinitions())
            .addAttribute("columns", mapColumns());

        for (var entry : options.templateParams().entrySet())
            root.addAttribute(entry.getKey(), entry.getValue());

        var out = new StringWriter();
        template.process(root, out);

        return out.toString();
    }

    private String readDefinitions() throws Exception {
        var xml = loadXML("kunkunshi-all.svg");
        return DefinitionExtractor.extract(xml);
    }

    private List<Map<String, Object>> mapColumns() {
        var list = new ArrayList<Map<String, Object>>();
        for (int colNo = 0; colNo < columns.size(); colNo++) {
            var column = columns.get(colNo);
            var map = new TemplateMap("index", colNo);
            if (column.isTitle()) {
                map.addAttribute("song", mapSong(column));
            } else {
                map.addAttribute("notes", mapNotes(column))
                    .addAttribute("lyrics", mapLyrics(column))
                    .addAttribute("repeats", mapRepeats(column))
                    .addAttribute("chords", mapLines(column, Note::isChord))
                    .addAttribute("slurs", mapLines(column, Note::isSlur));
            }
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> mapNotes(Column column) {
        var list = new ArrayList<Map<String, Object>>();
        for (var note : column.notes())
            list.add(mapNote(note));
        return list;
    }

    private List<Map<String, Object>> mapLyrics(Column column) {
        var list = new ArrayList<Map<String, Object>>();
        for (var lyric : column.lyrics())
            list.add(mapLyric(lyric));
        return list;
    }

    private List<Map<String, Object>> mapRepeats(Column column) {
        var list = new ArrayList<Map<String, Object>>();
        for (var repeat : column.repeats())
            list.add(mapRepeat(repeat));
        return list;
    }

    private List<Map<String, Object>> mapLines(Column column, Predicate<Note> predicate) {
        var list = new ArrayList<Map<String, Object>>();
        Note start = null;
        Note end = null;

        for (var note : column.notes()) {
            if ((start == null) == predicate.test(note)) {
                if (predicate.test(note)) {
                    start = note;
                } else {
                    list.add(mapJoinLine(start, note));
                    start = null;
                }
            }
            end = note;
        }
        if (start != null)
            list.add(mapJoinLine(start, end));
        return list;
    }

    private Map<String, Object> mapJoinLine(Note start, Note end) {
        return new TemplateMap("top", verticalPos(start))
            .addAttribute("bottom", verticalPos(end))
            .addAttribute("startSmall", start.isSmall())
            .addAttribute("endSmall", end.isSmall());
    }

    private Map<String, Object> mapLyric(Lyric lyric) {
        var text = options.romajiLyrics()
            ? KanaConverter.toRomaji(lyric.getValue())
            : lyric.getValue();

        return new TemplateMap("pos", verticalPos(lyric))
            .addAttribute("text", text);
    }

    private Map<String, Object> mapRepeat(Repeat repeat) {
        return new TemplateMap("pos", verticalPos(repeat))
            .addAttribute("back", repeat.isBack())
            .addAttribute("style", repeat.getStyle().name().toLowerCase(Locale.ROOT));
    }

    private Map<String, Object> mapNote(Note note) {
        var map = new TemplateMap("pos", verticalPos(note))
            .addAttribute("name", Symbols.noteRef(note))
            .addAttribute("small", note.isSmall());
        if (note.getAccidental() != Accidental.NONE)
            map.addAttribute("accidental", Symbols.accidentalRef(note));
        if (note.getUtou() != Utou.NONE)
            map.addAttribute("articulation", Symbols.articulationRef(note));
        if (note.getFinger() != 0)
            map.addAttribute("finger", Symbols.fingerRef(note));
        return map;
    }

    private Map<String, Object> mapSong(Column column) {
        var song = column.song();
        var map = new TemplateMap("title", mapJapaneseTitle(song));

        if (song.getTuning() != null)
            map.addAttribute("tuning", song.getTuning().getDisplayName());

        if (song.getTitleRomaji() != null)
            map.addAttribute("romaji", song.getTitleRomaji());

        return map;
    }

    private List<Map<String, Object>> mapJapaneseTitle(Song song) {
        var jTitle = new FuriganaString(song.getTitle());
        var parts = new ArrayList();
        for (var component : jTitle.components()) {
            var part = new TemplateMap("surface", component.surface());
            if (component.reading() != null)
                part.addAttribute("reading", component.reading());
            parts.add(part);
        }

        return parts;
    }

    // Units are cells, so if there are 12 cells per column, 0.0 <= pos <= 12.0
    public double verticalPos(Locatable l) {
        double cellTop = l.getIndex() % CELLS_PER_COL;
        double offset = (double) l.getOffset() / (double) Locatable.CELL_TICKS;
        return cellTop + offset;
    }
}
