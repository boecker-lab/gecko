package gecko2.io;

import gecko2.GeckoInstance;
import gecko2.datastructures.DataSet;
import gecko2.datastructures.GeneCluster;
import org.junit.Before;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static gecko2.io.ResultWriter.exportResultsToFile;

@RunWith(Parameterized.class)
public class ResultWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    static DataSet data;
    ResultWriter.ExportType exportType;

    public ResultWriterTest(ResultWriter.ExportType exportType){
        this.exportType = exportType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> list = new ArrayList<>();
        for (ResultWriter.ExportType exportType : ResultWriter.ExportType.values())
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
