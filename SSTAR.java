//Copyright (C) 2015 Tom de Man
//This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2 of the license, or any later version

//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
//of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. 

//You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software 
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

//Email: xku6@cdc.gov or tjb.deman@gmail.com

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;
import java.util.*;


public class SSTAR extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel information, empty;
	private JFileChooser fileChooserAssembly, fileChooserEnzyme;
	private JTextField assemblySelection, enzymeSelection;
	private JTextArea progessOutput, enzymeOutput, proteinSeqOutput;
	private JButton geneDetectButton, exportButton, assemblyUploadButton, enzymeUploadButton;
	private PrintWriter outFasta;
	
	//save enzyme class, contig, start and stop position 
	private HashMap <String, String> newEnzymeCoordinate = new HashMap<String, String>();
	
	//link cluster number with variant family
	private HashMap <String, String> clusterNrlinkVariantFam = new HashMap<String, String>();
	
	//variable for saving the enzyme family name of potential new variants
	private String enzymeFamName = "";
	
	public SSTAR () {
		
		super("SSTAR v1.0");
		
		Container window = getContentPane();
		window.setLayout(new BorderLayout());
		window.setBackground(Color.white);
		
		JPanel options = new JPanel(new GridLayout(6,2,3,3));
		JPanel output = new JPanel(new GridLayout(2,2,3,3));
        
		options.setBackground(Color.cyan);
		JPanel menuConstrain = new JPanel(new BorderLayout());
        menuConstrain.setBackground(Color.white);

        menuConstrain.add(options,BorderLayout.NORTH);
        menuConstrain.add(output, BorderLayout.CENTER);
        
        window.add(menuConstrain);
     
		information = new JLabel (" Please select your sequence data, AR gene database and start identifying AR genes ");
		options.add(information);
		
		empty = new JLabel (" ");
		options.add(empty);
		
		assemblyUploadButton = new JButton("Please upload your assembly in FASTA format here!");
		options.add(assemblyUploadButton);
		assemblyUploadButton.addActionListener(this);
		
		assemblySelection = new JTextField(25);
		options.add(assemblySelection);
		
		enzymeUploadButton = new JButton("Please upload your enzymes in FASTA format here!");
		options.add(enzymeUploadButton);
		enzymeUploadButton.addActionListener(this);
		
		enzymeSelection = new JTextField(25);
		options.add(enzymeSelection);
		
		geneDetectButton = new JButton("Identify resistance genes!");
		options.add(geneDetectButton);
		geneDetectButton.addActionListener(this);
		
		exportButton = new JButton("Export potentially new enzyme(s)!");
		options.add(exportButton);
		exportButton.addActionListener(this);
		
		progessOutput = new JTextArea("Log history.... \n",8,30);
		JScrollPane scrollPane1 = new JScrollPane(progessOutput);
		output.add(scrollPane1);
		
		enzymeOutput = new JTextArea("Antimicrobial gene listing.... Gene name - Contig - Alignment length - Gene length \n",15,30);
		JScrollPane scrollPane2 = new JScrollPane(enzymeOutput);
		output.add(scrollPane2);
		
		proteinSeqOutput = new JTextArea("", 10,30);
		JScrollPane scrollPane3 = new JScrollPane(proteinSeqOutput);
		output.add(scrollPane3);
		
		pack();
		setSize(1100, 900);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		SSTAR frame = new SSTAR();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@SuppressWarnings("static-access")
	@Override
	public void actionPerformed(ActionEvent event) {
		int reply;
		File selectedFile;
		String assembly_path = null;
		String enzyme_path = null;
		String newPath = null;
		String showAssemblyPath;
		String dbBuild;
		String blastn;
		String newLine = "\n";
		String tab = "\t";
		ArrayList<String> blastOutput = new ArrayList<String>();
		
		if (event.getSource() == geneDetectButton) {
			try {
					
					assembly_path = assemblySelection.getText();
					enzyme_path = enzymeSelection.getText();
					showAssemblyPath = String.format("Your genome assembly is located at %s", assembly_path);
					if (assembly_path != null && assembly_path.length() != 0) {
						progessOutput.append(showAssemblyPath + newLine);
					}
					//remove file from assembly path
					ArrayList<String> assembly_pathNoFile = new ArrayList<String>(Arrays.asList(assembly_path.split("/")));
					assembly_pathNoFile.remove(assembly_pathNoFile.size()-1);
					StringBuffer restorePath = new StringBuffer();
					for (int i = 0; i < assembly_pathNoFile.size(); i++) {
						restorePath.append(assembly_pathNoFile.get(i));
						restorePath.append("/");
					}
					newPath = restorePath.toString();
					
					//turn assembly into BLAST database
					if (assembly_path != null && assembly_path.length() != 0 && enzyme_path != null && enzyme_path.length() != 0) {
						if (newPath != null && newPath.length() != 0) {
							progessOutput.append("Current working directory: " + newPath + newLine);
							progessOutput.append("Building BLAST database......" + newLine);
							dbBuild = String.format("/usr/local/ncbi/blast/bin/makeblastdb -in %s -out %sblastDB -dbtype nucl", assembly_path, newPath);    
							Process p = Runtime.getRuntime().exec (dbBuild);
							p.waitFor ();
							//run a BLASTN against your custom BLAST database
							progessOutput.append("Performing a BLASTN run......" + newLine);
							String[] cmd = new String[13];
							cmd[0] = "/usr/local/ncbi/blast/bin/blastn";
							cmd[1] = "-query";
							cmd[2] = enzyme_path;
							cmd[3] = "-db";
							cmd[4] = newPath+"blastDB";
							cmd[5] = "-outfmt";
							cmd[6] = "6 qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore qlen";
							cmd[7] = "-evalue";
							cmd[8] = "1e-5";
							cmd[9] = "-out";
							cmd[10] = newPath+"BLASTN";
							cmd[11] = "-max_target_seqs";
							cmd[12] = "1";
							p = Runtime.getRuntime().exec (cmd);
							p.waitFor ();
							progessOutput.append("Your BLASTN run has finished!!!" + newLine);
							//call method for parsing BLAST output 
							blastOutput = parseBlast(newPath);
							for (int i = 0; i < blastOutput.size(); i = i+4) {
								enzymeOutput.append(blastOutput.get(i) + tab);
								enzymeOutput.append(blastOutput.get(i+1) + tab);
								enzymeOutput.append(blastOutput.get(i+2) + tab);
								enzymeOutput.append(blastOutput.get(i+3) + newLine);
							}
						}
					} else {
						JOptionPane.showMessageDialog(null,"No input files!!" + newLine);
					}
				}
			catch (Exception e) {
				progessOutput.append("Error: " + e.getMessage());
			}
		}
		if (event.getSource() == exportButton) {
			String assemblyPath2 = "";
			String newPath2 = "";
			try {
				assemblyPath2 = assemblySelection.getText();
				
				//remove file from assembly path
				ArrayList<String> assembly_pathNoFile2 = new ArrayList<String>(Arrays.asList(assemblyPath2.split("/")));
				assembly_pathNoFile2.remove(assembly_pathNoFile2.size()-1);
				StringBuffer restorePath2 = new StringBuffer();
				for (int i = 0; i < assembly_pathNoFile2.size(); i++) {
					restorePath2.append(assembly_pathNoFile2.get(i));
					restorePath2.append("/");
				}
				newPath2 = restorePath2.toString();
			}
			catch (Exception e) {
				progessOutput.append("Error: " + e.getMessage());
			}
			
			try {
				if (!newPath2.equals("")) {
					outFasta = new PrintWriter (
							new FileWriter (newPath2+"STARzymes.fasta", true));
					outFasta.print(proteinSeqOutput.getText());
					outFasta.close();
					JOptionPane.showMessageDialog(null, "Protein sequence exported to file");
				}
				else {
					JOptionPane.showMessageDialog(null, "No protein sequence(s) to store!");
				}
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "No more protein sequence(s) to store!");
				e.printStackTrace();
			}
		}
		if (event.getSource() == enzymeUploadButton) {
			fileChooserEnzyme = new JFileChooser();
			reply = fileChooserEnzyme.showOpenDialog(this);
			if (reply == fileChooserEnzyme.APPROVE_OPTION) {
				selectedFile = fileChooserEnzyme.getSelectedFile();
				enzymeSelection.setText(selectedFile.getAbsolutePath());
			}
		}
		if (event.getSource() == assemblyUploadButton) {
			fileChooserAssembly = new JFileChooser();
			reply = fileChooserAssembly.showOpenDialog(this);
			if (reply == fileChooserAssembly.APPROVE_OPTION) {
				selectedFile = fileChooserAssembly.getSelectedFile();
				assemblySelection.setText(selectedFile.getAbsolutePath());
			
			}
		
		}
	}
	
	private ArrayList<String> parseBlast(String path) throws IOException {
		BufferedReader br = null;
		String line, blastnfile = null;
		int check1 = -1;
		int check2 = -1;
		String coordinateContig;
		ArrayList<String> enzymes = new ArrayList<String>();
		final int row = 501;
		final int col = 501;
		String[] [] enzymeScore = new String [row] [col];
		String inputBlastn = path + "BLASTN";
		File blastFile = new File(inputBlastn);
		
		try {
			br = new BufferedReader(new FileReader(blastFile));
		} catch (FileNotFoundException e) {
			System.out.println ("file not found! please run your BLAST again!");
			e.printStackTrace();
		}
		
		int rowCount = 0, colCount = 0;
		boolean newFamily = false;
		int coordinateLengthStart = 0, coordinateLengthStop = 0;
		
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t");
			String enzyme = parts[0];
			//split enzyme name
			String[] enzymeParts = enzyme.split("__");
			String variant = enzymeParts[2];
			String variantFam = enzymeParts[1];
			String clusterNr = enzymeParts[0];
			String contig = parts[1];
			String similarity = parts[2];
			String alnLen = parts[3];
			String start = parts[8];
			String stop = parts[9];
			String qlen = parts[12];
			int qlenInt = Integer.parseInt(qlen);
			int alnLenInt = Integer.parseInt(alnLen);
			int startInteger = Integer.parseInt(start);
			int stopInteger = Integer.parseInt(stop);
			int cluster = Integer.parseInt(clusterNr);
			coordinateContig = String.format("%s\t%s\t%s\t%s\t%s", contig, start, stop, alnLen, qlen);
			int thresHold = (qlenInt/5)*2;
			//fill the conversion hashmap
			clusterNrlinkVariantFam.put(clusterNr, variantFam);
			if (alnLenInt > thresHold) {
				//fill the newEnzymeCoordinate HashMap 
				if (startInteger < stopInteger) {
					if (!(cluster == check1)) {
						newEnzymeCoordinate.put(clusterNr, coordinateContig);
						check1 = cluster;
						coordinateLengthStart = startInteger;
						coordinateLengthStop = stopInteger;
					}
					else if (cluster == check1) {
						//Store the longest BLAST hit region
						if (startInteger <= coordinateLengthStart & stopInteger >= coordinateLengthStop) {
							newEnzymeCoordinate.put(clusterNr, coordinateContig);
							coordinateLengthStart = startInteger;
							coordinateLengthStop = stopInteger;
						}
						check1 = cluster;	
					}
				}
				else if (startInteger > stopInteger) {
					if (!(cluster == check1)) {
						newEnzymeCoordinate.put(clusterNr, coordinateContig);
						check1 = cluster;
						coordinateLengthStart = startInteger;
						coordinateLengthStop = stopInteger;
					}
					else if (cluster == check1) {
						//Store the longest BLAST hit region
						if (startInteger >= coordinateLengthStart & stopInteger <= coordinateLengthStop) {
							newEnzymeCoordinate.put(clusterNr, coordinateContig);
							coordinateLengthStart = startInteger;
							coordinateLengthStop = stopInteger;
						}
						check1 = cluster;	
					}
				}
				
				//fill 2D array
				if (check2 == cluster) {
					enzymeScore[rowCount][colCount] = similarity;
					colCount++;
				} 
				else if (!(check2 == cluster) && newFamily == true) {
					rowCount++;
					colCount = 0;
					enzymeScore[rowCount][colCount] = clusterNr;
					colCount++;
					enzymeScore[rowCount][colCount] = similarity;
					colCount++;
					check2 = cluster;
				}
				else {
					enzymeScore[rowCount][colCount] = clusterNr;
					colCount++;
					enzymeScore[rowCount][colCount] = similarity;
					check2 = cluster;
					colCount++;
					newFamily = true;
				}
				
				if (similarity.equals("100.00")) {
					blastnfile = String.format("%s", variant);
					enzymes.add(blastnfile);
					enzymes.add(contig);
					enzymes.add(alnLen);
					enzymes.add(qlen);
				}
			}
			
		}
		br.close();
		
		//print and search the 2D array
		printMatrix(enzymeScore);
		return (enzymes);
	}
	
	private void printMatrix(String[][] grid) throws IOException {
		ArrayList<Double> enzymeDouble = new ArrayList<Double>();
		ArrayList<Double> enzymeDoubleNew = new ArrayList<Double>();
		double value;
		String newEnzymeOut;
		String newLine = "\n";
		for(int i=0;i<500;i++){
		    for(int j=0;j<500;j++) {
		    	if (grid[i][j] != null && j >= 1) {
		    		value = Double.parseDouble(grid[i][j]);
			    	enzymeDouble.add(value);
		    	}
		    	else if (grid[i][j] != null && j == 0){
		    		enzymeFamName = grid[i][j];
		    	}
		    	
		        
		    }
		    //check the arrayList for new variants
		    int lengthDouble = enzymeDouble.size();
		    for (int k = 0; k < enzymeDouble.size(); k++) {
		    	if (enzymeDouble.get(k) > 94.00 && enzymeDouble.get(k) < 100.00) {
		    		enzymeDoubleNew.add(enzymeDouble.get(k));
		    	}
		    }
		    int lengthDoubleNew = enzymeDoubleNew.size();
		    
		    //compare two ArrayLists to determine if we found new variants
		    if (lengthDouble == lengthDoubleNew) {
		    	if (lengthDouble != 0 && lengthDoubleNew != 0) {
		    		String conversion = clusterNrlinkVariantFam.get(enzymeFamName);
		    		newEnzymeOut = String.format("A potentially new %s discovered", conversion);
		    		
		    		//determine if PNV is full length or truncated
		    		String alignInfo = newEnzymeCoordinate.get(enzymeFamName);
		    		String[] alignInfoParts = alignInfo.split("\t");
		    		int alnignL = Integer.parseInt(alignInfoParts[3]);
		    		int queryL = Integer.parseInt(alignInfoParts[4]);
			    	enzymeOutput.append(newEnzymeOut + "\t" + alignInfoParts[0] + "\t" + alignInfoParts[3] + "\t" + alignInfoParts[4] + "\t" + newLine);
			    	
			    	//set threshold for generating protein sequences
		    		int seqGenerateThreshold = (queryL/5)*4;
			    	//call the FASTA parser
		    		if (alnignL > seqGenerateThreshold) {
		    			parseFasta(newEnzymeCoordinate.get(enzymeFamName));
		    		}
			    	
		    	}
		    }
		    enzymeDouble.clear();
		    enzymeDoubleNew.clear();
		}
	}
	
	private void parseFasta (String variantCoordinate) throws IOException {
		String fastaString = assemblySelection.getText();
		File fastaFile = new File(fastaString);
		
		//split coordinate string
		String[] coordinatesList = variantCoordinate.split("\t");
		String contig = ">"+coordinatesList[0];
		String start = coordinatesList[1];
		String stop = coordinatesList[2];
		int startInt = Integer.parseInt(start);
		int stopInt = Integer.parseInt(stop);
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fastaFile));
		} catch (FileNotFoundException e) {
			System.out.println ("No FASTA file found! ... Please upload your assembly!");
			e.printStackTrace();
		}
		
		String line;
		String seq = "";
		boolean selectSeq = false;
		String arGene;
		String revArGene;
		HashMap <String, String> contigNewVariant = new HashMap <String, String>();
		ArrayList <String> proteinSeq = new ArrayList<String>();
		
		while((line=in.readLine())!=null) {
			line = line.replaceAll("(\\r|\\n)", "");
			
		    if (line.equals(contig)) {
		    	proteinSeqOutput.append(line + "_" + clusterNrlinkVariantFam.get(enzymeFamName) + "\n");
		    	selectSeq = true;
		    }
		    else if (line.startsWith(">")) {
		    	selectSeq = false;
		    }
		    else if (selectSeq) {
		    	seq += line;
		    	contigNewVariant.put(contig, seq); 
		    }
		}
		in.close();
		for (Map.Entry<String, String> entry : contigNewVariant.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    //get the AR gene substring out of the contig
		    if (startInt > stopInt) {
		    	arGene = value.substring(stopInt - 1, startInt);
		    	revArGene = new StringBuffer(arGene).reverse().toString();
		    	String revArGeneUp = revArGene.toUpperCase();
		    	String reverseCompDna = transliterateDna(revArGeneUp);
		    	proteinSeq = translateNucleotide(reverseCompDna);
		    	//retrieve longest ORF
		    	int stopCodons = 10000;
		    	HashMap <String, String> longestOrf = new HashMap<String, String>();
		    	for (int i = 0; i < proteinSeq.size(); i++) {
		    		String protein = proteinSeq.get(i);
		    		int count = protein.length() - protein.replace("-", "").length();
		    		if (count < stopCodons) {
		    			System.out.println("\n");
		    			System.out.println(proteinSeq.get(i));
		    			longestOrf.put("ORF", proteinSeq.get(i));
		    			if (proteinSeq.get(i).startsWith("M")) {
		    				break;
		    			}
		    			stopCodons = count;
		    		}
		    	}
		    	//check if protein contains internal stop codons
		    	if (!longestOrf.get("ORF").endsWith("-")) {
		    		if 	(longestOrf.get("ORF").contains("-")) {
		    			proteinSeqOutput.append("!! INTERNAL STOP CODON LOCATED !! \n");
		    			proteinSeqOutput.append(longestOrf.get("ORF") + "\n");
		    		}
		    		else {
		    			proteinSeqOutput.append(longestOrf.get("ORF") + "\n");
		    		}
		    	}
		    	else if (longestOrf.get("ORF").endsWith("-")) {
		    		proteinSeqOutput.append(longestOrf.get("ORF") + "\n");
		    	}
		    }
		    else {
		    	arGene = value.substring(startInt - 1, stopInt);
		    	proteinSeq = translateNucleotide(arGene);
		    	//retrieve longest ORF
		    	int stopCodons = 10000;
		    	HashMap <String, String> longestOrf = new HashMap<String, String>();
		    	for (int i = 0; i < proteinSeq.size(); i++) {
		    		String protein = proteinSeq.get(i);
		    		int count = protein.length() - protein.replace("-", "").length();
		    		if (count < stopCodons) {
		    			longestOrf.put("ORF", proteinSeq.get(i));
		    			if (proteinSeq.get(i).startsWith("M")) {
		    				break;
		    			}
		    			stopCodons = count;
		    		}
		    	}
		    	//check if protein contains internal stop codons
		    	if (!longestOrf.get("ORF").endsWith("-")) {
		    		if 	(longestOrf.get("ORF").contains("-")) {
		    			proteinSeqOutput.append("!! INTERNAL STOP CODON LOCATED !! \n");
		    			proteinSeqOutput.append(longestOrf.get("ORF") + "\n");
		    		}
		    		else {
		    			proteinSeqOutput.append(longestOrf.get("ORF") + "\n");
		    		}
		    	}
		    	else if (longestOrf.get("ORF").endsWith("-")) {
		    		proteinSeqOutput.append(longestOrf.get("ORF") + "\n");
		    	}
		    }
		}
		
	}

	private String transliterateDna(String revArGene) {
		char[] minusStrand = {'A','T','C','G'};
		String[] plusStrand = {"T","A","G","C"};
		StringBuilder revStrand = new StringBuilder();
		outer:
		for (int i = 0; i < revArGene.length(); i++) {
			for(int x = 0; x < minusStrand.length; x++) {
				if (revArGene.charAt(i) == minusStrand[x]) {
					revStrand.append(plusStrand[x]);
					continue outer;
				}
			}
			revStrand.append(revArGene.charAt(i));
		}
		return revStrand.toString();
	}

	private ArrayList<String> translateNucleotide(String dnaString) {
		String newLine = "\n";
		//Variable used for capturing each reading frame on one strand
		String subDnaString;
		//Store three proteins in this array, one per reading frame
		ArrayList <String> proteinsThreeFrames = new ArrayList <String>();
		//Variable for controlling String index out of range errors
		int control;
		final String[][] codonAmino= {
				{"att", "i"}, {"atc", "i"}, {"ata", "i"}, {"ctt", "l"},
				{"ctc", "l"}, {"cta", "l"}, {"ctg", "l"}, {"tta", "l"},
				{"ttg", "l"}, {"gtt", "v"}, {"gtc", "v"}, {"gta", "v"},
				{"gtg", "v"}, {"ttt", "f"}, {"ttc", "f"}, {"atg", "M"},
				{"tgt", "c"}, {"tgc", "c"}, {"gct", "a"}, {"gcc", "a"},
				{"gca", "a"}, {"gcg", "a"}, {"ggt", "g"}, {"ggc", "g"},
				{"gga", "g"}, {"ggg", "g"}, {"cct", "p"}, {"ccc", "p"},
				{"cca", "p"}, {"ccg", "p"}, {"act", "t"}, {"acc", "t"},
				{"aca", "t"}, {"acg", "t"}, {"tct", "s"}, {"tcc", "s"},
				{"tca", "s"}, {"tcg", "s"}, {"agt", "s"}, {"agc", "s"},
				{"tat", "y"}, {"tac", "y"}, {"tgg", "w"}, {"caa", "q"},
				{"cag", "q"}, {"aat", "n"}, {"aac", "n"}, {"cat", "h"},
				{"cac", "h"}, {"gaa", "e"}, {"gag", "e"}, {"gat", "d"},
				{"gac", "d"}, {"aaa", "k"}, {"aag", "k"}, {"cgt", "r"},
				{"cgc", "r"}, {"cga", "r"}, {"cgg", "r"}, {"aga", "r"},
				{"agg", "r"}, {"taa", "-"}, {"tag", "-"}, {"tga", "-"}};
		progessOutput.append("Start translation" + newLine);
		for (int n = 3; n <= 5; n++) {
			StringBuilder proteinStrand = new StringBuilder();
			for (int i = 0; i <= dnaString.length(); i += 3) {
				control = i + 5;
				if (control <= dnaString.length()) {
					for (int x = 0; x <= 63; x++) {
						for (int y = 0; y <= 1; y++) {
							if (n == 3) {
								subDnaString = dnaString.substring(i, n+i);
								if (subDnaString.equalsIgnoreCase(codonAmino[x][y])) {
									//attach the second column, which is the aminoacid, to the stringbuilder
									proteinStrand.append(codonAmino[x][1]);
								}
							}
							else if (n == 4) {
								subDnaString = dnaString.substring(i+1, n+i);
								
								if (subDnaString.equalsIgnoreCase(codonAmino[x][y])) {
									//attach the second column, which is the aminoacid, to the stringbuilder
									proteinStrand.append(codonAmino[x][1]);
								}
							}
							else if (n == 5) {
								subDnaString = dnaString.substring(i+2, n+i);
								if (subDnaString.equalsIgnoreCase(codonAmino[x][y])) {
									//attach the second column, which is the aminoacid, to the stringbuilder
									proteinStrand.append(codonAmino[x][1]);
								}
							}
							
						}
					}	
				}
			}
			proteinsThreeFrames.add(proteinStrand.toString());
		}
		return proteinsThreeFrames;
	}
}

