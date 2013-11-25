package cs446;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HMM2 {
	
	private Double[][] tr;	// StateId -> StateId -> Prob
	private Double[][] em; // StateId -> ObsrvId -> Prob
	private Double[] init_state;	// initial probabalities
	private int numStates;
	
	public HMM2(){
		
	}
	// calculates the 
	public void ForwardBackwardAlgorithm(List<String> observs){
		computeForward(observs);
		computeBackward(observs);
	}
		
	private void computeBackward() {
		
	}
	private Double[][] computeForward(List<String> observs) {
		int[] obvsId=observToIds(observs);
		Double[][] fwd=new Double[numStates][observs.size()];
		// pr of being in state s and having seen upto i obvs
		Double Z=Double.NaN; // partition function
		
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]=init_state[i]*em[i][obvsId[0]];
			Z+=fwd[i][0];
		}
		assert Z!=0.0;
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]/=Z;
		}
		
		for(int i=1;i<obvsId.length;i++)
		{
			Z=0.0;
			for(int s=0;s<numStates;s++)
			{
				Double val=0.0;
				for(int t=0;t<numStates;t++)
					val+=fwd[t][i-1]*tr[t][s];
				val*=em[s][obvsId[i]];
				fwd[s][i]=val;
				Z+=fwd[s][i];
			}
			assert Z!=0.0;
			for(int s=0;s<numStates;s++)
			{
				fwd[s][i]/=Z;
			}
		}
		return fwd;
	}
	public List<String> Viterbi(List<String> observs)
	{
		int[] obvsId=observToIds(observs);
		Double[][] dp=new Double[numStates][observs.size()];
		// stores max probability of being in state s and seeing observation with id obv
		int[][] bp= new int[numStates][observs.size()];
		// backpointers
		
		for(int i=0;i<numStates;i++)
		{
			dp[i][0]=Math.log(init_state[i])+Math.log(em[i][obvsId[0]]);
		}
		
		Double val,maxval;
		int argmax=-1;
		
		for(int i=1;i<observs.size();i++)
		{
			maxval=Double.NEGATIVE_INFINITY;
			for(int j=0;j<numStates;j++)
			{
				for(int k=0;j<numStates;k++)
				{
					val= dp[k][i-1]+Math.log(tr[j][k]);
					if(maxval < val)
					{
						maxval=val;
						argmax=j;
					}
				}
				dp[j][i]=maxval;
				bp[j][i]=argmax;
			}
			}
		assert (argmax!=-1) :  "bad end state";
		
		int[] answerIds= new int[observs.size()];
		answerIds[observs.size()-1]=argmax;
		int prev;
		for(int i=observs.size()-2;i>=0;i--)
		{
			prev=answerIds[i+1];
			answerIds[i]=bp[prev][i+1];
		}
		return null;
		
	}
	public int[] observToIds(List<String>observs)
	{
		int[] obvsId=new int[observs.size()];	// map obvs to their ids
		for(int i=0;i<observs.size();i++)
			obvsId[i]=getId(observs.get(i));
		return obvsId;
	}
}
