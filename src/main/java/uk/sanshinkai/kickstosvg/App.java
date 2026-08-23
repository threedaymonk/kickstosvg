package uk.sanshinkai.kickstosvg;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "main",
    description = "Convert input file(s) to SVG"
)
public class App implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("Hello world");
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
