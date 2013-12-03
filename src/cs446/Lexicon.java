package cs446;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Lexicon {
	
	public Map<String,Integer>word_to_id;	// int ids for each observation
	public Map<Integer,String>id_to_word;	// 
	public Map<String,List<String>>possible_tags_for_word;	// list of possible tags for a given string
	public Map<String,Integer>tags_to_id;
	public Map<Integer,String>id_to_tag;
	public Lexicon(String doc) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(doc));
		String line;
		int c=0;
		word_to_id = new HashMap<String, Integer>();	// word (observation)
		id_to_word = new HashMap<Integer,String>();	// word (observation)
		possible_tags_for_word= new HashMap<String,List<String>>();
		tags_to_id= new HashMap<String,Integer>();
		id_to_tag= new HashMap<Integer,String>();
		while((line=br.readLine())!=null)
		{
			String[] parts=line.split(" ");
			
			word_to_id.put(parts[0], c);
			id_to_word.put(c++,parts[0]);
			
			possible_tags_for_word.put(parts[0],Arrays.asList(Arrays.copyOfRange(parts,1,parts.length)));
			for(int i=1;i<parts.length;i++)
			{
				if(!tags_to_id.containsKey(parts[i]))
				{
					tags_to_id.put(parts[i], tags_to_id.size());
					id_to_tag.put(tags_to_id.get(parts[i]),parts[i]);
				}
			}
		}
		for(Entry<String, Integer> e: tags_to_id.entrySet())
			System.out.println(e.getKey()+"="+e.getValue());

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
//	public Double[] getInitFromLex()	// starting point for EM
//	{
//		for(String word:possible_tags.keySet())
//			for()
//			
//	}
//	public Double[][] getTrFromLex()	// starting point for EM
//	{
//		Double[][] tr= new Double[][]
//	}
	public Double[][] getEmFromLex()	// starting point for EM
	{
		Double[][] em= new Double[tags_to_id.size()][getVocabSize()];
		for(String s:possible_tags_for_word.keySet())
		{
			for(String pos: possible_tags_for_word.get(s))
			{
				em[getTagId(pos)][word_to_id.get(s)]=1.0/(possible_tags_for_word.get(s).size());
		
			}
		}
//		for(int i=0;i<tags_index.size();i++)
//		{
//			for(int j=0;j<getVocabSize();j++)
//			{
////				if(em[i][j]==null)
////					em[i][j]=0.0;
//				System.out.print(em[i][j]+" ");
//			}
//			System.out.println();
//		}
		for(int j=0;j<getVocabSize();j++)
		{
			double sum=0.0;
			System.out.print(id_to_word.get(j)+" ");
			for(int i=0;i<tags_to_id.size();i++)
			{
				if(em[i][j]==null)
					em[i][j]=0.0;
				System.out.print(em[i][j]+" ");
				sum+=em[i][j];
			}
			System.out.print("SUM "+sum);
			assert sum==1.0;
			System.out.println();
		}
		return em;
	}
	public int getVocabSize()
	{
		return word_to_id.size();	// all possible words (read observed states)
	}
	public int getObservId(String word) 
	{
		return word_to_id.get(word);	
	}
	public int getTagId(String tag)
	{
		return tags_to_id.get(tag);
	}
	public List<String> getPossibleTags(String string)
	{
		return possible_tags_for_word.get(string);	// get possible tags for a given word(read possible hidden state for a given observed word)
	}
	public static void main(String args[]) throws IOException
	{
		Lexicon lex = new Lexicon("data/HW6.lexicon.txt");
		int vocabSize = lex.getVocabSize();
		System.out.println(vocabSize);
		lex.getEmFromLex();
	}
}
