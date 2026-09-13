package uk.sanshinkai.kickstosvg;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.UseDefaultConverter;

@Command(
    name = "main",
    description = "Render kicks file(s) to SVG"
)
public class App implements Callable<Integer> {
    @Option(
        names = { "-o", "--output-dir" },
        paramLabel = "PATH",
        description = "Where to write output files"
    )
    private String outputDir = ".";

    @Option(
        names = { "-s", "--suffix" },
        paramLabel = "STRING",
        description = "Suffix to be added to file names (before the page number)"
    )
    private String fileSuffix = "";

    @Option(
        names = { "-C", "--columns-per-page" },
        paramLabel = "N",
        description = "Number of columns per page"
    )
    private int columnsPerPage = 11;

    @Option(
        names = { "-c", "--crop-to-fit" },
        description = "Crop SVG to the minimum size needed for the music"
    )
    private boolean cropToFit = false;

    @Option(
        names = { "-P", "--template-parameter" },
        converter = {UseDefaultConverter.class, TemplateParamConverter.class},
        paramLabel = "KEY=VALUE",
        description = "Set a parameter to be passed to the template"
    )
    private Map<String, Object> templateParams = new HashMap<String, Object>();

    @Parameters(
        paramLabel = "FILE",
        description = "One or more files to be rendered"
    )
    private String[] inputPaths = {};

    public String fileSuffix() {
        if (this.fileSuffix == "") return "";
        else return "-" + this.fileSuffix;
    }

    public int columnsPerPage() {
        return this.columnsPerPage;
    }

    public boolean cropToFit() {
        return this.cropToFit;
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
