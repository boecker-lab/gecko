/*
 * Copyright 2014 Sascha Winter, Tobias Mann, Hans-Martin Haase, Leon Kuchenbecker and Katharina Jahn
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

package de.unijena.bioinf.gecko3.io;

import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.DataSet;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.unijena.bioinf.gecko3.io.ResultWriter.exportResultsToFile;

@RunWith(Parameterized.class)
public class ResultWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    static DataSet data;
    ExportType exportType;

    public ResultWriterTest(ExportType exportType){
        this.exportType = exportType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> list = new ArrayList<>();
        for (ExportType exportType : ExportType.values())
            list.add(new Object[]{exportType});
        return list;
    }

    @BeforeClass
    public static void createTestData() throws IOException, ParseException {
        File gckFile = new File(ResultWriterTest.class.getResource("/statisticsClusters.gck").getFile());
        GckFileReader gckFileReader = new GckFileReader(gckFile);
        data = gckFileReader.readData();
        GeckoInstance.getInstance().setGeckoInstanceData(data);
    }

    @Test
    public void testResultOutput() throws IOException{
        File file = folder.newFile(exportType.toString());
        exportResultsToFile(file, data.getClusters(), data.getGenomeNames(), exportType);
    }
}
