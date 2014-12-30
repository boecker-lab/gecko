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

3.  You can now choose to run a local blast search, or run the blast search on another machine, that has BLAST+ installed.

3a. To run a local search, start the script with "python gecko3_gb_to_transclust_to_cog.py -doLocalBlast <WD>"
 or
3b. To prepare a remote BLAST+ search start the script with "python gecko3_gb_to_transclust_to_cog.py -prepareBlast <WD>"
    Copy the complete directory to the machine you want to run BLAST+ on. The directory contains a file myBlastAll.sh,
    that contains the needed commands to run BLAST+. When you are done, you can copy the resulting *.blast2p files to your
    <WD>. Run "python gecko3_gb_to_transclust_to_cog.py -mergeBlast <WD>"

4.  Start Transclust (e.g. "java -Xmx6G -jar Tranclust.jar", the -Xmx6G will allow Tranclust to use 6 GB of memory, adjust
    the number to your system).
