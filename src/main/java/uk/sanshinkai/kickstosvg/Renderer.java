package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import com.google.common.collect.Lists;
import freemarker.template.Template;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.persistence.DocumentStoreFactory;

class Renderer implements Callable<Boolean> {
    private String inputPath;
    private String outputDir;
    private App options;

    Renderer(String inputPath, String outputDir, App options) {
        this.inputPath = inputPath;
        this.outputDir = outputDir;
        this.options = options;
    }

    private KicksDocument loadDocument(File file) throws Exception {
        if (!file.exists() || !file.canRead())
            throw new Exception("Cannot read file: " + file.getAbsolutePath());

        var store = DocumentStoreFactory.create(file);

        if (!store.isPresent())
            throw new Exception("Unknown file: " + file.getAbsolutePath());

        return store.get().load(file);
    }

    @Override
    public Boolean call() throws Exception {
        var template = FreeMarker.getTemplate("template.ftlx");
        var pages = paginateMusic(loadDocument(new File(inputPath)));

        for (var i = 0; i < pages.size(); i++) {
            var svg = renderPage(template, pages.get(i));
            var filename = generateFilename(i + 1, pages.size());
            var out = new PrintWriter(filename);
            try {
                if (options.printFilenames()) System.out.println(filename);
                System.err.printf("Writing page %d to %s%n", i + 1, filename);
                out.print(svg);
            } finally {
                out.close();
            }
        }

        return true;
    }

    private List<List<Column>> paginateMusic(KicksDocument music) {
        var columns = new Columnizer(options).columnize(music);
        return Lists.partition(columns, options.columnsPerPage());
    }

    private String renderPage(Template template, List<Column> columns) throws Exception {
        var root = new Page(options, columns).build();
        var out = new StringWriter();
        template.process(root, out);
        return SVGOptimizer.optimize(out.toString());
    }

    private String generateFilename(int pageNo, int pageCount) throws Exception {
        var suffix = options.fileSuffix();
        if (pageCount > 1)
            suffix += String.format(Locale.ROOT, "-%02d", pageNo);
        suffix += ".svg";

        var inputFilename = Path.of(inputPath).getFileName();
        if (inputFilename == null)
            throw new Exception("Input path is empty");

        var name = inputFilename.toString()
            .replaceFirst("\\.[^\\.]+$|$", suffix);

        return Path.of(outputDir, name).toString();
    }
}
