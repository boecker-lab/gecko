package de.unijena.bioinf.gecko3.io;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import de.unijena.bioinf.gecko3.GeckoInstance;
import de.unijena.bioinf.gecko3.datastructures.Genome;
import de.unijena.bioinf.gecko3.gui.GenomePainting;
import de.unijena.bioinf.gecko3.gui.util.JCheckList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * All possible export types, as used by the @link{ResultWriter}
 */
public enum ExportType {
    clusterData("txt", new NoAdditionalOptions()),
    clusterStatistics("txt", new NoAdditionalOptions()),
    table("txt", new NoAdditionalOptions()),
    geneNameTable("txt", new AdditionalGeneNameTableParameters()),
    clusterConservation("txt", new NoAdditionalOptions()),
    clusterGenomeInformation("txt", new NoAdditionalOptions()),
    referenceClusterTags("csv", new NoAdditionalOptions()),
    latexTable("tex", new NoAdditionalOptions()),
    internalDuplications("txt", new NoAdditionalOptions()),
    pdf("pdf", new AdditionalPdfOptions()),
    multiPdf("zip",new AdditionalPdfOptions());

    private final String defaultFileExtension;
    private final AdditionalExportBundle additionalExportBundle;

    public static final String types = "\"clusterData\" similar to the information in the gui.\n" +
            "\"clusterStatistics\" general statistics about all the clusters.\n" +
            "\"table\" table of cluster information, as used in Jahn et al, Statistics for approximate gene clusters, BMC Bioinformatics, 2013.\n" +
            "\"latexTable\" same as above, only latex ready.\n" +
            "\"geneNameTable\" table of all gene names in the reference occ and additional info.\n" +
            "\"clusterConservation\" information about the gene oder and additional genes for each cluster.\n" +
            "\"clusterGenomeInformation\" in which genome the cluster occurs.\n" +
            "\"referenceClusterTags\" the locus_tags of all genes in the reference occurrence.\n" +
            "\"pdf\" all clusters as a single pdf picture.\n" +
            "\"multiPdf\" a zip file containing one pdf picture for each cluster.";

    public String getDefaultFileExtension() {return defaultFileExtension;}

    ExportType(String defaultFileExtension, AdditionalExportBundle additionalExportBundle) {
        this.defaultFileExtension = defaultFileExtension;
        this.additionalExportBundle = additionalExportBundle;
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

    public Component getAdditionalOptionsPanel() {
        return additionalExportBundle.getBody();
    }
    
    public AdditionalExportParameters getAdditionalExportParameters() {
        return additionalExportBundle.getAdditionalExportParameters();
    }

    /**
     * AdditionalExportBundle for all ExportTypes, that do not have additional parameters
     */
    private static class NoAdditionalOptions implements AdditionalExportBundle {
        private JPanel body = new JPanel();

        @Override
        public AdditionalExportParameters getAdditionalExportParameters() {
            return null;
        }

        @Override
        public Component getBody() {
            return body;
        }
    }

    /**
     * AdditionalExportBundle for pdf exports. Allows to set the name type in the picture.
     */
    private static class AdditionalPdfOptions implements AdditionalExportBundle {
        private AdditionalExportParameters additionalExportParameters = new AdditionalExportParameters();
        private JPanel body;

        @Override
        public AdditionalExportParameters getAdditionalExportParameters() {
            return additionalExportParameters;
        }

        @Override
        public Component getBody() {
            if (body == null){
                DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 4dlu, p"));
                final JComboBox<GenomePainting.NameType> nameTypeComboBox = new JComboBox<>(GenomePainting.NameType.values());
                nameTypeComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            additionalExportParameters.setNameType((GenomePainting.NameType)nameTypeComboBox.getSelectedItem());
                        }
                    }
                });
                nameTypeComboBox.setSelectedItem(additionalExportParameters.getNameType());
                builder.append("Gene annotation:", nameTypeComboBox);
                body = builder.build();
            }
            return body;
        }
    }

    private static class AdditionalGeneNameTableParameters implements AdditionalExportBundle {
        private AdditionalExportParameters additionalExportParameters = new AdditionalExportParameters();

        @Override
        public AdditionalExportParameters getAdditionalExportParameters() {
            return additionalExportParameters;
        }

        @Override
        public Component getBody() {
            /**
             * We cannot store the body, as the genomes might change.
             */
            additionalExportParameters = new AdditionalExportParameters();
            DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 4dlu, p"));

            Genome[] genomes = GeckoInstance.getInstance().getGenomes();
            java.util.List<String> baseList = new ArrayList<>(genomes.length+1);
            baseList.add(AdditionalExportParameters.REFERENCE_GENOME);
            for (Genome genome : genomes)
                baseList.add(genome.getName());

            final JCheckList<String> genomeChooser = new JCheckList<>("Choose Genomes:", baseList);

            genomeChooser.setSelectedIndex(0);
            additionalExportParameters.addGenomeName(AdditionalExportParameters.REFERENCE_GENOME);

            genomeChooser.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                        additionalExportParameters.addGenomeName((String)e.getItem());
                    else if (e.getStateChange() == ItemEvent.DESELECTED)
                        additionalExportParameters.removeGenomeName((String) e.getItem());
                }
            });

            builder.append(genomeChooser);
            return builder.build();
        }
    }

    /**
     * Additional parameters for the ResultWriters, with default settings.
     */
    public static class AdditionalExportParameters {
        public static final String REFERENCE_GENOME = "Reference";
        /**
         * The NameType, default ist NameType.NAME
         */
        private GenomePainting.NameType nameType;
        private java.util.Set<String> genomeNames;

        public AdditionalExportParameters() {
            nameType = GenomePainting.NameType.NAME;
            genomeNames = new LinkedHashSet<>();
            genomeNames.add(REFERENCE_GENOME);
        }

        public GenomePainting.NameType getNameType() {
            return nameType;
        }

        private void setNameType(GenomePainting.NameType nameType) {
            this.nameType = nameType;
        }

        public java.util.Set<String> getGenomeNames() {
            return genomeNames;
        }

        private void addGenomeName(String name) {
            genomeNames.add(name);
        }

        private void removeGenomeName(String name) {
            genomeNames.remove(name);
        }
    }

    /**
     * Interface that bundles AdditionalExportParameters with a Gui to set the parameters
     */
    private interface AdditionalExportBundle {
        /**
         * Returns the AdditionalExportParameters that can be set by the gui
         * @return
         */
        public AdditionalExportParameters getAdditionalExportParameters();

        /**
         * The Component used to set the AdditionalExportParameters
         * @return
         */
        public Component getBody();
    }
}
