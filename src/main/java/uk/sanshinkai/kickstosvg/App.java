package uk.sanshinkai.kickstosvg;

import java.util.concurrent.Callable;
import java.util.Properties;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "kickstosvg",
    description = "Render kicks file(s) to SVG"
)
public class App implements Callable<Integer> {
    @Mixin Options options = new Options();
    final Properties properties = new Properties();

    @Override
    public Integer call() throws Exception {
        properties.load(getClass().getClassLoader()
            .getResourceAsStream("project.properties"));

        if (options.getShowVersion())
            System.out.printf("%s %s%n",
                properties.getProperty("artifactId"),
                properties.getProperty("version"));

        for (var inputPath : options.getInputPaths())
            new Renderer(inputPath, options.getOutputDir(), options).call();

        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
