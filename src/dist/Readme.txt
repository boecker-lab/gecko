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

USER MANUAL
-----------

CONTACT
For support and suggestions, sascha.winter(at)uni-jena.de

1. INSTALLATION

Java 7 is needed to run Gecko3.
If you have a 64-Bit operations system, 64-Bit Java is highly suggested.
Java 7 and installation instructions are available from http://www.java.com or http://openjdk.java.net.

1.1 JAVA INSTALLATION

Java 7 and installation instructions are available from http://www.java.com/de/download/manual.jsp
or http://openjdk.java.net.

You can check if and which version of java you have installed from the command line with "java -version".
The first line should give you the java version, for java 7 something like <java version "1.7.0_67"> (the
second number gives the java version).

1.1.1 WINDOWS: Oracle Java is recommended. Follow this link (http://www.java.com/de/download/manual.jsp)
and select Windows Offline (64-Bit) (If you have a 32-Bit operation system, you need to choose
Windows Offline (32-Bit), but will most likely not have enough memory available to use Gecko3 with a large data).
Download and afterwards execute the file to install Java.

1.1.2 LINUX: OpenJDK 7 is recommended. How to install Java depends on your specific linux distribution.
You should be able to install Java 7 or Java 8 from your package management system.
Note: The Oracle Java 8 Version that is available for Ubuntu causes display errors in Gecko3.

1.1.3 MAC OS X:
Mac OS X >= 10.7.3: Oracle Java is recommended. Follow this link (http://www.java.com/de/download/manual.jsp)
and select Mac OS X. Download and afterwards execute the file to install Java.

Mac OS X < 10.7.3: Java 7 installation is not possible.
If you have Lion Mac OS X 10.7.1 or 10.7.2 you need to upgrade to 10.7.3.

2. USING GECKO 3

Given a set of genomes in which each gene is assigned to a family
of homologous genes, Gecko3 detects sets of genes that appear in an
approximately conserved neighborhood among the genomes.

A typical Gecko session is divided into three parts: genome selection,
cluster detection, manual evaluation of predictions. These parts are
described in the following in more detail.

2.1 STARTING GECKO 3

For a 64-Bit operation system, 64-Bit Java is highly recommended.
Windows: To start Gecko3, go to the bin directory and execute Gecko3.bat
For Linux and OS X: To start Gecko3, go to the bin directory and execute Gecko3

If you have only 32-Bit Java, there are also 32-Bit starters contained:
Windows: To start Gecko3 in 32-Bit mode, go to the bin directory and execute Gecko3-32bit.bat
For Linux and OS X: To start Gecko3 in 32-Bit mode, go to the bin directory and execute Gecko3-32bit

If Gecko3 does not start, run the appropriate file from the command line, to get error messages.
Most likely, you have no Java or 32-Bit Java and try to run the normal 64-Bit Gecko3 version.

Gecko3 can either be run in commandline mode, '-h' will give all possible
parameters, that you can, or need to supply.
Or it can be used in GUI mode, if started without any
parameters, or just a *.gck or *.cog input file that will be automatically loaded.

By default, Gecko3 will use a maximum heap size of 6GB (-Xmx6G), set in
the Gecko3.bat and Gecko3 start scripts. This is sufficient to work with about 1000 Genomes.
If you need more memory, modify the start script with any text editor, changing
-Xmx6G to an appropriate value (e.g. -Xmx12G).

2.2 INPUT DATA FORMAT

For Gecko3, the basic requirement is that the genomes are given as
sequences of strings where each character represents a certain family
containing at least one gene. All genes in a family should be homologs
performing the same (or very similar) function.

Genome input files have the file extension '.cog' and have to be
organized as follows:

<Genome Data>
Empty Line
<GenomeData>
Empty Line
...

With <Genome Data> being:

GenomeName <COMMA> Descriptive Text  <NEWLINE>
Descriptive Text (ignored) <NEWLINE>
<Genome Content> <NEWLINE>

Where in <Genome Content> each line contains information about the
family and function of single genes in the order of their
occurrence in the genome in one of two different formats
(if unique locus tags are available, the second format is preferred):

<Homology> <TAB> Strand (+ or -) <TAB> functional category <TAB> Gene Name <TAB> functional annotation <NEWLINE>

or

<Homology> <TAB> Strand (+ or -) <TAB> functional category <TAB> Gene Name <TAB> functional annotation <TAB> Locus Tag <TAB> product <NEWLINE>

<Homology> can be any word or number, not containing ",". All genes with the same entry
will be in one homology family. All genes with the empty string or
0 will be treated as un-homologue. Multiple gene families can be assigned to one gene
(see Example 2, second gene), as comma separated entries. Gecko will for visualisation
and computation split the one gene with multiple gene families into multiple gene with
one gene family each, in the order given in the .cog file.
Locus Tag should be an unique tag for each gene in the data set.

Example 1 for the format without locus tag:

Aquifex aeolicus, complete genome - 0..1551335
1529 proteins
0480    +   J   fusA    elongation factor EF-G
0050    +   J   tufA1   elongation factor EF-Tu
0051    +   J   rpsJ    ribosomal protein S10
            ...
0459    +   O   mopA    GroEL
0000    -   -   ----    putative protein
0612    -   R   ymxG    processing protease

Example 2 for the format with locus tag:

Escherichia coli O127:H6 str. E2348/69 chromosome, complete genome.
4552 proteins
0	+	?	thrL	involved in threonine biosynthesis; controls the expression of the thrLABC operon	E2348C_0001	unknown
COG0527,COG0460	+	?	thrA	multifunctional homotetrameric enzyme that catalyzes the phosphorylation of aspartate to form aspartyl-4-phosphate as well as conversion of aspartate semialdehyde to homoserine; functions in a number of amino acid biosynthetic pathways	E2348C_0002	unknown
COG0083	+	?	thrB	catalyzes the formation of O-phospho-L-homoserine from L-homoserine in threonine biosynthesis from asparate	E2348C_0003	unknown
COG0498	+	?	thrC	catalyzes the formation of L-threonine from O-phospho-L-homoserine	E2348C_0004	unknown
NOG76743	+	?	yaaX	hypothetical protein	E2348C_0005	unknown
COG3022	-	?	yaaA	hypothetical protein	E2348C_0006	unknown
            ...

2.3 IMPORTING DATA

After selecting an input file via 'File'->'Open session or genome file',
Gecko determines automatically from the file ending whether it loads a
genome file (.cog) or a stored session (.gck). In case a genome file is
selected, it is parsed and all found chromosomes are listed in a table.
Ticking the check boxes next to a chromosomes in the table, one can
choose the chromosomes that should be part of the search for approximate
gene clusters. Different chromosomes of one genome can be marked and
grouped by clicking on the 'Group' button. Gecko3 suggests a grouping of
chromosomes based on chromosome names. This can be reverted by marking
the grouped chromosomes and clicking on the 'Ungroup' button. Genome
selection is finished by clicking on the button 'OK'. The genomes are
then visualized in a genome browser, allowing to inspect the genomes,
contained genes, and gene annotations.


2.4 CLUSTER DETECTION

When clicking the 'start computation" button, the user is asked to
select a search mode, as well as global and model-dependent parameters
before the actual search begins.
In the simple "Single Distance" mode the minimum cluster size and the maximum distance
have to be set. The distance threshold determines the maximum pair-wise distance
between the reference set, and each approximate occurrence. The minimum size gives the minimum
number of genes a gene cluster has to contain, to be reported.
As an alternative in "Distance Table" mode, for each size, a maximum number of gene losses, a maximum number of
gene insertions and the maximum sum of losses and insertions can be set. A right click in the table allows to
add or delete rows or reset the table to some parameters we used in different publications.
For both modes, the minimum number of genomes a cluster has to appear in (quorum parameter)
can be set. By default, this value is set to the number of selected genomes. Then, only gene clusters
with an approximate occurrence in all genomes are reported.

Additional, one can chose between three sub-modes. In
the 'all against all' mode, gene cluster are predicted using all input
genomes one after the other as reference genome. In the 'fixed genome'
mode only one genome is used as reference. It can be chosen from a
drop-down list containing the previously selected genomes. You can filter the list by typing in the text field.
In the 'manual cluster' mode, a sequence of genes can be typed in manually, or
pasted when e. g. copying a cluster from the result list of a previous
run of Gecko3.

Finally, by checking "Search Ref. in Ref.", each reference genome will also search for occurrences of the cluster in
the reference genome.

After all parameters are set, computation can be started by clicking the 'OK' button.

2.5 GRAPHICAL EVALUATION

After completion of computations, results are shown in tabular form
below the genome browser. The table contains the list of all predicted
gene clusters, listing a unique id, the number of genes, the number of included
genomes, the score of the best occurrence combination (negative
logarithm of p-value, and negative logarithm of FDR corrected p-value)
and a list with the gene families of the genes in the reference occurrence.
By default, the gene cluster list is sorted by decreasing score, but clicking on the table header
will sort or inverse sort the clusters according to the selected column.
A gene cluster can be selected with a double-click on the entry -- its best
occurrence will then be visualized by the genome browser, and details
about the cluster will be displayed in an information area. Additional
(and, if enabled by the user, also suboptimal) occurrences can be
selected with navigation buttons next to the genomes.
On top of the genome browser, one can choose to hide un-clustered genomes.
By default a chosen cluster will be centered on the screen. To manually align a cluster,
you can scroll or drag and drop each genome browser to the left or right. SHIFT + Double Click
on a genome browser will invert the genome. A Double Click on a single gene will align
the cluster using this gene family as an anchor.

The visualization of a selected gene cluster has been optimized to allow
for an easy inspection of the gene cluster -- the genome browser allows
to visualize the neighborhood on each genome, mouse over tooltips provide
the user with the annotation data available for genes or chromosomes,
and the information area allows for a more detailed inspection of the
search result.

2.6 FILTERING AND SEARCHING

Under the table different filter modes for the results can be chosen.
Either all gene clusters are shown ("showAll"), for all overlapping occurrences,
only the best scoring one is reported ("showFiltered"), or only selected
clusters are shown. Clusters can be selected by right clicking on the table columns and
choosing "Add to selection" or "Add all in list to selection".

In the top right corner of the gui, it is possible to filter for clusters containing
individual genes or functional gene annotations by typing the respective information
into the 'Search' field above the genome browser.

In front of each genome in the genome browser, one can choose between "None", "Include" and "Exclude".
"None" means no additional filtering. With "Include" the Gene Cluster Table will only contain clusters,
that include the genome, when choosing "Exclude", the table will only contain clusters, that do not contain
the genome.

By right Clicking on a gene cluster in the table, one can choose "Show similar clusters". This will automatically
enter all gene families from this cluster into the search field.

2.7 SAVING A GECKO 3 SESSION

The results of a Gecko3 session can be stored in a file with ending
'.gck' via 'File'->'Save session'.

2.8 EXPORTING CLUSTERS

Results can be exported via 'File'->'Export results' in different data formats.
1. "clusterData" similar to the information in the gui.
2. "clusterStatistics" general statistics about all the clusters.
3. "table" table of cluster information, as used in Jahn et al, Statistics for approximate gene clusters, BMC Bioinformatics, 2013.
4. "latexTable" same as above, only latex ready.
5. "geneNameTable" table of all gene names in the reference occ and additional info.
6. "clusterConservation" information about the gene oder and additional genes for each cluster.
7. "clusterGenomeInformation" in which genome the cluster occurs.
8. "referenceClusterTags" the locus_tags of all genes in the reference occurrence.
9. "pdf" all clusters as a single pdf picture(not all pdf viewers will be able to open this, due to the size).
10. "multiPdf" a zip file containing one pdf picture for each cluster.

If you right click on a single cluster in the table, you can select "Export gene cluster" to export this single cluster
as a picture (pdf, jpg or png).



