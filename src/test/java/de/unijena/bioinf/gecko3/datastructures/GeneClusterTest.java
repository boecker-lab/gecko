package de.unijena.bioinf.gecko3.datastructures;

import de.unijena.bioinf.gecko3.io.GckFileReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class GeneClusterTest {
    @Test
    public void readMultiGeneFamilyCluster()  throws IOException, ParseException {
        File gckFile = new File(getClass().getResource("/multiGeneFamilyClusterTest.gck").getFile());

        GckFileReader gckFileReader = new GckFileReader(gckFile);

        DataSet data = gckFileReader.readData();
        assertTrue(data.getClusters().get(0).invalidMultiGeneFamilyGeneCluster(4, data.getGenomes()));
        assertFalse(data.getClusters().get(1).invalidMultiGeneFamilyGeneCluster(4, data.getGenomes()));
    }
}
