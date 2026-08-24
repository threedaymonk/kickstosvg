package uk.sanshinkai.kickstosvg;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import uk.sanshinkai.kickstosvg.Renderer;

@Command(
    name = "main",
    description = "Render kicks file(s) to SVG"
)
public class App implements Callable<Integer> {
    @Option(
        paramLabel = "<path>",
        names = { "-o", "--output-dir" },
        defaultValue = "current working directory",
        description = "Where to write output files"
    )
    private String outputDir = ".";

    @Parameters(
        paramLabel = "<input>",
        description = "Files to be rendered"
    )
    private String[] inputPaths = {};

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello world");
        for (var inputPath : inputPaths) {
            new Renderer(inputPath, outputDir).call();
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
