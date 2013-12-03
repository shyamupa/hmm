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
		for(String s1:EMtable.keySet())
		{
			Z=0.0;
			for(Double val: EMtable.get(s1).values())
				Z+=val;
			for(String s2:EMtable.get(s1).keySet())
			{
				//System.out.println(lex.getId(s2)+";;"+s1+index.get(s1));
				em[index.get(s1)][lex.getId(s2)]=EMtable.get(s1).get(s2)/(Z);
			}
		}
		for(int i=0;i<POStable.size();i++)
			{
				Double sum=0.0;
				for(int j=0;j<lex.getVocabSize();j++)
				{
					if(em[i][j]==null)
						em[i][j]=0.0;
//						System.out.println(Math.log(0.0));
//					System.out.print(em[i][j]);
					sum+=em[i][j];
				}
				assert sum==1.0;
//				System.out.println();
			}
		return em;
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
	public void LearnFromLabeledData() throws IOException{
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
						EMtable.get(pos).put(word,0.0);
						
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
			System.out.println("POS="+p+s);
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
	public static void main(String[] args) throws IOException {
		GetParameters gp=new GetParameters("data/HW6.gold.txt",new Lexicon("data/HW6.lexicon.txt"));
		gp.LearnFromLabeledData();
		Double[] pi = gp.getInit();
		Double[][] tr = gp.getTr();
		Double[][] em = gp.getEm();
		HMM2 hmm=new HMM2(tr,em,pi,gp.getNumTags(),gp.lex);
		String line;
		BufferedReader br = new BufferedReader(new FileReader("data/HW6.train.txt"));
		while((line=br.readLine())!=null)
		{
			System.out.println(line);
			break;
		}
		for(int i=0;i<line.split(" ").length;i++)
			System.out.println(line.split(" ")[i]);
		System.out.println("-----");
		hmm.Viterbi(Arrays.asList(line.split(" ")));
	}
}
