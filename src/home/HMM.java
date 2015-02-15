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

	public Double[][] computeForward(int[] obvsId){
		Double[][] fwd=new Double[numStates][obvsId.length];
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]=Math.log(pi[i])+Math.log(emission[i][obvsId[0]]);
			if(Double.isNaN(fwd[i][0]))
				System.out.println("VAL "+Math.log(pi[i])+"VAL 2 "+Math.log(emission[i][obvsId[0]]));
		}
		for(int t=1;t<obvsId.length;t++)
		{
			for(int s=0;s<numStates;s++)
			{
				Double val=Double.NEGATIVE_INFINITY;
				for(int j=0;j<numStates;j++)
				{
					val=LogUtils.logAdd(val,fwd[j][t-1]+Math.log(trans[j][s]));
				}
				val+=emission[s][obvsId[t]];
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
	
	public static void main(String[] args) throws IOException {
		HMM hmm = new HMM();
		hmm.preprocess();
		hmm.prepare();
		hmm.initEstimates();
	}
}
