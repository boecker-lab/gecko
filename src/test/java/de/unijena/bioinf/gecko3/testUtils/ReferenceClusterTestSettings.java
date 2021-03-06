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

package de.unijena.bioinf.gecko3.testUtils;

import de.unijena.bioinf.gecko3.algo.GeneClusterTestUtils;
import de.unijena.bioinf.gecko3.datastructures.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sascha Winter (sascha.winter@uni-jena.de)
 */
public class ReferenceClusterTestSettings {
    public Parameter p;
    public String referenceGenome;
    public File dataFile;
    public File expectedResultFile;
    public File resultOutputFile;
    public List<Set<Integer>> genomeGroups;

    public static ReferenceClusterTestSettings memoryReductionDataD2S4Q2() {
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                2,
                4,
                2,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.allAgainstAll
        );
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/memoryReductionData.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/memoryReductionDataD2S4Q2.gck") != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/memoryReductionDataD2S4Q2.gck").getFile());

        settings.resultOutputFile = new File("src/test/resources/memoryReductionDataD2S4Q2.gck");
        settings.genomeGroups = null;
        return settings;
    }

    public static ReferenceClusterTestSettings memoryReductionBugD2S5Q2() {
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                2,
                5,
                2,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.allAgainstAll
        );
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/memoryReductionBug.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/memoryReductionBugD2S5Q2.gck") != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/memoryReductionBugD2S5Q2.gck").getFile());

        settings.resultOutputFile = new File("src/test/resources/memoryReductionBugD2S5Q2.gck");
        settings.genomeGroups = null;
        return settings;
    }

    public static ReferenceClusterTestSettings memoryReductionWithSuboptimalOccurrenceD3S5() {
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                3,
                5,
                2,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.genome
        );
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/mRWithSuboptimalOccurrence.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/mRWithSuboptimalOccurrenceD3S5.gck") != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/mRWithSuboptimalOccurrenceD3S5.gck").getFile());

        settings.resultOutputFile = new File("src/test/resources/mRWithSuboptimalOccurrenceD3S5.gck");
        settings.genomeGroups = null;
        return settings;
    }

    public static ReferenceClusterTestSettings fiveProteobacterDeltaTable() {
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                Parameter.DeltaTable.test_five_proteobacter.getDeltaTable(),
                4,
                4,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.allAgainstAll
        );
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/fiveProteobacterDeltaTable.gck") != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacterDeltaTable.gck").getFile());

        settings.resultOutputFile = new File("src/test/resources/fiveProteobacterDeltaTable.gck");
        settings.genomeGroups = null;
        return settings;
    }

    public static ReferenceClusterTestSettings fiveProteobacterD3S6Q4() {
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                3,
                6,
                4,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.allAgainstAll
        );
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/fiveProteobacterD3S6Q4.gck") != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacterD3S6Q4.gck").getFile());

        settings.resultOutputFile = new File("src/test/resources/fiveProteobacterD3S6Q4.gck");
        settings.genomeGroups = null;
        return settings;
    }

    public static ReferenceClusterTestSettings fiveProteobacterD3S6Q2Grouping() {
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                3,
                6,
                2,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.allAgainstAll
        );
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacter.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/fiveProteobacterD3S6Q2Grouping.gck") != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/fiveProteobacterD3S6Q2Grouping.gck").getFile());

        settings.resultOutputFile = new File("src/test/resources/fiveProteobacterD3S6Q2Grouping.gck");
        settings.genomeGroups = new ArrayList<>(3);
        Set<Integer> set1 = new HashSet<>();
        set1.add(0);
        settings.genomeGroups.add(set1);
        Set<Integer> set2 = new HashSet<>();
        set2.add(1);
        set2.add(2);
        settings.genomeGroups.add(set2);
        Set<Integer> set3 = new HashSet<>();
        set3.add(3);
        set3.add(4);
        settings.genomeGroups.add(set3);
        return settings;
    }

    public static ReferenceClusterTestSettings statisticsDataD5S8Q10EColiRef() {
        final String fileName = "statisticsDataD4S8Q25EColiRef.gck";
        ReferenceClusterTestSettings settings = new ReferenceClusterTestSettings();
        settings.p = new Parameter(
                4,
                8,
                25,
                Parameter.OperationMode.reference,
                Parameter.ReferenceType.genome
        );
        settings.referenceGenome = "Escherichia_coli_str_K-12_substr_MG1655";
        settings.dataFile = new File(GeneClusterTestUtils.class.getResource("/statistics.cog").getFile());

        settings.expectedResultFile = null;
        if (GeneClusterTestUtils.class.getResource("/"+fileName) != null)
            settings.expectedResultFile = new File(GeneClusterTestUtils.class.getResource("/"+fileName).getFile());

        settings.resultOutputFile = new File(fileName);
        settings.genomeGroups = null;
        return settings;
    }
}
