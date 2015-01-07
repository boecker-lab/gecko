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
