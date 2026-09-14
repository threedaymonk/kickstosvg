package uk.sanshinkai.kickstosvg;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.UseDefaultConverter;

@Command(
    name = "kickstosvg",
    description = "Render kicks file(s) to SVG"
)
public class App implements Callable<Integer> {
    @Option(
        names = { "-c", "--crop-to-fit" },
        description = "Crop SVG to the minimum size needed for the music"
    )
    private boolean cropToFit = false;

    @Option(
        names = { "-C", "--columns-per-page" },
        paramLabel = "N",
        description = "Number of columns per page"
    )
    private int columnsPerPage = 11;

    @Option(
        names = { "-h", "--help" },
        usageHelp = true,
        description = "Display this help and exit"
    )
    boolean help;

    @Option(
        names = { "-o", "--output-dir" },
        paramLabel = "PATH",
        description = "Where to write output files"
    )
    private String outputDir = ".";

    @Option(
        names = { "-P", "--template-parameter" },
        converter = {UseDefaultConverter.class, TemplateParamConverter.class},
        paramLabel = "KEY=VALUE",
        description = "Set a parameter to be passed to the template"
    )
    private Map<String, Object> templateParams = new HashMap<String, Object>();

    @Option(
        names = { "-r", "--romaji-lyrics" },
        description = "Convert the lyrics to romaji"
    )
    private boolean romajiLyrics = false;

    @Option(
        names = { "-s", "--suffix" },
        paramLabel = "STRING",
        description = "Suffix to be added to file names (before the page number)"
    )
    private String fileSuffix;

    @Option(
        names = { "--stylesheet" },
        paramLabel = "FILE",
        description = "Stylesheet to insert into SVG"
    )
    private String stylesheetPath;
    private String stylesheet;

    @Option(
        names = { "--titles" },
        negatable = true,
        defaultValue = "true",
        fallbackValue = "true",
        description = "Show title columns. True by default"
    )
    private boolean showTitles = true;

    @Parameters(
        paramLabel = "FILE",
        description = "One or more files to be rendered"
    )
    private String[] inputPaths = {};

    public int columnsPerPage() { return this.columnsPerPage; }
    public boolean cropToFit() { return this.cropToFit; }
    public boolean romajiLyrics() { return this.romajiLyrics; }
    public boolean showTitles() { return this.showTitles; }

    public String fileSuffix() {
        if (this.fileSuffix == null) return "";
        return "-" + this.fileSuffix;
    }

    public String stylesheet() throws Exception {
        if (this.stylesheetPath == null) return null;

        if (this.stylesheet == null)
            this.stylesheet = new String(Files.readAllBytes(Paths.get(stylesheetPath)));
        return this.stylesheet;
    }

    public Map<String, Object> templateParams() {
        return Map.copyOf(this.templateParams);
    }

    @Override
    public Integer call() throws Exception {
        for (var inputPath : inputPaths) {
            new Renderer(inputPath, outputDir, this).call();
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
