package uk.sanshinkai.kickstosvg;

import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.UseDefaultConverter;

class Options {
    @Option(
        names = { "-c", "--crop-to-fit" },
        description = "Crop SVG to the minimum size needed for the music"
    )
    private boolean cropToFit = false;
    public boolean getCropToFit() { return this.cropToFit; }
    public void setCropToFit(boolean b) { this.cropToFit = b; }

    @Option(
        names = { "-C", "--columns-per-page" },
        paramLabel = "N",
        description = "Number of columns per page"
    )
    private int columnsPerPage = 11;
    public int getColumnsPerPage() { return this.columnsPerPage; }
    public void setColumnsPerPage(int i) { this.columnsPerPage = i; }

    @Option(
        names = { "-h", "--help" },
        usageHelp = true,
        description = "Display this help and exit"
    )
    boolean help = false;
    public boolean getHelp() { return this.help; }
    public void setHelp(boolean b) { this.help = b; }

    @Option(
        names = { "-o", "--output-dir" },
        paramLabel = "PATH",
        description = "Where to write output files. Default is current working directory"
    )
    private String outputDir = ".";
    public String getOutputDir() { return this.outputDir; }
    public void setOutputDir(String s) { this.outputDir = s; }

    @Option(
        names = { "-P", "--template-parameter" },
        converter = {UseDefaultConverter.class, TemplateParamConverter.class},
        paramLabel = "KEY=VALUE",
        description = "Set a parameter to be passed to the template"
    )
    private Map<String, Object> templateParameters = new HashMap<String, Object>();
    public Map<String, Object> getTemplateParameters() {
        return Map.copyOf(this.templateParameters);
    }
    public void setTemplateParameters(Map<String, Object> m) {
        this.templateParameters = Map.copyOf(m);
    }

    @Option(
        names = { "--print-filenames" },
        description = "Print filenames to standard out. Useful for feeding to another tool"
    )
    private boolean printFilenames = false;
    public boolean getPrintFilenames() { return this.printFilenames; }
    public void setPrintFilenames(boolean b) { this.printFilenames = b; }

    @Option(
        names = { "-r", "--romaji-lyrics" },
        description = "Convert the lyrics to romaji"
    )
    private boolean romajiLyrics = false;
    public boolean getRomajiLyrics() { return this.romajiLyrics; }
    public void setRomajiLyrics(boolean b) { this.romajiLyrics = b; }

    @Option(
        names = { "-s", "--filename-suffix" },
        paramLabel = "STRING",
        description = "Suffix to be added to file names (before the page number)"
    )
    private String filenameSuffix = "";
    public String getFilenameSuffix() { return this.filenameSuffix; }
    public void setFilenameSuffix(String s) { this.filenameSuffix = s; }

    @Option(
        names = { "--stylesheet" },
        paramLabel = "FILE",
        description = "Stylesheet to insert into SVG"
    )
    private String stylesheet = "";
    public String getStylesheet() { return this.stylesheet; }
    public void setStylesheet(String s) { this.stylesheet = s; }

    @Option(
        names = { "--show-titles" },
        negatable = true,
        defaultValue = "true",
        fallbackValue = "true",
        description = "Show title columns. True by default"
    )
    private boolean showTitles = true;
    public boolean getShowTitles() { return this.showTitles; }
    public void setShowTitles(boolean b) { this.showTitles = b; }

    @Parameters(
        paramLabel = "FILE",
        description = "One or more files to be rendered"
    )
    private String[] inputPaths = {};
    public String[] getInputPaths() { return this.inputPaths; }
    public void setInputPaths(String[] a) { this.inputPaths = a; }
}
