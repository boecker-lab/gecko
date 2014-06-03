USER MANUAL
-----------

1. Installation

Java 7 (http://www.java.com/) is needed to run Gecko2

2. Gecko2

Given a set of genomes in which each gene is assigned to a family
of homologous genes, Gecko2 detects sets of genes that appear in an
approximately conserved neighborhood among the genomes.
A typical Gecko session is divided into three parts: genome selection,
cluster detection, manual evaluation of predictions. These parts are
described in the following in more detail.

Gecko2 can either be run in commandline mode, '-h' will give all possible
parameters. Or it can be used in GUI mode, if started without any
parameters.

2.1 Data

For Gecko2, the basic requirement is that the genomes are given as
sequences of strings where each character represents a certain family
containing at least one gene. All
genes in a family should be homologs performing the same (or very
similar) function.

Input files have the file extension '.cog' and have to be
organized as follows:

GenomeName <COMMA> Descriptive Text  <NEWLINE>
Descriptive Text (ignored) <NEWLINE>
<Genome Content> <NEWLINE>

Where in <Genome Content> each line contains information about the
family and function of single genes in the order of their
occurrence in the genome in one of two different formats:

<Homology> <TAB> Strand (+ or -) <TAB> functional category <TAB>
Gene Name <TAB> functional annotation <NEWLINE>

or

<Homology> <TAB> Strand (+ or -) <TAB> functional category <TAB>
Gene Name <TAB> functional annotation <TAB> Locus Tag <TAB> product <NEWLINE>

<Homology> can be any word or number, all genes with the same entry
will be in one homology family. All genes with the empty string or
0 will be treated as un-homologue.

Example from the file COG_data.cog

Aquifex aeolicus, complete genome - 0..1551335
1529 proteins
0480    +   J   fusA    elongation factor EF-G
0050    +   J   tufA1   elongation factor EF-Tu
0051    +   J   rpsJ    ribosomal protein S10
            ...
0459    +   O   mopA    GroEL
0000    -   -   ----    putative protein
0612    -   R   ymxG    processing protease

Clostridium acetobutylicum ATCC824, complete genome - 0..3940880
3672 proteins
0593    +   L   dnaA    DNA replication initiator protein, ATPase
0592    +   L   dnaN    DNA polymerase III beta subunit
2501    +   S   ----    Small conserved protein, ortholog of YAAA B.subtilis

After selecting an input file via 'File'->'Open session or genome file',
Gecko determines automatically from the file ending whether it loads a
genome file (.cog) or a stored session (.gck). In case a genome file is
selected, it is parsed and all found chromosomes are listed in a table.
Ticking the check boxes next to a chromosomes in the table, one can
choose the chromosomes that should be part of the search for approximate
gene clusters. Different chromosomes of one genome can be marked and
grouped by clicking on the 'Group' button. Gecko2 suggests a grouping of
chromosomes based on chromosome names. This can be reverted by marking
the grouped chromosomes and clicking on the 'Ungroup' button. Genome
selection is finished by clicking on the button 'OK'. The genomes are
then visualized in a genome browser, allowing to inspect the genomes,
contained genes, and gene annotations.


4.2 Cluster Detection

When clicking the 'start computation" button, the user is asked to
select a search mode, as well as global and model-dependent parameters
before the actual search begins. The first step is to choose between the
median, center, and reference gene cluster model.
For all models, the minimum cluster size, the maximum distance and optionally a quorum
parameter can be set.
The minimum cluster size defines the minimum number of genes that a gene
cluster must contain to be reported by the program.
The distance threshold, gives an upper bound on the number of
differences that are allowed between a gene set and its approximate
occurrences. In reference mode, the value determines the
maximum pair-wise distance between the reference set, and each approximate occurrence.
The third parameter determines the minimum number of genomes in which a gene cluster must
have an approximate occurrence in order to be reported. By default, this
value is set to the number of selected genomes. Then, only gene clusters
with an approximate occurrence in all genomes are reported.
In case the reference mode is selected, one can chose between three sub-modes. In
the 'all against all' mode, gene cluster are predicted using all input
genomes one after the other as reference genome. In the 'fixed genome'
mode only one genome is used as reference. It can be chosen from a
drop-down list containing the previously selected genomes. In the
'manual cluster' mode, a sequence of genes can be typed in manually, or
pasted when e. g. copying a cluster from the result list of a previous
run of Gecko2.  After all parameters are set, computation can be started
by clicking the 'OK' button.

4.3 Graphical Evaluation

After completion of computations, results are shown in tabular form
below the genome browser. The table contains the list of all predicted
gene clusters, listing the number of genes, the number of included
genomes, the score of the best occurrence combination (negative
logarithm of p-value, and negative logarithm of FDR corrected p-value)
and a list with the IDs of all involved genomes.
Under the table different filter modes for the results can be choosen.
Either all gene clusters are shown (showAll), for all overlapping occurrences,
only the best scoring one is reported (showFiltered), or only selected
clusters are shown. Clusters can be selected by right clicking on the table columns.
By default, the gene cluster list is sorted by decreasing score.  A gene
cluster can be selected with a double-click on the entry -- its best
occurrence will then be visualized by the genome browser, and details
about the cluster will be displayed in an information area. Additional
(and, if enabled by the user, also suboptimal) occurrences can be
selected with navigation buttons next to the genomes.

The visualization of a selected gene cluster has been optimized to allow
for an easy inspection of the gene cluster -- the genome browser allows
to visualize the neighborhood on each genome, mouse over tooltips provide
the user with the annotation data available for genes or chromosomes,
and the information area allows for a more detailed inspection of the
search result. It is possible to filter for clusters containing
individual genes or functional gene annotations by typing the respective
information into the 'Search' field above the genome browser.

The results of a Gecko2 session can be stored in a file with ending
'.gck' via 'File'->'Save session'.

Results can be exported via 'File'->'Export results' in different data formats.
1. A simple <Tab> separated table
2. A latex table
3. A pdf with pictures of all clusters (not all pdf viewers will be able to open them)
4. A zip file with one pdf per cluster



