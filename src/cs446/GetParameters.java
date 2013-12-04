package cs446;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

public class GetParameters {
	private String trainfile;
	private Map<String,Map<String,Double>>EMtable;
	private Map<String, Double> POStable;
	private Map<String, Map<String,Double>> TRtable;
	private Map<String, Integer> index;
	public Lexicon lex;
	public GetParameters(String doc, Lexicon lex){
		this.trainfile=doc;
		this.EMtable=new HashMap<String,Map<String,Double>>();	// emisson prob
		this.POStable=new HashMap<String,Double>();	// init prob
		this.index=new HashMap<String,Integer>();	// init prob
		this.TRtable=new HashMap<String,Map<String,Double>>();	
		this.lex=lex;
	}
	
	public Double[][] getTr()
	{
		Double[][] tr= new Double[POStable.size()][POStable.size()];
		Double Z=0.0;
		for(String s1:TRtable.keySet())
		{
			Z=0.0;
			for(Double val: TRtable.get(s1).values())
				Z+=val;
			for(String s2:TRtable.get(s1).keySet())
			{
				tr[index.get(s1)][index.get(s2)]=TRtable.get(s1).get(s2)/Z;
			}
		}
//		for(int i=0;i<POStable.size();i++)
//		{
//			for(int j=0;j<POStable.size();j++)
//				System.out.print(tr[i][j]);
//			System.out.println();
//		}
		return tr;
				
	}
	public Double[][] getEm()
	{
		Double[][] em= new Double[POStable.size()][lex.getVocabSize()];
		Double Z=0.0;
//		for(String s1:EMtable.keySet())	// POS
//		{
//			for(String s2:EMtable.get(s1).keySet())	// word(observation)
//			{
//				System.out.println("S1"+s1+" S2 "+s2+EMtable.get(s1).get(s2));
//			}
//		}
		for(String s1:EMtable.keySet())	// POS
		{
			Z=0.0;
			for(Double val: EMtable.get(s1).values())
				Z+=val;
			for(String s2:EMtable.get(s1).keySet())	// word(observation)
			{
				//System.out.println(lex.getId(s2)+";;"+s1+index.get(s1));
				em[index.get(s1)][lex.getObservId(s2)]=EMtable.get(s1).get(s2)/(Z);
//				System.out.println();
			}
		}
		for(int i=0;i<POStable.size();i++)
		{
			Double sum=0.0;
			for(int j=0;j<lex.getVocabSize();j++)
			{
				if(em[i][j]==null)
					em[i][j]=0.0;
//					System.out.println(Math.log(0.0));
//				System.out.print(em[i][j]);
				sum+=em[i][j];
			}
			assert sum==1.0;
//			System.out.println();
		}
		return em;
	}
	public Double[] getRandomInit(){
		Double[] init=new Double[POStable.size()];
		for(int i=0;i<POStable.size();i++)
			init[i]=1.0/POStable.size();
		return init;
	}
	public Double[][] getRandomTr(){
		Double[][] tr = new Double[POStable.size()][POStable.size()];
		for(int i=0;i<POStable.size();i++)
			for(int j=0;j<POStable.size();j++)
				tr[i][j]=1.0/POStable.size();
		return tr;
	}
	public Double[] getInit(){
		Double[] init=new Double[POStable.size()];
		Double sum=0.0;
		for(Double d:POStable.values())
		{
			sum+=d;
		}
		for(String s:POStable.keySet())
		{	
//			System.out.println(s+":"+POStable.get(s)+":"+sum+"=="+POStable.get(s)/sum);
			init[index.get(s)]=POStable.get(s)/sum;
		}
		
		
		
		return init;
	}
	public void getGoldData() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(trainfile));
		String line;
		int c=1;
		String[] parts;
		String pos,word;
		while((line=br.readLine())!=null)
		{
			parts=line.split(" ");
			for (int j = 0; j < parts.length; j++) 
			{
//				System.out.print(parts[j]+" ");
				
				word=parts[j].split("_")[0];
				pos=parts[j].split("_")[1];
				safePutMap(POStable, pos);

				if(!EMtable.containsKey(pos))
					EMtable.put(pos, new HashMap<String,Double>());
				else
				{	
					if(EMtable.get(pos).containsKey(word))
						EMtable.get(pos).put(word,EMtable.get(pos).get(word)+1.0);
					else
						EMtable.get(pos).put(word,1.0);
						
				}
			}
//			System.out.println();
		}
		for(String s: POStable.keySet())
		{
			TRtable.put(s, new HashMap<String,Double>());
			for(String t:POStable.keySet())
				TRtable.get(s).put(t, 0.0);
		}
		int p=0;
		for(String s:POStable.keySet())
		{	
//			System.out.println("POS="+p+" at "+s);
			index.put(s, p++);
		}
		System.out.println(index.size());
		String text = new Scanner( new File(trainfile) ).useDelimiter("\\A").next();
		text = text.replace("\n", "").replace("\r", "");
		String[]tokens= text.split(" ");
		String pos1,pos2;
		for(int i=1;i<tokens.length;i++)
		{
			pos1=tokens[i-1].split("_")[1];
			pos2=tokens[i].split("_")[1];
//			System.out.println(pos1+":"+pos2);
			TRtable.get(pos1).put(pos2, TRtable.get(pos1).get(pos2)+1.0);
		}

//		for(String s: POStable.keySet())
//		{
//			for(String t:POStable.keySet())
//				System.out.println(TRtable.get(s).get(t));
//				
//		}
//		for(String tagged_word:EMtable.keySet())
//			System.out.println(tagged_word);
//		for(String p:POStable.keySet())
//			System.out.println(p+"::"+POStable.get(p));
	}

	public void safePutMap(Map<String,Double>table, String s)
	{
		if(table.containsKey(s)){
			table.put(s, table.get(s)+1.0);
		}
		else
		{
			table.put(s, 1.0);
		}
	}
	
	public int getNumTags()
	{
		return index.size();
	}
	public String getTagFromId(int id)
	{
//		for(Entry<String, Integer> e: index.entrySet())
//			System.out.println(e.getKey()+"="+e.getValue());
		for(String pos:index.keySet())
		{
//			System.out.println(pos);
			if(index.get(pos)==id)
		
				return pos;
			else
				continue;
		}
		return null;
	}
	public static void main(String[] args) throws IOException {
		Lexicon lex=new Lexicon("data/HW6.lexicon.txt");
		GetParameters gp=new GetParameters("data/HW6.gold.txt",lex);
		gp.getGoldData();
		// for supervised training ONLY USE TO CHECK CORRECTNESS OF VITERBI
//		Double[] pi = gp.getInit();
//		Double[][] tr = gp.getTr();
//		Double[][] em = gp.getEm();
//		HMM2 hmm=new HMM2(tr,em,pi,gp.getNumTags(),lex);
		// the real deal. Unsupervised Training
		Double[] pi = gp.getRandomInit();
		Double[][] tr = gp.getRandomTr();
		Double[][] em = lex.getEmFromLex();
		HMM2 hmm=new HMM2(tr,em,pi,gp.getNumTags(),lex);
		
		String line;
		Double[][] fwd,bwd,gamma;
		Double[][][] epsilon;
		
		int iteration=0;
		while(iteration!=10)	// num of iterations
		{
			BufferedReader br = new BufferedReader(new FileReader("data/HW6.train.txt"));
			int counter=1;
			hmm.resetUpdates();
//			hmm.printInit();
			while((line=br.readLine())!=null)
			{
//				System.out.println("Processing sentence: "+counter++);
				List<String> observs = Arrays.asList(line.split(" "));
				fwd = hmm.computeForward(observs);
				bwd = hmm.computeBackward(observs);
				gamma = hmm.computeGamma(observs, fwd, bwd);
				epsilon = hmm.computeEpsilon(observs, fwd, bwd);
				hmm.updateParameters(observs,gamma,epsilon);
	//			hmm.printInitUpdate();
	//			hmm.printInit();
	//			hmm.printTr();
	//			break;
			}
//			hmm.printTrUpdate();
//			hmm.printInitUpdate();
//			hmm.printEmUpdate();
			hmm.updateInit(counter);
			hmm.updateTr(counter);
			hmm.updateEM(counter);
			iteration++;
			System.out.println("Finished iteration "+iteration);
			br.close();
		}
		
		hmm.printInit();
		hmm.printTr();
		hmm.printEm();
//
//		BufferedReader br = new BufferedReader(new FileReader("data/HW6.train.txt"));
//		while((line=br.readLine())!=null)
//		{
////			System.out.println(line);
//		
////		for(int i=0;i<line.split(" ").length;i++)
////			System.out.println(line.split(" ")[i]);
////		System.out.println("-----");
//			List<Integer> ans = hmm.Viterbi(Arrays.asList(line.split(" ")));
//			for(int a: ans)
//			{
//	//			System.out.println(a);
//				System.out.print(gp.getTagFromId(a)+" ");
//			}
//			System.out.println();
//		}
	}
}
