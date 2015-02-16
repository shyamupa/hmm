package home;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs446.LogUtils;

public class HMM {

	Lexiconer lex;
	Lexiconer label_lex;
	float[][] trans;
	float[][] emission;
	float[] pi;
	Map<Integer, List<Integer>> legalTags;
	int vocabSize;
	int numStates;

	public HMM() {
		lex = new Lexiconer();
		label_lex = new Lexiconer();
	}

	public void preprocess() throws IOException {
		populateLexicons();
		numStates = label_lex.vocabSize();
		vocabSize = lex.vocabSize();
		System.out.println(label_lex.vocabSize());
		System.out.println(lex.vocabSize());
		populateLegalTags();
	}

	private void populateLegalTags() throws IOException {
		legalTags = new HashMap<Integer, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(
				"data/HW6.lexicon.txt"));
		String line;
		String[] parts;
		while ((line = br.readLine()) != null) {
			parts = line.split("\\s+");
			int id = lex.getId(parts[0]);
			if (!legalTags.containsKey(id)) {
				legalTags.put(id, new ArrayList<Integer>());
			}
			for (int i = 1; i < parts.length; i++)
				legalTags.get(id).add(label_lex.getId(parts[i]));
		}
		// for(Integer i:legalTags.get(lex.getId("acting")))
		// {
		// System.out.println(label_lex.getString(i));
		// }
		br.close();
	}

	private void populateLexicons() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				"data/HW6.gold.txt"));
		String[] parts;
		String pos, word;
		String line;
		while ((line = br.readLine()) != null) {
			parts = line.split("\\s+");
			for (int j = 0; j < parts.length; j++) {
				word = parts[j].split("_")[0];
				pos = parts[j].split("_")[1];
				// System.out.println(word+" "+pos);
				lex.add(word);
				label_lex.add(pos);
			}
		}
		br.close();
	}

	public void prepare() {
		trans = new float[numStates][numStates];
		emission = new float[numStates][vocabSize];
		pi = new float[numStates];

	}

	private void initEstimates() {
		// emission estimates
		float epsilon = 0.001f; // some primitive smoothing
		float Z;
		for (int i = 0; i < numStates; i++) {
			Z = legalTags.get(i).size();
			for (int j = 0; j < vocabSize; j++) {
				if (legalTags.get(i).contains(j)) {
					emission[i][j] = 1.0f - epsilon / Z;
				} else {
					emission[i][j] = epsilon / (numStates - Z);
				}
			}
		}
		// trans estimates
		for (int i = 0; i < numStates; i++) {
			for (int j = 0; j < numStates; j++) {
				trans[i][j] = 1.0f / numStates;
			}
		}
		for (int i = 0; i < numStates; i++) {
			pi[i] = 1.0f / numStates;
		}
		float emSum = 0.0f, trSum = 0.0f, piSum = 0.0f;
		int row = 10;
		for (int j = 0; j < numStates; j++) {
			trSum += trans[row][j];
			piSum += pi[j];

		}
		// for(int j=0;j<vocabSize;j++)
		// {
		// emSum+=emission[row][j];
		// }
		// System.out.println(emSum);
		System.out.println(trSum);
		// System.out.println(piSum);
	}

	public float[][] computeGamma(int[] tokens, float[][] fwd, float[][] bwd) {
		float[][] gamma = new float[tokens.length][numStates]; // reversed
																// ORDER!!
		float Z;
		for (int t = 0; t < tokens.length; t++) {
			Z = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < numStates; i++) {
				gamma[t][i] = fwd[i][t] + bwd[i][t];
				Z = LogUtils.logAdd(Z, gamma[t][i]);
			}
			for (int i = 0; i < numStates; i++) {
				gamma[t][i] -= Z;
			}
		}
		return gamma;
	}

	public float[][] computeBackward(int[] tokens) {
		float[][] bwd = new float[numStates][tokens.length];
		for (int i = 0; i < numStates; i++) {
			bwd[i][tokens.length - 1] = (float) Math.log(1);
		}
		for (int t = tokens.length - 2; t >= 0; t--) {
			for (int s = 0; s < numStates; s++) {
				float val = (float) LogUtils.logAdd(
						bwd[0][t + 1] + Math.log(trans[s][0])
								+ Math.log(emission[0][tokens[t + 1]]),
						bwd[1][t + 1] + Math.log(trans[s][1])
								+ Math.log(emission[1][tokens[t + 1]]));
				for (int j = 2; j < numStates; j++) {
					val = (float) LogUtils.logAdd(
							val,
							bwd[j][t + 1] + Math.log(trans[s][j])
									+ Math.log(emission[j][tokens[t + 1]]));
				}
				bwd[s][t] = val;
			}
		}
		// System.out.println("Printing Backward");
		// for(int i=0;i<numStates;i++)
		// {
		// for(int j=0;j<observs.size();j++)
		// System.out.print(bwd[i][j]+" ");
		// System.out.println();
		// }
		// System.out.println("backward pass finished!");
		return bwd;
	}

	/**
	 * computes log fwd[state][position]
	 * 
	 * @param tokens
	 * @return
	 */
	public float[][] computeForward(int[] tokens) {
		float[][] fwd = new float[numStates][tokens.length];
		// base case
		// fwd[i][0] = pi[i]*em[i][O_0]
		for (int i = 0; i < numStates; i++) {
			fwd[i][0] = (float) (Math.log(pi[i]) + Math
					.log(emission[i][tokens[0]]));

		}
		for (int t = 1; t < tokens.length; t++) {
			for (int s = 0; s < numStates; s++) {
				float val = (float) LogUtils.logAdd(
						fwd[0][t - 1] + Math.log(trans[0][s]), fwd[1][t - 1]
								+ Math.log(trans[1][s]));
				for (int j = 2; j < numStates; j++) {
					val = (float) LogUtils.logAdd(val,
							fwd[j][t - 1] + Math.log(trans[j][s]));
				}
				val += emission[s][tokens[t]];
				fwd[s][t] = val;
			}
		}
		return fwd;
	}

	public void estimate(int MAX_ITERS, int PRINT_ITERS) {
		int iters = 0;
		while (iters <= MAX_ITERS) {
			if (iters % PRINT_ITERS == 0) {
				System.out.println();
			}
		}

	}

	public float[][][] computeEpsilon(int[] observs, float[][] fwd,
			float[][] bwd) {
		float[][][] epsilon = new float[observs.length - 1][numStates][numStates]; // reversed
																					// ORDER!!
		float Z;
		for (int t = 0; t < observs.length - 1; t++) {
			Z = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numStates; j++) {
					// System.out.println(obvsId[t+1]);
					epsilon[t][i][j] = (float) (fwd[i][t] + bwd[j][t + 1]
							+ Math.log(trans[i][j]) + Math
							.log(emission[j][observs[t + 1]]));
					Z = LogUtils.logAdd(Z, epsilon[t][i][j]);
				}
			}
			for (int i = 0; i < numStates; i++) {
				for (int j = 0; j < numStates; j++) {
					epsilon[t][i][j] -= Z;
				}
			}
		}
		// System.out.println("Printing Epsilon");
		// for(int t=0;t<observs.size()-1;t++)
		// {
		// for(int i=0;i<numStates;i++)
		// {
		// for(int j=0;j<numStates;j++)
		// {
		// System.out.print(epsilon[t][i][j]+" ");
		// }
		// }
		// System.out.println();
		// }
		// System.out.println("Epsilon computed!");
		return epsilon;

	}

	public static void main(String[] args) throws IOException {
		HMM hmm = new HMM();
		hmm.preprocess();
		hmm.prepare();
		hmm.initEstimates();
		// System.exit(-1);
		String train = "data/HW6.train.txt";
		BufferedReader br = new BufferedReader(new FileReader(train));
		String line;
		int i = 0;
		while ((line = br.readLine()) != null) {
			int[] observs = hmm.lex.mapTokensToID(line.split("\\s"));
			float[][] fwd = hmm.computeForward(observs);
			checkSanity(fwd);
			float llh = hmm.computeLLH(observs, fwd);
			float[][] bwd = hmm.computeBackward(observs);
			checkSanity(bwd);
			float[][] gamma = hmm.computeGamma(observs, fwd, bwd);
			checkSanity(gamma);
			float[][][] epsilon = hmm.computeEpsilon(observs, fwd, bwd);
			checkSanity(epsilon);
			i++;
			if (i % 1000 == 0) {
				System.out.println(i);
			}
		}
		br.close();
	}

	/***
	 * computes LLH using log P(O)= log \sum_i fwd[i][T]
	 * 
	 * @param observs
	 * @param fwd
	 * @return
	 */
	private float computeLLH(int[] observs, float[][] fwd) {
		float llh = 0.0f;
		int T = observs.length - 1;
		// System.out.println(fwd.length);
		// System.out.println(fwd[0].length);
		llh = LogUtils.logAdd(fwd[0][T], fwd[1][T]);
		for (int i = 2; i < fwd.length; i++) {
			llh = LogUtils.logAdd(llh, fwd[i][T]);
		}
		if (Float.isInfinite(llh))
			System.out.println("Oh no!");
		return llh;
	}

	public static void checkSanity(float[][][] f) {
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				for (int k = 0; k < f[0][0].length; k++)
					if (Float.isNaN(f[i][j][k])) {
						System.out.println("No!");
					}

			}
		}
	}

	public static void checkSanity(float[][] f) {
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < f[0].length; j++) {
				// System.out.println(f[i][j]);
				if (Float.isNaN(f[i][j])) {
					System.out.println("No!");
				}

			}
		}
	}

	public List<Integer> Viterbi(int[] observs) {

		Double[][] dp = new Double[numStates][observs.length];
		// stores max probability of being in state s and seeing observation
		// with id obv
		int[][] bp = new int[numStates][observs.length];
		// backpointers

		for (int i = 0; i < numStates; i++) {
			dp[i][0] = -Math.log(pi[i]) - Math.log(emission[i][observs[0]]); // init
																				// in
																				// i
																				// and
																				// emitting
																				// the
																				// first
																				// observation
			// System.out.println(dp[i][0]);
		}

		Double val, min;
		int argmin = -1;

		for (int t = 1; t < observs.length; t++) {
			for (int i = 0; i < numStates; i++) {
				min = dp[0][t - 1] - Math.log(trans[0][i])
						- Math.log(emission[i][observs[t]]);
				argmin = 0;
				for (int j = 1; j < numStates; j++) {
					// System.out.println(dp[j][t-1]);
					val = dp[j][t - 1] - Math.log(trans[j][i])
							- Math.log(emission[i][observs[t]]);
					if (min > val) {
						// System.out.println("UPDATING");
						min = val;
						argmin = j;
					}
					// System.out.println("STATE"+j+val+"and"+min);
					// if(min == Double.POSITIVE_INFINITY)
					// {
					// System.out.println("OH NO!!");
					// System.exit(-1);
					// }
				}
				// System.out.print("min should not be infinity! "+min+"argmin is "+argmin);
				dp[i][t] = min;
				bp[i][t] = argmin;
			}
			// System.out.println();
		}
		// print
		ArrayList<Integer> ans = new ArrayList<Integer>();
		int position = -1;
		for (int t = 0; t < observs.length; t++) {
			min = Double.POSITIVE_INFINITY;
			for (int i = 0; i < numStates; i++) {
				// System.out.print(dp[i][t]+" ");
				if (min > dp[i][t]) {
					min = dp[i][t];
					position = i;
				}
			}
			// System.out.println(min+" at "+position);
			ans.add(position);
		}
		// assert (argmin!=-1) : "bad end state";
		//
		// int[] answerIds= new int[observs.size()];
		// answerIds[observs.size()-1]=argmin;
		// int prev;
		// for(int i=observs.size()-2;i>=0;i--)
		// {
		// prev=answerIds[i+1];
		// answerIds[i]=bp[prev][i+1];
		// }
		// for(int i=0;i<answerIds.length;i++)
		// System.out.println(answerIds[i]);
		// return null;
		// System.out.println("Finished!");
		return ans;

	}
}
