#!/usr/bin/env python
from Bio import SeqIO
import os
import subprocess
import sys

__author__ = 'swinter'


def gb_to_fasta(infile):
    outfile = os.path.splitext(infile)[0]+".faa"
    with open(infile, "r") as input_handle:
        with open(outfile, "w") as output:
            for seq_record in SeqIO.parse(input_handle, "genbank"):
                print "Dealing with GenBank record %s" % seq_record.id
                for seq_feature in seq_record.features:
                    if not validFeature(seq_feature):
                        continue
                    output.write(">%s|%s\n" % (
                        getGeneIdentifier(seq_feature),
                        seq_record.id))
                    seq = seq_feature.qualifiers['translation'][0]
                    while len(seq) > 80:
                        output.write(seq[:80]+"\n")
                        seq = seq[80:]
                    if len(seq) > 0:
                        output.write(seq.strip()+"\n")
    return outfile


def make_blast_db(faa_files,  db_name="db", title="transclust"):
    fasta_files = ' '.join(faa_files)
    subprocess.call(["makeblastdb", "-dbtype", "prot", "-title", title, "-out", db_name, "-in", fasta_files])
    return db_name

def make_blast_db_command_string(faa_files, db_name="db", title="transclust"):
    fasta_files = ' '.join(faa_files)
    return "makeblastdb -dbtype prot -title %s -out %s -in %s\n" %(title, db_name, fasta_files)


def make_blast_command_string(faaFile, dbName, evalue, num_threads):
    return "blastp -db %s -query %s -evalue %f -outfmt 6 -num_threads %d > %s\n" % (dbName, faaFile, evalue, num_threads,os.path.splitext(faaFile)[0]+"_vs_all.blast2p")


def generate_blastAll_command(faaFiles, dbName, workingDir, evalue=0.01, num_threads=4):
    bashFile = os.path.join(workingDir, "myBlastAll.sh")
    with open(bashFile, "w") as output:
        output.write("#!/bin/bash\n")
        for faaFile in faaFiles:
            blastCommand = make_blast_command_string(faaFile, dbName, evalue, num_threads)
            output.write(blastCommand)
            with open(os.path.join(workingDir, os.path.splitext(faaFile)[0]+"_vs_all.sh"), "w") as singleScript:
                singleScript.write("#!/bin/bash\n")
                singleScript.write(blastCommand)


def getAllFiles(workingDir, ending, exclude=None):
    files =[]
    for f in os.listdir(workingDir):
        if os.path.isfile(os.path.join(workingDir, f)) and os.path.splitext(f)[1] == ending:
            if not exclude or not os.path.abspath(f) == os.path.abspath(exclude):
                files.append(os.path.join(workingDir, f))
    return files


def prepareBlast(workingDir):
    genbankFiles = getAllFiles(workingDir, ".gb")

    faaFiles = []
    for file in genbankFiles:
        faaFiles.append(gb_to_fasta(file))

    dbName = make_blast_db(faaFiles)

    generate_blastAll_command(faaFiles, dbName, workingDir)


def mergeBlast(workingDir):
    outfile = os.path.join(workingDir, "merged.blast2p")
    with open(outfile, "w") as output:
        blast2pFiles = getAllFiles(workingDir, ".blast2p", outfile)
        for f in blast2pFiles:
            print "Processing %s" % f
            with open(f, "r") as infile:
                for line in infile:
                    output.write(line)
    outfile = os.path.join(workingDir, "merged.faa")
    with open(outfile, "w") as output:
        fastaFiles = getAllFiles(workingDir, ".faa", outfile)
        for f in fastaFiles:
            print "Processing %s" % f
            with open(f, "r") as infile:
                for line in infile:
                    output.write(line)


def extractGeneFamiliesForParameter(workingDir, parameter):
    datafile = os.path.join(workingDir, "transclustResults.txt")
    if not os.path.exists(datafile):
        sys.exit("Transclust result file %s missing!" % datafile)

    genomeGeneFamilyDict = dict()
    i = 1
    with open(datafile, "r") as data:
        for line in data:
            split = line.strip().split("\t")
            if int(float(split[0])) == parameter:
                geneFamilies = split[2].split(";")
                for geneFamily in geneFamilies:
                    genes = geneFamily.split(",")
                    if len(genes) == 1:
                        familyId = 0
                    else:
                        familyId = i
                        i += 1
                    for gene in genes:
                        geneSplit = gene.split("|")
                        if len(geneSplit) > 2:
                            sys.exit("Maleformed gene: %s" % gene)
                        if not geneSplit[1] in genomeGeneFamilyDict:
                            genomeGeneFamilyDict[geneSplit[1]] = dict()
                        if geneSplit[0] in genomeGeneFamilyDict[geneSplit[1]]:
                            sys.exit("Duplicate Gene %s: %d and %d" %(gene, familyId, genomeGeneFamilyDict[geneSplit[1]][geneSplit[0]]))
                        genomeGeneFamilyDict[geneSplit[1]][geneSplit[0]] = familyId
    return genomeGeneFamilyDict


def extractGeneFamilySizePerParamter(datafile):
    parameterGeneFamilies = dict()
    i=1
    with open(datafile, "r") as data:
        for line in data:
            split = line.strip().split("\t")
            parameter = int(float(split[0]))
            parameterGeneFamilies[parameter] = dict()
            geneFamilies = split[2].split(";")
            for geneFamily in geneFamilies:
                genes = geneFamily.split(",")
                parameterGeneFamilies[parameter][i] = genes
                i += 1
    return parameterGeneFamilies


def statistics(workingDir):
    datafile = os.path.join(workingDir, "transclustResults.txt")
    if not os.path.exists(datafile):
        sys.exit("Transclust result file %s missing!" % datafile)
    geneFamiliesPerParameter = extractGeneFamilySizePerParamter(datafile)

    # generate mapping
    sizeMap = dict()
    for parameter in sorted(geneFamiliesPerParameter):
        for family in geneFamiliesPerParameter[parameter].values():
            if not len(family) in sizeMap:
                sizeMap[len(family)] = dict()

            if not parameter in sizeMap[len(family)]:
                sizeMap[len(family)][parameter] = 1
            else:
                sizeMap[len(family)][parameter] += 1
    # print mapping
    print "\t"+"\t".join(str(x) for x in sorted(geneFamiliesPerParameter))
    for key in sorted(sizeMap):
        print str(key)+"\t" + "\t".join(str(sizeMap[key].get(x, 0)) for x in sorted(geneFamiliesPerParameter))


def validFeature(seq_feature):
    if not seq_feature.type == "CDS":
        return False
    if not len(seq_feature.qualifiers['locus_tag'])==1:
        print seq_feature
        sys.exit("Error missing locus_tag")
    if not "translation" in seq_feature.qualifiers:
        print "Skipping %s, no translation contained!" % seq_feature.qualifiers['locus_tag'][0]
        return False
    if not len(seq_feature.qualifiers['translation'])==1:
        print seq_feature
        sys.exit("Error in translation")
    return True


def getGeneIdentifier(seq_feature):
    return seq_feature.qualifiers['locus_tag'][0]


def printFeature(name, positionMap, output_handle):
    output_handle.write(name+"\n")
    output_handle.write("%d proteins\n" % len(positionMap))
    lastPosition=(-1,-1)
    for key in sorted(positionMap.keys()):
        if max(lastPosition) > min(key):
            print "Overlapping positions, %s / %s" % (lastPosition, key)
        lastPosition = key
        output_handle.write(positionMap[key][0])
    output_handle.write("\n")


def mapGeneFamiliesToGenBank(genBankFile, geneFamilies, minGenes):
    namePositionMap = dict()
    with open(genBankFile, "r") as input_handle:
        print "Parsing %s" % genBankFile
        for seq_record in SeqIO.parse(input_handle, "genbank"):

            if not seq_record.id in geneFamilies:
                print "Skipping %s, missing in geneFamilies!" % seq_record.id
            elif len(geneFamilies[seq_record.id]) < minGenes:
                print "Skipping %s, only %d genes, threshold is %d" % (seq_record.id, len(geneFamilies[seq_record.id]), minGenes)
            else:
                positionMap = dict()
                print "Dealing with GenBank record %s" % seq_record.id
                for seq_feature in seq_record.features:
                    if not validFeature(seq_feature):
                        continue
                    if not getGeneIdentifier(seq_feature) in geneFamilies[seq_record.id]:
                        print "Missing gene %s, assigning 0 homology!" % getGeneIdentifier(seq_feature)
                        geneID = 0
                    else:
                        geneID = geneFamilies[seq_record.id][getGeneIdentifier(seq_feature)]
                    position = ((int(seq_feature.location.nofuzzy_start)), (int(seq_feature.location.nofuzzy_end)))
                    if position[0] < position[1]:
                        strand = "+"
                    else:
                        strand = "-"
                    if "gene" in seq_feature.qualifiers:
                        geneName = seq_feature.qualifiers["gene"][0]
                    else:
                        geneName = ""
                    if "note" in seq_feature.qualifiers:
                        note = seq_feature.qualifiers["note"][0]
                    else:
                        note = ""
                    product = seq_feature.qualifiers["product"][0]
                    locusTag = seq_feature.qualifiers["locus_tag"][0]
                    positionMap[position] = ("%d\t%s\t%s\t%s\t%s\t%s\t%s\n" % (geneID, strand, "?", geneName, product, locusTag, "unknown"), note)
                if seq_record.description in namePositionMap:
                    sys.exit("Duplicate entries for %s" % seq_record.description)
                namePositionMap[seq_record.description] = positionMap
    return namePositionMap


def mapGeneFamiliesToGeneBankAndPrintToCog(genBankFiles, geneFamilies, outfile, minGenes=1):
    with open(outfile, "w") as output:
        for genBankFile in genBankFiles:
            namePositionMap = mapGeneFamiliesToGenBank(genBankFile, geneFamilies, minGenes)
            for name, positionMap in namePositionMap.items():
                printFeature(name, positionMap, output)


def transclustToCog(workingDir, parameter=48):
    geneFamilies = extractGeneFamiliesForParameter(workingDir, parameter)
    genBankFiles = getAllFiles(workingDir, ".gb")
    mapGeneFamiliesToGeneBankAndPrintToCog(genBankFiles, geneFamilies, outfile=os.path.join(workingDir, "transclust.cog"))


def getGeneFamilyAnnotations(genBankFiles, geneFamilies):
    idToGenomeToAnnotationAll = dict()
    for genBankFile in genBankFiles:
        with open(genBankFile, "r") as input_handle:
            for seq_record in SeqIO.parse(input_handle, "genbank"):
                if not seq_record.id in geneFamilies:
                    print "Skipping %s" % seq_record.id
                else:
                    print "Dealing with GenBank record %s" % seq_record.id
                    for seq_feature in seq_record.features:
                        if not validFeature(seq_feature):
                            continue
                        if not getGeneIdentifier(seq_feature) in geneFamilies[seq_record.id]:
                            #print "Missing gene %s, skipping!" % getGeneIdentifier(seq_feature)
                            continue
                        geneID = geneFamilies[seq_record.id][getGeneIdentifier(seq_feature)]
                        if "gene" in seq_feature.qualifiers:
                            assert len(seq_feature.qualifiers["gene"]) == 1
                            geneName = seq_feature.qualifiers["gene"][0]
                        else:
                            geneName = ""
                        if "note" in seq_feature.qualifiers:
                            assert len(seq_feature.qualifiers["note"]) == 1
                            note = seq_feature.qualifiers["note"][0]
                        else:
                            note = ""
                        assert len(seq_feature.qualifiers["product"]) == 1
                        product = seq_feature.qualifiers["product"][0]
                        assert len(seq_feature.qualifiers["locus_tag"]) == 1
                        locusTag = seq_feature.qualifiers["locus_tag"][0]
                        if not geneID in idToGenomeToAnnotationAll:
                            idToGenomeToAnnotationAll[geneID] = dict()
                        if not genBankFile in idToGenomeToAnnotationAll[geneID]:
                            idToGenomeToAnnotationAll[geneID][genBankFile] = []
                        idToGenomeToAnnotationAll[geneID][genBankFile].append((geneName, product, note, locusTag))
    return idToGenomeToAnnotationAll


def writeNonAnnotatedGene(output, locusTag):
    pass
    #output.write("%s\t----\n\n" % locusTag)


def writeSingleAnnotationValues(annotations, output, locusTag, columnToWrite=0):
    differentValues = dict()
    for genome, annotationList in annotations.items():
        for valueTupel in annotationList:
            value = valueTupel[columnToWrite]
            if not value:
                continue
            if not value in differentValues:
                differentValues[value] = []
            differentValues[value].append(valueTupel)
            if len(differentValues) == 0:
                writeNonAnnotatedGene(output, locusTag)
                continue
            #if len(differentValues) > 1:
                #continue
            output.write("%s\n" % locusTag)
            for annotation, supportingGenomes in differentValues.items():
                output.write("%s\t%s\n" %(annotation, supportingGenomes))
            output.write("\n")


def writeAnnotationForGenome(annotations, output, locusTag, genomeFilter="ecoli_ec.gb"):
    for genome, annotationsList in annotations.items():
        if genomeFilter in genome:
            output.write("%s\n" % locusTag)
            for annotation in annotationsList:
                output.write("\t".join(annotation)+"\n")
            output.write("\n")


def mapAnnotationsToGenome(fileToAnnotate, geneFamilies, idToGenomeToAnnotation, outfile):
    with open(outfile, "w") as output:
        with open(fileToAnnotate, "r") as input_handle:
            for seq_record in SeqIO.parse(input_handle, "genbank"):
                if not seq_record.id in geneFamilies:
                    print "Skipping %s" % seq_record.id
                else:
                    print "Dealing with GenBank record %s" % seq_record.id
                    for seq_feature in seq_record.features:
                        if not validFeature(seq_feature):
                            continue
                        locusTag = seq_feature.qualifiers["locus_tag"][0]
                        if not getGeneIdentifier(seq_feature) in geneFamilies[seq_record.id]:
                            writeNonAnnotatedGene(output, locusTag)
                            continue
                        geneID = geneFamilies[seq_record.id][getGeneIdentifier(seq_feature)]
                        if not geneID in idToGenomeToAnnotation:
                            writeNonAnnotatedGene(output, locusTag)
                            continue
                        annotations = idToGenomeToAnnotation[geneID]
                        #writeSingleAnnotationValues(annotations, output, locusTag)
                        writeAnnotationForGenome(annotations, output, locusTag)


def getCoreGeneAnnotations(idToGenomeToAnnotation, nrOfGenomes):
    coreGenes = dict()
    for family, genomeAnnotationMapping in idToGenomeToAnnotation.iteritems():
        if len(genomeAnnotationMapping) == nrOfGenomes:
            length = 0
            for annotations in genomeAnnotationMapping.values():
                length += len(annotations)
            if length <= 1.5*nrOfGenomes:
                coreGenes[family] = genomeAnnotationMapping
    return coreGenes


def annotateGenome(workingDir, parameter=48):
    fileToAnnotate = os.path.join(workingDir, "morax_mx.gb")
    outfile = os.path.join(workingDir, "annotations.txt")

    seq_recordIds = []
    with open(fileToAnnotate, "r") as input_handle:
        for seq_record in SeqIO.parse(input_handle, "genbank"):
            seq_recordIds.append(seq_record.id)
    geneFamilies = extractGeneFamiliesForParameter(workingDir, parameter)
    genBankFiles = getAllFiles(workingDir, ".gb")
    genBankFiles.remove(fileToAnnotate)

    idToGenomeToAnnotation = getGeneFamilyAnnotations(genBankFiles, geneFamilies)
    coreGenes = getCoreGeneAnnotations(idToGenomeToAnnotation, len(genBankFiles))

    mapAnnotationsToGenome(fileToAnnotate, geneFamilies, coreGenes, outfile)

options = {
    "-prepareBlast": prepareBlast,
    "-mergeBlast": mergeBlast,
    "-statistics": statistics,
    "-transclustToCog": transclustToCog,
   # "-annotateGenome": annotateGenome,
}


def printUsage():
    print "Needs exactly 2 input parameters, a command and a working directory containing all the genbank input files (ending with .gb)."
    print "That directory should not contain any other files!"
    print "Files in the directory will be deleted without warning!"
    print "Valid commands are:"
    for key in options:
        print key


def main():
    if len(sys.argv) != 3 :
        printUsage()
        sys.exit("Invalid number of parameters!")
    command = sys.argv[1]
    if not command in options:
        sys.exit("Invalid command!")
    workingDir = sys.argv[2]
    if not os.path.exists(workingDir) or not os.path.isdir(workingDir):
        sys.exit("Directory %s not found!" % workingDir)

    options[command](workingDir)

if __name__ == "__main__":
    main()