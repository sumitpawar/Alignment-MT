package org.jhu.mt.model1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class IBMModel1 {
	// Constants for file paths 
	// English File Path
	private static final String enFilePath = "/home/sumit/MT/en600.468/aligner/data/hansards.e";
	// French File Path
	private static final String frFilePath = "/home/sumit/MT/en600.468/aligner/data/hansards.f";
	// Number of lines to read
	private static final int num_sent = 100000;
	// (Limit) Number of Iterations
	private static final int itr = 5;
	
	// count(e|f)
	private HashMap<String, Double>  countEF = new HashMap<String, Double>();
	// total(f) 
	private HashMap<String, Double>  totalF = new HashMap<String, Double>();
	// s-total(e) 
	private HashMap<String, Double>  stotalE = new HashMap<String, Double>();
	// t(e|f) 
	private HashMap<String, Double>  tEF = new HashMap<String, Double>();

	/**
	 * @return the countEF
	 */
	public HashMap<String, Double> getCountEF() {
		return countEF;
	}

	/**
	 * @param countEF the countEF to set
	 */
	public void setCountEF(HashMap<String, Double> countEF) {
		this.countEF = countEF;
	}

	/**
	 * @return the totalF
	 */
	public HashMap<String, Double> getTotalF() {
		return totalF;
	}

	/**
	 * @param totalF the totalF to set
	 */
	public void setTotalF(HashMap<String, Double> totalF) {
		this.totalF = totalF;
	}

	/**
	 * @return the stotalE
	 */
	public HashMap<String, Double> getStotalE() {
		return stotalE;
	}

	/**
	 * @param stotalE the stotalE to set
	 */
	public void setStotalE(HashMap<String, Double> stotalE) {
		this.stotalE = stotalE;
	}

	/**
	 * @return the tEF
	 */
	public HashMap<String, Double> gettEF() {
		return tEF;
	}

	/**
	 * @param tEF the tEF to set
	 */
	public void settEF(HashMap<String, Double> tEF) {
		this.tEF = tEF;
	}

	/**
	 * Method: main()
	 * @param args
	 */
	public static void main(String[] args) {
		File en = new File(enFilePath);
		File fr = new File(frFilePath);
		IBMModel1 m1 = new IBMModel1();
		try {
			BufferedReader enbr = null;
			BufferedReader frbr = null;
			//double vocsize = 0;
			
			for(int i = 0; i< itr; i++) {
				enbr = new BufferedReader(new FileReader(en));
				frbr = new BufferedReader(new FileReader(fr));
				String enline = null, frline = null;
				int cnt_lines = 0;
				m1.setStotalE(new HashMap<String, Double>());
				while( cnt_lines <= num_sent 
						&& (enline = enbr.readLine()) != null 
						&& (frline = frbr.readLine()) != null ) {
					cnt_lines ++;
					String[] enArr = enline.trim().split(" ");
					String[] frArr = frline.trim().split(" ");
					
					// Compute Normalization
					for(String enw : enArr) {
						for(String frw : frArr) {
							//vocsize ++;
							/*if((enw.equals(".") || enw.equals(",") 
									|| enw.equals(":") || enw.equals(";") 
									|| enw.equals("?") || enw.equals("!") 
									|| enw.equals("(") || enw.equals(")") ) && !enw.equals(frw)) {
								
							}else */
							if(null != m1.gettEF() 
									&& !m1.gettEF().isEmpty()) {
								String key = enw + "|" + frw;
								if(!m1.gettEF().containsKey(key)) {
									throw new Exception("Invalid condition :::: t(e|f) Key not found: " + key);
								}else {
									m1.getStotalE().put(enw, 
										( ((m1.getStotalE().containsKey(enw))?m1.getStotalE().get(enw):0) 
											+ m1.gettEF().get(key)));
								}
							}else {
								if(null != m1.getStotalE() 
										&& ! m1.getStotalE().isEmpty()) {
									m1.getStotalE().put(enw, (
										((m1.getStotalE().containsKey(enw))?m1.getStotalE().get(enw):0)  
											//+ (1.0 / (double) frArr.length) ));
											+ 0.01 ));
								}else {
									m1.getStotalE().put(enw, 0.01);//(1.0 /(double) frArr.length));
								}
							}
						}
					}

				}
					
				// Uniformly Normalize probabilities if first iteration
				/*if(null == m1.gettEF() 
						|| m1.gettEF().isEmpty()) {
					double vocabSize = m1.getStotalE().size();
					for(Entry<String, Double> entry : m1.getStotalE().entrySet()) {
						String key = entry.getKey();
						//Double val = entry.getValue()/vocsize; //vocabSize;
						Double val = entry.getValue()/vocabSize;
						m1.getStotalE().put(key, val);
					}
				}*/
				
				// Collect Counts
				m1.setCountEF(new HashMap<String, Double>());	// reset counts for count(e|f)
				m1.setTotalF(new HashMap<String, Double>());	// reset counts for total(f)
				// For the first iteration t(e|f) is not set 
				// because we did not know the size of the vocabulary
				if(null != m1.gettEF() && !m1.gettEF().isEmpty()) {
					for(Entry<String, Double> entry : m1.gettEF().entrySet()) {
						String key = entry.getKey();
						Double val = entry.getValue();
						String[] ef = key.split("\\|");
						String e = ef[0], f = ef[1];
						
						double value = ((m1.gettEF().get(key)) / (m1.getStotalE().get(e)));
						// count(e|f) += t(e|f) / s-total(e)
						m1.getCountEF().put(key, 
							((m1.getCountEF().containsKey(key))?m1.getCountEF().get(key):0)
									+ value);
						// total(f) += t(e|f) / s-total(e)
						m1.getTotalF().put(f, 
								((m1.getTotalF().containsKey(f))?m1.getTotalF().get(f):0) 
									+ value);
						
					}
				} else { // set t(e|f) = 1/|vocab|
					//double vocabSize = m1.getStotalE().size();
					enbr = new BufferedReader(new FileReader(en));
					frbr = new BufferedReader(new FileReader(fr));
					enline = null;
					frline = null;
					cnt_lines = 0;

					while( cnt_lines <= num_sent 
							&& (enline = enbr.readLine()) != null 
							&& (frline = frbr.readLine()) != null ) {
						cnt_lines ++;
					
						for(String enw : enline.split(" ")) {
							for(String frw : frline.split(" ")) {
								//double value = ( ((double) 1.0 / vocabSize) / (m1.getStotalE().get(enw)) );
								String key = enw + "|" + frw;
								// count(e|f) += t(e|f) / s-total(e)
								m1.getCountEF().put(key, 
									((m1.getCountEF().containsKey(key))?m1.getCountEF().get(key):0)
											+ 0.01);//+ value);
								// total(f) += t(e|f) / s-total(e)
								m1.getTotalF().put(frw, 
										((m1.getTotalF().containsKey(frw))?m1.getTotalF().get(frw):0) 
											+ 0.01 ); //+ value);
								
							}
						}
					}
				}
					
				
				// Update t(e|f)
				for(Entry<String, Double> entry : m1.getCountEF().entrySet()) {
					String key = entry.getKey();
					Double val = entry.getValue();
					String f = key.split("\\|")[1];
					m1.gettEF().put(key, (m1.getCountEF().get(key) / m1.getTotalF().get(f)) );
				}
				/*enbr = new BufferedReader(new FileReader(en));
				frbr = new BufferedReader(new FileReader(fr));
				enline = null;
				frline = null;
				cnt_lines = 0;
				m1.setStotalE(new HashMap<String, Double>());
				while( cnt_lines <= num_sent 
						&& (enline = enbr.readLine()) != null 
						&& (frline = frbr.readLine()) != null ) {
					cnt_lines ++;

					for(String frw : frline.trim().split(" ")) {
						for(String enw : enline.trim().split(" ")) {
							String key = enw + "|" + frw;
							m1.gettEF().put(key, (m1.getCountEF().get(key) / m1.getTotalF().get(frw)) );
						}
					}
					
				}*/
					
			}
			
			// estimate probabilities
			enbr = new BufferedReader(new FileReader(en));
			frbr = new BufferedReader(new FileReader(fr));
			String enline = null, frline = null;
			int cnt_lines = 0;
			m1.setStotalE(new HashMap<String, Double>());
			while( cnt_lines <= 1000//num_sent 
					&& (enline = enbr.readLine()) != null 
					&& (frline = frbr.readLine()) != null ) {
				cnt_lines ++;

				int j = 0;
				for(String frw : frline.trim().split(" ")) {
					int i = 0;
					int maxi = i;
					double max = -1.0;
					List<Integer> maxList = new ArrayList<Integer>();
					for(String enw : enline.trim().split(" ")) {
						String key = enw + "|" + frw;
						double val = m1.gettEF().get(key);
						if(val > max) {
							maxi = i;
							max = val;
							maxList.clear();
						} else if(val == max) {
							maxList.add(i);
						}
						i++;
					}
					System.out.print(j + "-" + maxi + " ");
					for(Integer mi : maxList) {
						System.out.print(j + "?" + mi + " ");
					}
					maxList.clear();
					j++;
				}
				System.out.println();
				
			}
			
			enbr.close();
			frbr.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
