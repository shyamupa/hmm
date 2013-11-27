package cs446;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexicon {
	
	Map<String,Integer>map;
	Map<String,List<String>>possible_tags;
	public Lexicon(String doc) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(doc));
		String line;
		int c=1;
		map = new HashMap<String, Integer>();
		possible_tags= new HashMap<String,List<String>>();
		while((line=br.readLine())!=null)
		{
			String[] parts=line.split(" ");
			
			map.put(parts[0], c++);
			possible_tags.put(parts[0],Arrays.asList(Arrays.copyOfRange(parts,1,parts.length)));
		}
//		for(Map.Entry<String, Integer> e: map.entrySet())
//			System.out.println(e.getKey()+"::"+e.getValue());
//		for(Map.Entry<String, List<String>> e: possible_tags.entrySet())
//		{
//			System.out.print(e.getKey()+"::");
//			for(String s: e.getValue())
//			{
//				System.out.print(s+" ");
//			}
//			System.out.println();
//		}
	}
	public int getVocabSize(){
		return map.size();
	}
	public int getId(String string) {
		return map.get(string);
	}
	public List<String> getPossibleTags(String string){
		return possible_tags.get(string);
	}
	public static void main(String args[]) throws IOException
	{
		Lexicon lex = new Lexicon("data/HW6.lexicon.txt");
		int vocabSize = lex.getVocabSize();
	}
}
