package cs446;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	public GetParameters(String doc){
		this.trainfile=doc;
		this.EMtable=new HashMap<String,Double>();	// emisson prob
		this.POStable=new HashMap<String,Double>();	// init prob
		this.TRtable=new HashMap<String,Map<String,Double>>();	
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
//				System.out.println(parts[j]);
				safePutMap(EMtable, parts[j]);
				
				pos=parts[j].split("_")[1];
				safePutMap(POStable, pos);
				
			}
		}
		for(String tagged_word:EMtable.keySet())
			System.out.println(tagged_word);
		for(String p:POStable.keySet())
			System.out.println(p+"::"+POStable.get(p));
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
	}
}
