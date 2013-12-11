package gecko2.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GckFileReaderTest {

    private static void testReadingDataWithoutClusters(File cogInfile, File gckInfile) throws IOException, ParseException {
        CogFileReader cogReader = new CogFileReader(cogInfile);


        cogReader.readData();


        GckFileReader gckReader = new GckFileReader(gckInfile);


        gckReader.readData();


        assertEquals(cogReader.getMaxIdLength(), gckReader.getMaxIdLength());
        assertEquals(cogReader.getMaxLocusTagLength(), gckReader.getMaxLocusTagLength());
        assertEquals(cogReader.getMaxNameLength(), gckReader.getMaxNameLength());

        assertArrayEquals(cogReader.getGenomes(), gckReader.getGenomes());
        assertEquals(cogReader.getGeneLabelMap(), gckReader.getGeneLabelMap());
    }

    @Test
    public void readFileStatisticsData()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/statisticsData.cog").getFile());
        File gckFile = new File(getClass().getResource("/statisticsData.gck").getFile());

        testReadingDataWithoutClusters(cogFile, gckFile);
    }

    @Test
    public void readFileSmallData()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/smallReaderTest.cog").getFile());
        File gckFile = new File(getClass().getResource("/smallReaderTest.gck").getFile());

        testReadingDataWithoutClusters(cogFile, gckFile);
    }

    @Test
    public void readFileStringDBPartialData()  throws IOException, ParseException{
        File cogFile = new File(getClass().getResource("/stringDBPartial.cog").getFile());
        File gckFile = new File(getClass().getResource("/stringDBPartial.gck").getFile());

        testReadingDataWithoutClusters(cogFile, gckFile);
    }


}
