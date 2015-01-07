/*
 * Copyright 2014 Sascha Winter
 *
 * This file is part of Gecko3.
 *
 * Gecko3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gecko3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Gecko3.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unijena.bioinf.gecko3.commandLine;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.io.ExportType;
import de.unijena.bioinf.gecko3.io.ResultWriter;

import java.io.File;

/**
 * Output options, to export gene clusters to text or picture
 */
public class OutputOption {
    private final File file;
    private final ExportType type;
    private final GeckoInstance.ResultFilter filter;

    public OutputOption(File file, ExportType type, GeckoInstance.ResultFilter filter) {
        this.file = file;
        this.type = type;
        this.filter = filter;
    }

    public File getFile() {
        return file;
    }

    public ExportType getType() {
        return type;
    }

    public GeckoInstance.ResultFilter getFilter() {
        return filter;
    }
}
