package home;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Lexiconer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 943625937105112898L;
	Map<String, Integer> str2id;
	Map<Integer, String> id2str;
	int id;

	public Lexiconer() {
		str2id = new HashMap<>();
		id2str = new HashMap<>();
		id = 0;
	}
	
	public int vocabSize()
	{
		return str2id.size();
	}
	public Integer add(String token) {
		if (str2id.containsKey(token))
			return str2id.get(token);
		else {
			str2id.put(token, id);
			id2str.put(id, token);
			return id++;
		}
	}
	public void update(String[] tokens)
	{
		for(String tok:tokens)
		{
			this.add(tok);
		}
	}
	public int[] mapTokensToID(String[] tokens)
	{
		int[] ans= new int[tokens.length];
		int i=0;
		for(String tok:tokens)
		{
			ans[i++]=getId(tok);
		}
		return ans;
	}
	public Integer getId(String token)
	{
		if(str2id.containsKey(token))
			return str2id.get(token);
		else
		{	
			System.err.println("Not here!");
			return -1;
		}
	}
	public String getString(int id)
	{
		if(id2str.containsKey(id))
			return id2str.get(id);
		else
		{	
			System.err.println("Not here!");
			return null;
		}
	}
}