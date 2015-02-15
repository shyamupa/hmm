package cs446;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HMM {
	public int numStates;
	public int vocabSize;
	public List<Double>init;	
	public Map<Integer,Map<Integer,Double>>tr;
	public Map<Integer,Map<Integer,Double>>em;
	private Lexicon lex;
	
	public HMM(int numStates, int vocabSize, List<Double>init, Map<Integer,Map<Integer,Double>>tr,
			Map<Integer,Map<Integer,Double>>em, Lexicon lex) {
		
		assert init.size()==numStates : "Number of states and length of initial vector mismatch";
		this.numStates = numStates;	// for eg. # of possible POS tags
		this.vocabSize = vocabSize;	// possible words in the vocabulary
		this.init = init;	// init probabilities
		this.tr = tr;
		this.em = em;
		this.lex=lex;
	}
//	public List<String> Viterbi(List<String> observs)
	{
//		Integer[] obvsId=observToIds(observs);
		// stores max probability of being in state s and seeing observation obv
//		HashMap<Integer, HashMap<Integer,Double>> dp = new HashMap<Integer, HashMap<Integer,Double>>();
//		Double[][] dp= new Double[numStates][vocabSize];
		
//		HashMap<Integer,HashMap<Integer,Integer>> backPointers= new HashMap<Integer,HashMap<Integer,Integer>>();
		// currentState -> currentObsv -> previousState
		
//		for(int i=0;i<numStates;i++)
//		{
//			dp.put(i,init.get(i)); // initialize
//		}
		
//		Double val,maxval;
//		String argmax=null;
//		for(int i=1;i<observs.size();i++)
//		{
//			maxval=Double.NEGATIVE_INFINITY;
//			argmax=null;
//			for(String state_s: states)
//			{
//				for(String state_sprime: states)
//				{
//					val=dp.get(state_sprime).get(observs.get(i-1))*getTr(state_sprime,state_s)*getEm(state_s, observs.get(i));
//					if(maxval < val)
//					{
//						maxval=val;
//						argmax=state_sprime;
//					}
//				}
//				backPointers.get(state_s).put(observs.get(i),argmax);
//				dp.get(state_s).put(observs.get(i),maxval);	//
//			}
//		}
////		maxval=Double.NEGATIVE_INFINITY;
////		for(String state: states)
////		{
////			val=dp.get(state).get(obvs.get(obvs.size()));
////			if(maxval < val)
////		}
////		assert (argmax!=null) :  "bad end state";
////		List<String> mostProbableStates = new ArrayList<String>();
////		String current_state=argmax;
////		for(int i=observs.size()-1;i>=0;i--)
////		{
////			mostProbableStates.add(current_state);
////			current_state=backPointers.get(current_state).get(observs.get(i));
////		}
//		return mostProbableStates;
		
	}
	private Integer[] observToIds(List<String> observs) {
		List<Integer>ids=new ArrayList<Integer>();
		for(String s: observs)
			ids.add(lex.getObservId(s));
		return ids.toArray(new Integer[ids.size()]);
	}
}
