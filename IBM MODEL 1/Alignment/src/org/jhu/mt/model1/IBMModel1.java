package org.jhu.mt.model1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author sumit
 *
 */
public class IBMModel1 {

	// Constants for file paths 
	// English File Path
	//private static final String enFilePath = "/home/sumit/MT/en600.468/aligner/data/hansards.e";
	private static final String enFilePath = "../data/hansards.e";
	// French File Path
	//private static final String frFilePath = "/home/sumit/MT/en600.468/aligner/data/hansards.f";
	private static final String frFilePath = "../data/hansards.f";
	// Number of lines to read
	private static final int num_sent = 100000;
	// (Limit) Number of Iterations
	private static final int itr = 2;
	
	// Map t(f|e)
	private Map<String, Double> tFE = new HashMap<String, Double>();
	// Map count(e,f)
	private Map<String, Double> countEF = new HashMap<String, Double>();
	// Map t(f) = t(f|e')
	private Map<String, Double> tf = new HashMap<String, Double>();
	// Map count(e) = Count(f'|e)
	private Map<String, Double> ce = new HashMap<String, Double>();
	// P_null (Null Probability)
	private double p_null = 0.0;
	
	/**
	 * @return the tFE
	 */
	public Map<String, Double> gettFE() {
		return tFE;
	}

	/**
	 * @param tFE the tFE to set
	 */
	public void settFE(Map<String, Double> tFE) {
		this.tFE = tFE;
	}

	/**
	 * @return the countEF
	 */
	public Map<String, Double> getCountEF() {
		return countEF;
	}

	/**
	 * @param countEF the countEF to set
	 */
	public void setCountEF(Map<String, Double> countEF) {
		this.countEF = countEF;
	}

	/**
	 * @return the tf
	 */
	public Map<String, Double> getTf() {
		return tf;
	}

	/**
	 * @param tf the tf to set
	 */
	public void setTf(Map<String, Double> tf) {
		this.tf = tf;
	}

	/**
	 * @return the ce
	 */
	public Map<String, Double> getCe() {
		return ce;
	}

	/**
	 * @param ce the ce to set
	 */
	public void setCe(Map<String, Double> ce) {
		this.ce = ce;
	}

	/**
	 * @return the p_null
	 */
	public double getP_null() {
		return p_null;
	}

	/**
	 * @param p_null the p_null to set
	 */
	public void setP_null(double p_null) {
		this.p_null = p_null;
	}

	// Compute Initial Counts
	private void collectInitialCounts() {
		File en = new File(enFilePath);
		File fr = new File(frFilePath);
		boolean print_stats = false;
		try {
			BufferedReader enbr = new BufferedReader(new FileReader(en));
			BufferedReader frbr = new BufferedReader(new FileReader(fr));
			// metric --- not required though
			//Map<String, Double> counte = new HashMap<String, Double>();
			//Map<String, Double> countf = new HashMap<String, Double>();
			long totale = 0, totalf = 0;
			double null_dem = 0.0, null_num = 0.0;
			
			String enline = null, frline = null;
			int cnt_lines = 0;
			while( cnt_lines <= num_sent 
					&& (enline = enbr.readLine()) != null 
					&& (frline = frbr.readLine()) != null ) {
				cnt_lines ++;
				
				String[] enArr = enline.trim().toLowerCase().split("\\s+");
				String[] frArr = frline.trim().toLowerCase().split("\\s+");
				
				int enlen = enArr.length;
				int frlen = frArr.length;
				
				// p_null computations
				if(enlen > frlen) {
					null_num +=  (enlen - frlen);
				}
				null_dem += enlen;
				
				totale += enlen + 1;		// english words count & +1 for NUll word
				totalf += frlen;			// french words count
				// Compute Initial Counts
				for(String frwd : frArr) {
					//countf.put(frwd, 
					//		((countf.containsKey(frwd))?countf.get(frwd):0.0) + 1.0);
					tf.put(frwd, 
							((tf.containsKey(frwd))?tf.get(frwd):0.0) + (1.0 + enlen));
					countEF.put("null_pos=#=" + frwd, 
							((countEF.containsKey("null_pos=#=" + frwd))?
									countEF.get("null_pos=#=" + frwd):0.0) + 1.0);
					ce.put("null_pos", 
							((ce.containsKey("null_pos"))?ce.get("null_pos"):0.0) + 1.0);
					
					for(String enwd : enArr) {
						ce.put(enwd, 
								((ce.containsKey(enwd))?ce.get(enwd):0.0) + 1.0);
						countEF.put(enwd + "=#=" + frwd, 
								((countEF.containsKey(enwd + "=#=" + frwd))?
										countEF.get(enwd + "=#=" + frwd):0.0) + 1.0);
					}
				}
				
			} // while loop-ends
			p_null = null_num / null_dem;
			
			// print initial stats if print_stats = 1;
			if(print_stats) {
				System.out.println(" = = = = = = = = = = = = = = = = = = = = = = = = = = ");
				System.out.println("Initial stats      : ");
				System.out.println("lines processed    : " + cnt_lines);
				System.out.println("total french words : " + totalf);
				System.out.println("total english words: " + totale);
				System.out.println("total vocab count  : " + countEF.size());
				System.out.println("t(f|e') count      : " + tf.size());
				System.out.println("c(e|f') count      : " + ce.size());
				System.out.println("null_num           : " + null_num);
				System.out.println("null_dem           : " + null_dem);
				System.out.println("p_null             : " + p_null);
				System.out.println(" = = = = = = = = = = = = = = = = = = = = = = = = = = ");
			}
			
			// Close file read buffers
			enbr.close();
			frbr.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Re-estimate Posterior Probability (Model);
	// M - Step
	private void reEstimateModel() {
		try {
			// re-compute model probabilities
			if(null != countEF && !countEF.isEmpty()) {
				tFE = new HashMap<String, Double>();
				tf =  new HashMap<String, Double>();
				for(Entry<String, Double> entry : countEF.entrySet()) {
					String key = entry.getKey();
					String[] ef = key.split("=#=");
					String e = ef[0];
					String f = ef[1];
					String newKey = f + "=#=" + e;
					double val = ( entry.getValue() / ce.get(e) );
					tFE.put(newKey, val);
					if(!e.equalsIgnoreCase("null_pos")) {
						tf.put(f, 
							( ((tf.containsKey(f))?tf.get(f):0.0) + val) );
					}
				}
			}else {
				System.err.println("Error: NULL or empty counts - countEF");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Re-estimate expectation: counts C(e,f) and C(e,f')
	// E- Step
	private void reEstimateCounts() {
		try {
			if(null != tFE && !tFE.isEmpty()) {
				countEF = new HashMap<String, Double>();
				ce = new HashMap<String, Double>();
				for(Entry<String, Double> entry : tFE.entrySet()) {
					String key = entry.getKey();
					String[] fe = key.split("=#=");
					String f = fe[0];
					String e = fe[1];
					String newKey = e + "=#=" + f;
					double value = entry.getValue();
					double p = 0.0;
					
					double den = ( (p_null * tFE.get(f + "=#=null_pos")) 
							+ ( (1.0 - p_null) * tf.get(f)) ); 
					if(e.equalsIgnoreCase("null_pos")) {
						p = (p_null * value) / (den);
					}else {
						p = ((1.0 - p_null) * value) / (den);
					}
					countEF.put(newKey, 
							( ((countEF.containsKey(newKey))?countEF.get(newKey):0.0 ) + p) );
					ce.put(e, 
							( ((ce.containsKey(e))?ce.get(e):0.0) + p) );
				}
			}else {
				System.err.println("Error : Please set p_null and/or t(f|e) ... ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void findOptiAlignment(Map<Integer, List<Object[]>> fval,
			Map<Integer, Integer> femap, Map<Integer, Integer> efmap, int j) {
		List<Object[]> jval = fval.get(j);
		if(jval.size() > 0) {
			Object[] val = jval.get(jval.size() - 1);
	
			double highest = Double.parseDouble(val[0].toString());
			int i = Integer.parseInt(val[1].toString());
			
			if(efmap.containsKey(i)) { 
				int j2 = efmap.get(i);
				List<Object[]> jval2 = fval.get(j2);
				if(jval2.size() > 0) {
					Object[] val2 = jval2.get(jval2.size() - 1);
					double highest2 = Double.parseDouble(val2[0].toString());
					
					if(highest >= highest2) {
						femap.put(j, i);
						efmap.put(i, j);
						
						Object[] obj = jval2.remove(jval2.size() - 1);
						jval2.add(0, obj);
						femap.remove(j2);
						findOptiAlignment(fval, femap, efmap, j2);
					}else {
						Object[] obj = jval.remove(jval.size() - 1);
						jval.add(0, obj);
						findOptiAlignment(fval, femap, efmap, j);
					}
				}
			}else {
				femap.put(j, i);
				efmap.put(i, j);
			}
		}else {
			
		}
	}
	
	// Estimate word alignments
	private void estimateAlignment() {
		try {
			BufferedReader enbr = new BufferedReader(
					new FileReader(new File(enFilePath)));
			BufferedReader frbr = new BufferedReader(
					new FileReader(new File(frFilePath)));
			
			String enline = null, frline = null;
			int cnt_lines = 0;
			
			while( cnt_lines <= 1000 //num_sent 
					&& (enline = enbr.readLine()) != null 
					&& (frline = frbr.readLine()) != null ) {
				cnt_lines ++;
				// if(cnt_lines ==1 ) continue; //debugging
				
				int frlen = frline.trim().toLowerCase().split("\\s+").length;
				int enlen = enline.trim().toLowerCase().split("\\s+").length;
				if(enlen < frlen) {
					int len = frlen - enlen;
					while (len > 0){
						enline += " null_pos ";	
						len --;
					}
				}
				String[] frArr = frline.trim().toLowerCase().split("\\s+");
				String[] enArr = enline.trim().toLowerCase().split("\\s+");
				
				Map<Integer, List<Object[]>> fval = new HashMap<Integer, List<Object[]>>();
				Map<Integer, Integer> femap = new HashMap<Integer, Integer>();
				Map<Integer, Integer> efmap = new HashMap<Integer, Integer>();
				int j = 0;
				for(String frw : frArr) {
					int i = -1;
					//int maxi = i;
					//double max = -1.0;
					List<Object[]> templist = new ArrayList<Object[]>();
					for(String enw : enArr) {
						i++;
						String key = frw + "=#=" + enw;
						double val = tFE.get(key);
						templist.add(new Object[] {val , i});
					}
					List<Object[]> d = new ArrayList<Object[]>();
					d.addAll(templist);
					Collections.sort(d, new Comparator<Object[]>() {
					    public int compare(Object[] obj1, Object[] obj2) {
					    	Double d1 = Double.parseDouble(obj1[0].toString());
					    	Double d2 = Double.parseDouble(obj2[0].toString());
					        return d1.compareTo(d2);
					    }
					});
					fval.put(j, d);
					findOptiAlignment(fval, femap, efmap, j);
					j++;
				}
				for (int i = 0;i<femap.size();i++) {
					if(/*null != femap.get(i) &&*/ !enArr[femap.get(i)].equalsIgnoreCase("null_pos")) {
						System.out.print(i + "-" + femap.get(i) + " ");
					}
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
			e.printStackTrace();
		}
	}
	
	// Compute IBM Model1
	private void compute_model1() {
		try {
			// initialize
			collectInitialCounts(); 	// Like E - Step
			// EM - Steps
			for(int i = 0; i< itr; i++) {
				reEstimateModel(); 		// M - Step 
				reEstimateCounts(); 	// E - Step 
			}
			estimateAlignment();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		//IBMModel1 m1 = new IBMModel1();
		try {
			//m1.compute_model1();
			long startTime = System.currentTimeMillis();
			new IBMModel1().compute_model1();
			long endTime = System.currentTimeMillis();
			System.err.println("" + ((double) (endTime - startTime) / (double) (1000*60)) + " mins");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
