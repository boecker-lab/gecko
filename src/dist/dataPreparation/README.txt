USER MANUAL
-----------

WARNING: This script will, without warning delete files and subdirectory in the given directory!

INSTALLATION

To run this python scrip, you need to have Python 2 (www.python.org) and Biopython (http://biopython.org) installed.

Linux - Unix:
You probably already have python installed or can install it and Biopython using our package management system.

Windows - Mac:
One easy (and free) way to install python and Biopython is Anaconda (https://store.continuum.io/cshop/anaconda/).
Once you have installed Anaconda, you can do all the following in the Anaconda Command Promt.
To install Biopython, simply execute "conda install biopython"

To assign gene similarities and ultimately gene families, this script use BLAST+ and Transclust.
BLAST+ can be downloaded from http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastDocs&DOC_TYPE=Download.
If you have BLAST+ available on a compute cluster, you can also skip the local installation and let this tool
generate the commands to run blast on the cluster.
The standalone Transclust can be downloaded from http://transclust.mmci.uni-saarland.de/main_page/index.php and needs Java to run.

USING THIS SCRIPT

1.  Start with an empty directory, or an directory only containing this script (called <WD> from now on).
    This script will, without warning delete files and subdirectory in the given directory!

2.  For each genome place one or more GenBank file in this directory. This files have to end with .gb
    This script will use the CDS entries for the genes. The CDS entries need to contain a locus_tag, and a translation that
    contains the protein sequence.

3.  You can now choose to run a local blast search, or run the blast search on another machine, that has BLAST+ installed,
    either as a single blast job, or one blast job per genome (this are linux bash scripts, but the commands should also work in windows.

3a. To run a local search, start the script with "python gecko3_gb_to_transclust_to_cog.py -doLocalBlast <WD>"

    or

3b. To prepare a remote BLAST+ search start the script with "python gecko3_gb_to_transclust_to_cog.py -prepareSingleBlast <WD>"
    Copy the complete directory to the machine you want to run BLAST+ on. The directory contains a file myBlastAll.sh,
    that contains the needed commands to run BLAST+. When you are done, you can copy the resulting *.blast2p file to your
    <WD>.

    or

3c. To prepare a remote BLAST+ search start the script with " python gecko3_gb_to_transclust_to_cog.py -prepareSplitBlast <WD>"
    Copy the complete directory to the machine you want to run BLAST+ on. The directory contains a file "makeBlastDb.sh",
    that has to be executed first, to generate a blast database.
    Additionally the directory contains multiple files named *_vs_all.sh, that contain the needed commands
    to run BLAST+ for a single genome against all other genomes. When you are done, copy the resulting *.blast2p files
    to your <WD>. Run "python gecko3_gb_to_transclust_to_cog.py -mergeBlast <WD>".

4.  Start Transclust in gui mode (e.g. "java -Xmx6G -jar Tranclust.jar -gui", the -Xmx6G will allow Tranclust to use 6 GB of memory, adjust
    the number to your system). In the menu, select File->Load->BLAST/FASTA file. Select merged.blast2p as blast and merged.faa as
    fasta file. Set the BLAST cutoff to 0.01 (1.0E-2) and start. For the clustering tab, set the density parameter in From and To to either 35.0 (optimistic)
    or 48.0 (conservative), as suggested in
    Density parameter estimation for finding clusters of homologous proteins—tracing actinobacterial pathogenicity lifestyles.
    Roettger R, Kalaghatgi P, Sun P, Soares S, Azevedo V, Wittkop T, Baumbach J
    Bioinformatics (2013) 29 (2): 215-222.
    Click start clustering. Once clustering is done, in the menu File->Save->Save results file and save the results as
    transclustResults.txt in the <WD>.
    If you select different values for From and To, clusterings for multiple density parameters will be generated and all be
    stored in the same file. In the next step, only the first density parameter will be used!

5.  Run "python gecko3_gb_to_transclust_to_cog.py --transclustToCogOptimistic <WD>"
    or
    "python gecko3_gb_to_transclust_to_cog.py --transclustToCogConservative <WD>"
    to generated the gecko3 input file transclust.cog.

sampleData.zip contains multiple GenBank files for Synechocystis and M. tuberculosis and the resulting cog file
for density parameter 48.
