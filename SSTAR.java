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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import java.io.*;
import java.util.*;

class JTextFieldLimit extends PlainDocument {
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int limit;
	  JTextFieldLimit(int limit) {
	    super();
	    this.limit = limit;
	  }

	  JTextFieldLimit(int limit, boolean upper) {
	    super();
	    this.limit = limit;
	  }

	  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
	    if (str == null)
	      return;

	    if ((getLength() + str.length()) <= limit) {
	      super.insertString(offset, str, attr);
	    }
	  }
}

public class SSTAR extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel inf1, inf2, inf3, inf4, inf5, inf6, seqIdLabel;
	private JFileChooser fileChooserAssembly, fileChooserEnzyme;
	private JTextField assemblySelection, enzymeSelection, seqId;
	private JTextArea progessOutput, enzymeOutput, proteinSeqOutput;
	private JButton geneDetectButton, exportButton, assemblyUploadButton, enzymeUploadButton;
	private PrintWriter outFasta, outTabular;
	private double cutOff;
	
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
		
		JPanel options = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel output = new JPanel(new GridLayout(3,3,3,3));
        
		options.setBackground(Color.cyan);
		JPanel menuConstrain = new JPanel(new BorderLayout());
        menuConstrain.setBackground(Color.white);

        menuConstrain.add(options,BorderLayout.NORTH);
        menuConstrain.add(output, BorderLayout.CENTER);
        
        window.add(menuConstrain);
     
		inf1 = new JLabel ("                       ");
		gbc.weighty = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		options.add(inf1, gbc);
		
		inf2 = new JLabel ("                       ");
		gbc.gridx = 1;
		gbc.gridy = 0;
		options.add(inf2, gbc);
		
		inf3 = new JLabel ("                       ");
		gbc.gridx = 2;
		gbc.gridy = 0;
		options.add(inf3, gbc);
		
		inf4 = new JLabel ("                       ");
		gbc.gridx = 3;
		gbc.gridy = 0;
		options.add(inf4, gbc);
		
		inf5 = new JLabel ("      ");
		gbc.gridx = 4;
		gbc.gridy = 0;
		options.add(inf5, gbc);
		
		inf6 = new JLabel ("                         ");
		gbc.gridx = 5;
		gbc.gridy = 0;
		options.add(inf6, gbc);
		
		assemblyUploadButton = new JButton("Upload your assembly in FASTA format here!");
		gbc.gridx = 0;
		gbc.gridy = 1;
		options.add(assemblyUploadButton, gbc);
		assemblyUploadButton.addActionListener(this);
		
		assemblySelection = new JTextField(25);
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		options.add(assemblySelection, gbc);
		
		enzymeUploadButton = new JButton("Upload your enzymes in FASTA format here!");
		gbc.gridx = 0;
		gbc.gridy = 2;
		options.add(enzymeUploadButton, gbc);
		enzymeUploadButton.addActionListener(this);
		
		enzymeSelection = new JTextField(25);
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		options.add(enzymeSelection, gbc);
		
		seqIdLabel = new JLabel ("Specify cut-off sequence similarity value for new variants");
		gbc.gridx = 0;
		gbc.gridy = 3;
		options.add(seqIdLabel, gbc);
		
		seqId = new JTextField(5);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		options.add(seqId, gbc);
		seqId.setDocument(new JTextFieldLimit(4));
		
		geneDetectButton = new JButton("Identify resistance genes!");
		gbc.gridx = 0;
		gbc.gridy = 4;
		options.add(geneDetectButton, gbc);
		geneDetectButton.addActionListener(this);
		
		exportButton = new JButton("Export output files!");
		gbc.gridx = 5;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.LINE_END;
		options.add(exportButton, gbc);
		exportButton.addActionListener(this);
		
		//beginning of GridLayout section
		progessOutput = new JTextArea("Log history.... \n",8,30);
		JScrollPane scrollPane1 = new JScrollPane(progessOutput);
		output.add(scrollPane1);
		
		enzymeOutput = new JTextArea("Gene\tContig\tSeq similarity\tAlignment length\tGene length \n",15,30);
		JScrollPane scrollPane2 = new JScrollPane(enzymeOutput);
		output.add(scrollPane2);
		
		proteinSeqOutput = new JTextArea("", 10,30);
		JScrollPane scrollPane3 = new JScrollPane(proteinSeqOutput);
		output.add(scrollPane3);
		
		pack();
		setSize(1200, 1100);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		SSTAR frame = new SSTAR();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@SuppressWarnings({ "static-access", "unused" })
	@Override
	public void actionPerformed(ActionEvent event) {
		int reply;
		File selectedFile;
		String assembly_path = null;
		String enzyme_path = null;
		String newPath = null;
		String showAssemblyPath;
		String dbBuild;
		String removeDB;
		String newLine = "\n";
		String tab = "\t";
		ArrayList<String> blastOutput = new ArrayList<String>();
		
		if (event.getSource() == geneDetectButton) {
					
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
							
						//check if makeblastdb is available on your system as system variable (in .bash_profile or .bashrc)
						try {
							String javaMakedb = System.getenv("MAKEBLASTDB");
							if (javaMakedb.equals("makeblastdb")) {
								dbBuild = String.format("makeblastdb -in %s -out %sblastDB -dbtype nucl", assembly_path, newPath);    
								Process p = Runtime.getRuntime().exec (dbBuild);
								p.waitFor ();
							}
						}
						catch (Exception e) {
							progessOutput.append("ERROR: makeblastdb is not set as system variable" + newLine);
						}	
						//run a BLASTN against your custom BLAST database
						progessOutput.append("Performing a BLASTN run......" + newLine);
						String[] cmd = new String[13];
						//check if BLASTN is available on your system as system variable (in .bash_profile or .bashrc)
						try {
							String javaBlastn = System.getenv("BLASTN");
							if (javaBlastn.equals("blastn")) {
								cmd[0] = javaBlastn;
							}
						}
						catch (Exception e) {
							progessOutput.append("ERROR: blastn is not set as system variable" + newLine);
						}
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
						if (seqId.getText().trim().length() == 0) {
							JOptionPane.showMessageDialog(null,"No sequence similarity value inserted" + newLine);
							//remove the BLAST database files to save disk space
							removeDB = String.format("rm %sblastDB.nhr %sblastDB.nin %sblastDB.nsq", newPath, newPath, newPath);
							Process p3 = null;
							try {
								p3 = Runtime.getRuntime().exec (removeDB);
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								p3.waitFor ();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						else {
							cutOff = Double.parseDouble(seqId.getText());
							Process p2 = null;
							try {
								p2 = Runtime.getRuntime().exec (cmd);
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								p2.waitFor ();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							progessOutput.append("Your BLASTN run has finished!!!" + newLine);	
							progessOutput.append("Going over your raw BLAST output now...." + newLine);
								
							//remove the BLAST database files to save disk space
							removeDB = String.format("rm %sblastDB.nhr %sblastDB.nin %sblastDB.nsq", newPath, newPath, newPath);
							Process p4 = null;
							try {
								p4 = Runtime.getRuntime().exec (removeDB);
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								p4.waitFor ();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
								
							//call method for parsing BLAST output 
							try {
								blastOutput = parseBlast(newPath);
							} catch (IOException e) {
								e.printStackTrace();
							}
							progessOutput.append("Presenting your filtered BLAST data...." + newLine);
								
							for (int i = 0; i < blastOutput.size(); i = i+5) {
								enzymeOutput.append(blastOutput.get(i) + tab);
								enzymeOutput.append(blastOutput.get(i+1) + tab);
								enzymeOutput.append(blastOutput.get(i+2) + "%" + tab);
								enzymeOutput.append(blastOutput.get(i+3) + tab);
								enzymeOutput.append(blastOutput.get(i+4) + newLine);
							}
						}
					}
					else {
						JOptionPane.showMessageDialog(null,"Cannot find your working directory, using the wrong SSTAR version?" + newLine);
					}
				} else {
					JOptionPane.showMessageDialog(null,"No input files!!" + newLine);
				}
		}
			
		if (event.getSource() == exportButton) {
			String assemblyPath2 = "";
			String newPath2 = "";
			String fileName = "";
			try {
				assemblyPath2 = assemblySelection.getText();
				
				//remove file from assembly path
				ArrayList<String> assembly_pathNoFile2 = new ArrayList<String>(Arrays.asList(assemblyPath2.split("/")));
				//store the assembly file name in a variable for later
				fileName = assembly_pathNoFile2.get(assembly_pathNoFile2.size()-1);
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
							new FileWriter (newPath2+"SSTARzymes_" + fileName + ".txt", true));
					outFasta.print(proteinSeqOutput.getText());
					outFasta.close();
					outTabular = new PrintWriter (
							new FileWriter (newPath2+ fileName + ".genes_tab_separated.txt", true));
					outTabular.print(enzymeOutput.getText());
					outTabular.close();
					JOptionPane.showMessageDialog(null, "Protein sequences and gene listing exported to files");
				}
				else {
					JOptionPane.showMessageDialog(null, "Nothing to store!");
				}
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "No more data to store!");
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
			coordinateContig = String.format("%s\t%s\t%s\t%s\t%s\t%s", contig, start, stop, alnLen, qlen, similarity);
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
					enzymes.add(similarity);
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
		    	//using the user defined sequence similarity value for storing potential new variants
		    	if (enzymeDouble.get(k) >= cutOff && enzymeDouble.get(k) < 100.00) {
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
			    	enzymeOutput.append(newEnzymeOut + "\t" + alignInfoParts[0] + "\t" + alignInfoParts[5] + "%" + "\t" + alignInfoParts[3] + "\t" + alignInfoParts[4] + "\t" + newLine);
			    	
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
		boolean selectSeqNCBI = false;
		String arGene;
		String revArGene;
		String[] splitNcbiHeader = null;
		HashMap <String, String> contigNewVariant = new HashMap <String, String>();
		ArrayList <String> proteinSeq = new ArrayList<String>();
		
		while((line=in.readLine())!=null) {
			line = line.replaceAll("(\\r|\\n)", "");
			
			if (line.matches(".*\\s+.*") && line.startsWith(">")) {
				splitNcbiHeader = line.split("\\s+");
				selectSeqNCBI = false;
				if (splitNcbiHeader[0].equals(contig)) {
					proteinSeqOutput.append(splitNcbiHeader[0] + "_" + clusterNrlinkVariantFam.get(enzymeFamName) + "\n");
					selectSeqNCBI = true;
				}
			}
			
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
		    else if (selectSeqNCBI) {
		    	seq += line;
		    	contigNewVariant.put(splitNcbiHeader[0], seq);
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

