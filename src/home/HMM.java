package home;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
	Map<Integer,List<Integer>> legalTags;
	int vocabSize;
	int numStates;
	
	public HMM() {
		lex=new Lexiconer();
		label_lex=new Lexiconer();
	}

	public void preprocess() throws IOException {
		populateLexicons();
		numStates=label_lex.vocabSize();
		vocabSize=lex.vocabSize();
		System.out.println(label_lex.vocabSize());
		System.out.println(lex.vocabSize());
		populateLegalTags();
	}
			
	private void populateLegalTags() throws IOException {
		legalTags= new HashMap<Integer,List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader("data/HW6.lexicon.txt"));
		String line;
		String[] parts;
		while((line=br.readLine())!=null)
		{
			parts=line.split("\\s+");
			int id=lex.getId(parts[0]);
			if(!legalTags.containsKey(id))
			{
				legalTags.put(id, new ArrayList<Integer>());
			}
			for(int i=1;i<parts.length;i++)
				legalTags.get(id).add(label_lex.getId(parts[i]));
		}
//		for(Integer i:legalTags.get(lex.getId("acting")))
//		{
//			System.out.println(label_lex.getString(i));
//		}
		br.close();
	}

	private void populateLexicons() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("data/HW6.gold.txt"));
		String[] parts;
		String pos,word;
		String line;
		while((line=br.readLine())!=null)
		{
			parts=line.split("\\s+");
			for (int j = 0; j < parts.length; j++) 
			{
				word=parts[j].split("_")[0];
				pos=parts[j].split("_")[1];
//				System.out.println(word+" "+pos);
				lex.add(word);
				label_lex.add(pos);
			}
		}
		br.close();
	}

	public void prepare()
	{
		trans=new float[numStates][numStates];
		emission=new float[numStates][vocabSize];
		pi = new float[numStates];
		
	}
	
	private void initEstimates() {
		// emission estimates
		float Z;
		for(int i=0;i<numStates;i++)
		{
			Z=legalTags.get(i).size();
			for(int j=0;j<vocabSize;j++)
			{
				if(legalTags.get(i).contains(j))
				{
					emission[i][j]=1.0f/Z;
				}
			}
		}
		// trans estimates
		for(int i=0;i<numStates;i++)
		{
			for(int j=0;j<numStates;j++)
			{
				trans[i][j]=1.0f/numStates;
			}
		}
		for(int i=0;i<numStates;i++)
		{
				pi[i]=1.0f/numStates;
		}
//		float emSum=0.0f,trSum=0.0f,piSum=0.0f;
//		int row=10;
//		for(int j=0;j<numStates;j++)
//		{
//			trSum+=trans[row][j];
//			piSum+=pi[j];
//			
//		}
//		for(int j=0;j<vocabSize;j++)
//		{
//			emSum+=emission[row][j];
//		}
//		System.out.println(emSum);
//		System.out.println(trSum);
//		System.out.println(piSum);
	}

	public float[][] computeGamma(int[] tokens, float[][] fwd, float[][] bwd){
		float[][] gamma=new float[tokens.length][numStates];	// reversed ORDER!!
		float Z;
		for(int t=0;t<tokens.length;t++)
		{
			Z=Float.NEGATIVE_INFINITY;
			for(int i=0;i<numStates;i++)
			{
				gamma[t][i]=fwd[i][t]+bwd[i][t];
				Z=LogUtils.logAdd(Z, gamma[t][i]);
			}
			for(int i=0;i<numStates;i++)
			{
				gamma[t][i]-=Z;
			}
		}
		return gamma;
	}
	public float[][] computeBackward(int[] tokens){
		float[][] bwd=new float[numStates][tokens.length];
		for(int i=0;i<numStates;i++)
		{
			bwd[i][tokens.length-1]=(float) Math.log(1);
		}
		for(int t=tokens.length-2;t>=0;t--)
		{
			for(int s=0;s<numStates;s++)
			{
				float val=Float.NEGATIVE_INFINITY;
				for(int j=0;j<numStates;j++)
				{
					val=(float) LogUtils.logAdd(val,bwd[j][t+1]+Math.log(trans[s][j])+Math.log(emission[j][tokens[t+1]]));
				}
				bwd[s][t]=val;
			}
		}
//		System.out.println("Printing Backward");
//		for(int i=0;i<numStates;i++)
//		{
//			for(int j=0;j<observs.size();j++)
//				System.out.print(bwd[i][j]+" ");
//			System.out.println();
//		}
//		System.out.println("backward pass finished!");
		return bwd;
	}
	public float[][] computeForward(int[] tokens){
		float[][] fwd=new float[numStates][tokens.length];
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]=(float) (Math.log(pi[i])+Math.log(emission[i][tokens[0]]));
			if(Float.isNaN(fwd[i][0]))
				System.out.println("VAL "+Math.log(pi[i])+"VAL 2 "+Math.log(emission[i][tokens[0]]));
		}
		for(int t=1;t<tokens.length;t++)
		{
			for(int s=0;s<numStates;s++)
			{
				float val=Float.NEGATIVE_INFINITY;
				for(int j=0;j<numStates;j++)
				{
					val=(float) LogUtils.logAdd(val,fwd[j][t-1]+Math.log(trans[j][s]));
				}
				val+=emission[s][tokens[t]];
				fwd[s][t]=val;
			}
		}
		return fwd;
	}
	
	public void estimate(int MAX_ITERS, int PRINT_ITERS)
	{
		int iters=0;
		while(iters<=MAX_ITERS)
		{
			if(iters % PRINT_ITERS ==0)
			{
				System.out.println();
			}
		}
		
	}
	public float[][][] computeEpsilon(int[] observs, float[][] fwd, float[][] bwd){
		float[][][] epsilon=new float[observs.length-1][numStates][numStates];	// reversed ORDER!!
		float Z;
		for(int t=0;t<observs.length-1;t++)
		{
			Z=Float.NEGATIVE_INFINITY;
			for(int i=0;i<numStates;i++)
			{
				for(int j=0;j<numStates;j++)
				{
//					System.out.println(obvsId[t+1]);
					epsilon[t][i][j]=(float) (fwd[i][t]+bwd[j][t+1]+Math.log(trans[i][j])+Math.log(emission[j][observs[t+1]]));
					Z=LogUtils.logAdd(Z, epsilon[t][i][j]);
				}
			}
			for(int i=0;i<numStates;i++)
			{
				for(int j=0;j<numStates;j++)
				{
					epsilon[t][i][j]-=Z;
				}
			}
		}
//		System.out.println("Printing Epsilon");
//		for(int t=0;t<observs.size()-1;t++)
//		{
//			for(int i=0;i<numStates;i++)
//			{
//				for(int j=0;j<numStates;j++)
//				{
//					System.out.print(epsilon[t][i][j]+" ");
//				}
//			}
//			System.out.println();
//		}
//		System.out.println("Epsilon computed!");
		return epsilon;
		
	}
	public static void main(String[] args) throws IOException {
		HMM hmm = new HMM();
		hmm.preprocess();
		hmm.prepare();
		hmm.initEstimates();
		int[] observs = null;
		float[][] fwd = hmm.computeForward(observs);
		float[][] bwd = hmm.computeBackward(observs);
		float[][] gamma = hmm.computeGamma(observs, fwd, bwd);
		float[][][] epsilon = hmm.computeEpsilon(observs, fwd, bwd);
	}
}
