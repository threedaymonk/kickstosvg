package uk.sanshinkai.kickstosvg;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;
import com.google.common.collect.Lists;
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
        var music = loadDocument(new File(inputPath));
        var columns = new Columnizer(options).columnize(music);

        var pages = Lists.partition(columns, options.columnsPerPage());

        for (var i = 0; i < pages.size(); i++) {
            var xml = new Page(options, pages.get(i)).render();
            var filename = generateFilename(i + 1);
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

    private String generateFilename(int pageNo) throws Exception {
        var suffix = String.format(Locale.ROOT, "%s-%02d.svg", options.fileSuffix(), pageNo);
        var inputFilename = Path.of(inputPath).getFileName();
        if (inputFilename == null)
            throw new Exception("Input path is empty");
        var name = inputFilename.toString()
            .replaceFirst("\\.[^\\.]+$|$", suffix);
        return Path.of(outputDir, name).toString();
    }
}
