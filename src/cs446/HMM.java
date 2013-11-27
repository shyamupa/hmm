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

	public HMM(int numStates, int vocabSize, List<Double>init, Map<Integer,Map<Integer,Double>>tr,
			Map<Integer,Map<Integer,Double>>em) {
		this.numStates = numStates;	// for eg. # of possible POS tags
		this.vocabSize = vocabSize;	// possible words in the vocabulary
		this.init = init;
		this.tr = tr;
		this.em = em;
	}
	public List<String> Viterbi(List<String> obvs)
	{
		
//		HashMap<String, HashMap<String,Double>> dp = new HashMap<String, HashMap<String,Double>>();
		Double[][] dp= new Double[numStates][vocabSize];
		// stores max probability of being in state s and seeing observation obv
		
		HashMap<String,HashMap<String,String>> backPointers= new HashMap<String,HashMap<String,String>>();
		// currentState -> currentObsv -> previousState
		
		for(int i=0;i<numStates;i++)
		{
			dp[i][0]=; // initialize
			backPointers.put(state,new HashMap<String,String>());
			
			dp.get(state).put(obvs.get(0), init_state.get(state)*getEm(state,obvs.get(0)));
			backPointers.get(state).put(obvs.get(0),null);
		}
		
		Double val,maxval;
		String argmax=null;
		for(int i=1;i<obvs.size();i++)
		{
			maxval=Double.NEGATIVE_INFINITY;
			argmax=null;
			for(String state_s: states)
			{
				for(String state_sprime: states)
				{
					val=dp.get(state_sprime).get(obvs.get(i-1))*getTr(state_sprime,state_s)*getEm(state_s, obvs.get(i));
					if(maxval < val)
					{
						maxval=val;
						argmax=state_sprime;
					}
				}
				backPointers.get(state_s).put(obvs.get(i),argmax);
				dp.get(state_s).put(obvs.get(i),maxval);	//
			}
		}
//		maxval=Double.NEGATIVE_INFINITY;
//		for(String state: states)
//		{
//			val=dp.get(state).get(obvs.get(obvs.size()));
//			if(maxval < val)
//		}
		assert (argmax!=null) :  "bad end state";
		List<String> mostProbableStates = new ArrayList<String>();
		String current_state=argmax;
		for(int i=obvs.size()-1;i>=0;i--)
		{
			mostProbableStates.add(current_state);
			current_state=backPointers.get(current_state).get(obvs.get(i));
		}
		return mostProbableStates;
		
	}
}
