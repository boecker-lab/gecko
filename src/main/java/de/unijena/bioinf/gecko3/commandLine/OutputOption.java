package de.unijena.bioinf.gecko3.commandLine;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.io.ResultWriter;

import java.io.File;

/**
 * Output options, to export gene clusters to text or picture
 */
public class OutputOption {
    private final File file;
    private final ResultWriter.ExportType type;
    private final GeckoInstance.ResultFilter filter;

    public OutputOption(File file, ResultWriter.ExportType type, GeckoInstance.ResultFilter filter) {
        this.file = file;
        this.type = type;
        this.filter = filter;
    }

    public File getFile() {
        return file;
    }

    public ResultWriter.ExportType getType() {
        return type;
    }

    public GeckoInstance.ResultFilter getFilter() {
        return filter;
    }
}
