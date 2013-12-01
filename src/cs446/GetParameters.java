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
	private Map<String,Double>EMtable;
	private Map<String, Double> POStable;
	private Map<String, Map<String,Double>> TRtable;
	private HashMap<String, Integer> index;
	public GetParameters(String doc){
		this.trainfile=doc;
		this.EMtable=new HashMap<String,Double>();	// emisson prob
		this.POStable=new HashMap<String,Double>();	// init prob
		this.index=new HashMap<String,Integer>();	// init prob
		this.TRtable=new HashMap<String,Map<String,Double>>();	
	}
	public Double[][] getTr()
	{
		Double[][] tr= new Double[POStable.size()][POStable.size()];
		for(String s1:TRtable.keySet())
			for(String s2:TRtable.get(s1).keySet())
				tr[index.get(s1)][index.get(s2)]=TRtable.get(s1).get(s2);
		return tr;
				
	}
	public Double[][] getEm()
	{
		return null;
	}
	public Double[] getInit(){
		Double[] init=new Double[POStable.size()];
		Double sum=0.0;
		for(Double d:POStable.values())
		{
			sum+=d;
		}
		int i=0;
		for(String s:POStable.keySet())
		{	
//			System.out.println(s+":"+POStable.get(s)+":"+sum+"=="+POStable.get(s)/sum);
			init[i++]=POStable.get(s)/sum;
		}
		i=0;
		for(String s:POStable.keySet())
		{	
			System.out.println(s+"=="+init[i++]);
			index.put(s, i);
		}
		
		return init;
	}
	public void LearnFromLabeledData() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(trainfile));
		String line;
		int c=1;
		String[] parts;
		String pos;
		while((line=br.readLine())!=null)
		{
			parts=line.split(" ");
			for (int j = 0; j < parts.length; j++) 
			{
//				System.out.print(parts[j]+" ");
				safePutMap(EMtable, parts[j]);
				
				pos=parts[j].split("_")[1];
				safePutMap(POStable, pos);
				
			}
//			System.out.println();
		}
		for(String s: POStable.keySet())
		{
			TRtable.put(s, new HashMap<String,Double>());
			for(String t:POStable.keySet())
				TRtable.get(s).put(t, 0.0);
		}
		
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
	
	public static void main(String[] args) throws IOException {
		GetParameters gp=new GetParameters("data/HW6.gold.txt");
		gp.LearnFromLabeledData();
		gp.getInit();
	}
}
