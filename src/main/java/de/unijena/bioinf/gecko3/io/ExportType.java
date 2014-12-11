package de.unijena.bioinf.gecko3.io;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

/**
 * All possible export types, as used by the @link{ResultWriter}
 */
public enum ExportType {
    clusterData("txt", noAdvancedOptions()),
    clusterStatistics("txt", noAdvancedOptions()),
    table("txt", noAdvancedOptions()),
    geneNameTable("txt", noAdvancedOptions()),
    clusterConservation("txt", noAdvancedOptions()),
    clusterGenomeInformation("txt",noAdvancedOptions()),
    referenceClusterTags("csv", noAdvancedOptions()),
    latexTable("tex", noAdvancedOptions()),
    internalDuplications("txt", noAdvancedOptions()),
    pdf("pdf", advancedPdfOptions()),
    multiPdf("zip",advancedPdfOptions());

    private final String defaultFileExtension;
    private final Component advancedOptionsPanel;

    public static final String types = "\"clusterData\" similar to the information in the gui.\n" +
            "\"clusterStatistics\" general statistics about all the clusters.\n" +
            "\"table\"\n table of cluster information, as used in Jahn et al, Statistics for approximate gene clusters, BMC Bioinformatics, 2013." +
            "\"latexTable\" same as above, only latex ready.\n" +
            "\"geneNameTable\" table of all gene names in the reference occ and additional info.\n" +
            "\"clusterConservation\" information about the gene oder and additional genes for each cluster.\n" +
            "\"clusterGenomeInformation\" in which genome the cluster occurs.\n" +
            "\"referenceClusterTags\" the locus_tags of all genes in the reference occurrence.\n" +
            "\"pdf\"\n all clusters as a single pdf picture." +
            "\"multiPdf a zip file containing one pdf picture for each cluster.\"";

    public String getDefaultFileExtension() {return defaultFileExtension;}

    ExportType(String defaultFileExtension, Component advancedOptionsPanel) {
        this.defaultFileExtension = defaultFileExtension;
        this.advancedOptionsPanel = advancedOptionsPanel;
    }

    /**
     * Wrapper method for values() that only returns the currently supported subset of values
     * @return the supported subset of values
     */
    public static ExportType[] getSupported() {
        // Support all values
        return values();

        // Support only a subset of values
        //return new ExportType[]{clusterData, table, latexTable, pdf, multiPdf};
    }

    public Component getAdvancedOptionsPanel() {
        return advancedOptionsPanel;
    }

    private static Component noAdvancedOptions() {
        return new JPanel();
    }

    private static Component advancedPdfOptions() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p"));
        builder.append("PDF");
        return builder.build();
    }
}
