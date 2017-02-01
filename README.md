Manual for Sequence Search Tool for Antimicrobial Resistance (SSTAR), version 1.1
=================================================================================

Table of Contents
=================

* [Table of Contents](#table-of-contents)
  * [Introduction](#introduction)
  * [Two SSTAR versions](#two-sstar-versions)
  * [Obtaining and installing SSTAR dependencies](#obtaining-and-installing-sstar-dependencies)
  * [Input data](#input-data)
  * [Running SSTAR](#running-sstar)
  * [BLAST output file produced by SSTAR](#blast-output-file-produced-by-sstar)
  * [Planned features](#planned-features)
  * [Citing SSTAR](#citing-sstar)
  * [Contact](#contact)

Introduction
------------
SSTAR enables fast and accurate antimicrobial resistance (AR) surveillance from Whole Genome Sequencing (WGS) data. It is able to identify known AR genes and detect putative new variants as well as truncated genes due to internal stop codons. 
SSTAR also reports modifications and/or truncations in outer membrane porins.

Two SSTAR versions
-----------------------------------------------------------------
**In your downloaded archive you will find two different SSTAR versions**

1.	**SSTAR.jar** is for Linux and OS X 
2.	**SSTAR_windows.jar** is for Windows 

**JAR and Java files**

* Each SSTAR version is available as an executable JAR file. Double click on this file and the SSTAR tool will pop up on your screen
* The raw Java files are available as well for those people who want to explore the source code
* SSTAR was successfully tested under Windows 7 and Windows 10, OS X 10.9.5 and Ubuntu 14.04 LTS

Obtaining and installing SSTAR dependencies
-------------------------------------------
SSTAR combines a standalone BLASTN with a Java interface, which operates under Windows and Unix systems. 
In order to run SSTAR under Windows you need Java Runtime Environment (JRE) 6 or newer and standalone BLAST 2.2.29+, BLAST 2.2.30+ or BLAST 2.2.31+. 
SSTAR for Linux and OS X is compatible with JRE6 or newer and all BLAST+ versions.

**For Windows users**
* BLAST+ needs to be installed in C:\\Program Files\\NCBI\\blast-2.2.29+, C:\\Program Files\\NCBI\\blast-2.2.30+ or C:\\Program Files\\NCBI\\blast-2.2.31+, which are the default locations after following the BLAST installation steps in the installation wizard

**For OS X and Linux users**
* BLAST+ can be installed anywhere. Please add the two lines listed below to your **.bash_profile** or **.profile** depending on your shell, and operating system
```bash
    export BLASTN="blastn"
    export MAKEBLASTDB="makeblastdb"
```

Load your **.bash_profile** or **.profile** into the current shell or command prompt

```bash
    source .bash_profile
    OR
    source .profile
```

You can check if your variables were exported correctly using the **echo** command and the commands should return "blastn" and "makeblastdb" 
```bash
    echo $BLASTN
    echo $MAKEBLASTDB
```



**For all users**
* Java can be downloaded from the Oracle website at: https://java.com/en/download/index.jsp
* A BLAST+ copy and easy installation guide of standalone BLAST can be retrieved from: http://www.blaststation.com/freestuff/en/howtoNCBIBlastWin.html

Input data
----------
One needs two input files in order for SSTAR to run: A microbial genome assembly and AR gene file, both in FASTA format. SSTAR is developed in a certain way so it can handle the ‘SRST2 database header’ format. Two AR database files are included with SSTAR, a SRST2 modified ARG-ANNOT database and a SRST2 modified Resfinder database. 
This format is specified below.

**The AR gene file header format**

The format of each AR gene FASTA header is structured like the below example:

**92__CMY_Bla__CMY-37__402**

The AR gene family is between the first and second double underscore (CMY_Bla) and the variant is between the second and third double underscore (CMY-37).
The first number (92) is a unique identifier for each AR gene group. The last number (402) is a unique identifier for each single variant. The other information in the header (right of the space) is ignored by SSTAR and not shown in this manual example.

Users who want to use different AR databases (ResFinder or custom databases) need to make sure the headers have the exact same structure as the SRST2 header format. 

Running SSTAR
-------------
SSTAR contains an easy interface with currently only four buttons. The top two buttons are for uploading the genome assembly file and the AR gene database file. Both files need to be in FASTA format. 
One needs to enter a sequence similarity percentage value that serves as cut-off for detecting potential new variants of AR genes. A value between 80 and 99% is recommended.
The ‘Identify resistance genes’ button starts the actual AR gene annotation process. The genes will be listed in the the second output window.

**AR gene output**

The second output window displays the AR resistance genes and porins that are identified on your input genome assembly. Each output line contains five fields, separated by a tab:

1.	The AR gene name
2.	The contig, scaffold or chromosome where the AR gene is located
3.	Sequence similarity 
4.	Alignment length
5.	AR gene length

First, SSTAR lists the potential new alleles or variants at the top of the window. These are the variants that share between **X%** and **99.99%** sequence similarity with an AR gene in the used database. The user is free to pick a value for **X**.
Below the potential new variants SSTAR lists the AR genes that share 100% sequence similarity with AR genes in the used database. The alignment length shows the user how much of the AR database gene is found on your genome. In other words, when the alignment length equals the gene length one identified the full AR gene with 100% sequence similarity.

**The export button will export the data as a tab delimited file and can then be opened in a spreadsheet, such as Microsoft Excel. The file is stored in the same directory as the input genome assembly file**

**Detecting new and truncated AR gene variants**

The bottom output window will show putative new variants and truncated enzymes in protein space. The protein sequences can be exported to a plain text file, in FASTA format, using the export button. The file is saved in the same directory as the input genome assembly file. 
The protein file can be used with BLASTP against the NR database of NCBI for detecting new variants. Potential novel beta-lactamase proteins can be submitted to the NCBI (http://www.ncbi.nlm.nih.gov/pathogens/submit_beta_lactamase/) for verification. 
Translated start codons (Methionines, M) are capitalized so the user gets a better idea where the protein starts. Not all proteins start with an M, however in that case SSTAR will report the ORF with the fewest internal stop codons. 
When a protein sequence contains an internal stop codon it will be flagged underneath the FASTA header of that particular protein. This makes the FASTA file invalid and forces the user to remove that sequence from the file. Protein sequences with internal stop codons are otherwise easily missed and misinterpreted as putative new variants of an AR gene group.

**Detecting modified and truncated outer membrane porin sequences**

The bottom output window will also show modified and/or truncated outer membrane porins (OMPs). We have included OmpK35, OmpK36 and OmpK37 from Klebsiella pneumoniae, OmpC and OmpF from Escherichia coli, OmpC and OmpF from Enterobacter cloacae and Omp35 and Omp36 from Enterobacter earogenes. 
When a porin protein sequence contains an internal stop codon it will be flagged underneath the FASTA header of that particular porin as truncated. 

BLAST output file produced by SSTAR
------------------------------
The results that are generated by SSTAR are shown in the graphical interface as explained in the previous section, however SSTAR is also producing a raw BLASTN file in the same folder as your input data:

The BLASTN file is in tabular form and each line represents an allele that was part of your query database. A line contains 13 fields and are briefly described here:

1. 	Query **(An antimicrobial gene allele)**
2. 	Target sequence **(A contig, scaffold or chromosome of your input genome file)**
3. 	% ID
4. 	Alignment length
5. 	Number of mismatches
6. 	Number of gap opens
7. 	Start position in query 
8. 	End position in query 
9. 	Start position in target
10. 	End position in target
11. 	E-value 
12. 	Bit score
13. 	Query length 


Planned features
----------------
1.	Multiple genome assembly file upload (multithreading)
2.	Compatibility with other BLAST+ versions (Windows version only, Unix version works with all BLAST versions already)
3.	An eraser button for all three output windows
4.	A method to use two AR gene databases (ResFinder and ARG-ANNOT) simultaneously
5.	Add HMMER functionality for detecting new genes
6.	Build a command line version of SSTAR for Linux and OS X

Citing SSTAR
------------
**Please cite our paper in mSphere:** de Man TJB, Limbago BM. 2016. SSTAR, a stand-alone easy-to-use antimicrobial resistance gene predictor.
mSphere 1(1): e00050-15

**When using the ARG-ANNOT database please also cite:** Gupta SK, Padmanabhan BR, Diene SM, Lopez-Rojas R, Kempf M, Landraud L, Rolain J-M. 2014. ARG-ANNOT (Antibiotic Resistance Gene-ANNOTation), a new bioinformatic tool to discover antibiotic resistance genes in bacterial genomes. 
Antimicrobial Agents and Chemotherapy 58:212–220.

**When using the ResFinder database please also cite:** Zankari E, Hasman H, Cosentino S, Vestergaard M, Rasmussen S, Lund O, Aarestrup F, Larsen MV. 2012. Identification of acquired antimicrobial resistance genes. 
Journal of Antimicrobial Chemotherapy 67:2640–2644.

Contact
-------
For assistance, feedback or suggestions please contact Tom de Man via xku6@cdc.gov or tjb.deman@gmail.com
 




