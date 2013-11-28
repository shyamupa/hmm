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
	private Map<String,Double>tags;
	public GetParameters(String doc){
		this.trainfile=doc;
		this.tags=new HashMap<String,Double>();
	}
	public void LearnFromLabeledData() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(trainfile));
		String line;
		int c=1;
		while((line=br.readLine())!=null)
		{
			String[] parts=line.split(" ");
			for (int j = 0; j < parts.length; j++) {
//				System.out.println(parts[j]);
				if(tags.get(parts[j].split("_")[1])!=null)
					tags.add(parts[j].split("_")[1],tags.get(parts[j].split("_")[1])+1.0);
				else
					tags
			}
		}
		for(String tag:tags)
			System.out.println(tag);
		
	}
	public static void main(String[] args) throws IOException {
		GetParameters gp=new GetParameters("data/HW6.gold.txt");
		gp.LearnFromLabeledData();
	}
}
