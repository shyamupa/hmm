package cs446;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HMM2 {
	
	private Double[][] tr;	// StateId -> StateId -> Prob
	private Double[][] em; // StateId -> ObsrvId -> Prob
	private Double[] init_state;	// initial probabalities
	private int numStates;
	private Lexicon lexicon;
	
	
	public HMM2(Double[][] tr, Double[][] em, Double[] init_state,
			int numStates, Lexicon lexicon) {
		this.tr = tr;
		this.em = em;
		this.init_state = init_state;
		this.numStates = numStates;
		this.lexicon = lexicon;
	}

	public void ForwardBackwardAlgorithm(List<String> observs)
	{

	}

	private List<Double[][]> computeForwardAndBackward(List<String> observs) 
	{
		int[] obvsId=observToIds(observs);
		
		Double[][] fwd=new Double[numStates][observs.size()];
		Double[][] bwd=new Double[numStates][observs.size()];
		Double[] Z= new Double[observs.size()];
		Z[0]=0.0;
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]=init_state[i]*em[i][obvsId[0]];
			Z[0]+=fwd[i][0];
		}
		assert Z[0]!=0.0;
		
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]/=Z[0];
		}
		
		for(int t=1;t<obvsId.length;t++)
		{
			Z[t]=0.0;
			for(int s=0;s<numStates;s++)
			{
				Double val=0.0;
				for(int j=0;j<numStates;j++)
				{
					val+=fwd[j][t-1]*tr[j][s];
				}
				val*=em[s][obvsId[t]];
				fwd[s][t]=val;
				Z[t]+=fwd[s][t];
			}
			assert Z[t]!=0.0;
			for(int s=0;s<numStates;s++)
			{
				fwd[s][t]/=Z[t];
			}
		}
		// Z[t] computed for all time t !!
		
		for(int i=0;i<numStates;i++)
		{
			bwd[i][observs.size()-1]=1/Z[observs.size()-1];
		}
		
		for(int t=observs.size()-2;t>=0;t--)
		{
			for(int s=0;s<numStates;s++)
			{
				Double val = 0.0;
				for(int j=0;j<numStates;j++)
				{
					val+=bwd[j][t+1]*tr[j][s]*em[j][obvsId[t+1]];
				}
				bwd[s][t]=val/Z[t];
			}
		}
		List<Double[][]> ls=new ArrayList<Double[][]>();
		ls.add(fwd);
		ls.add(bwd);
		return ls;
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
			dp[i][0]=-Math.log(init_state[i])-Math.log(em[i][obvsId[0]]);	// init in i and emitting the first observation
		}
		
		Double val,max;
		int argmax=-1;
		
		for(int t=1;t<observs.size();t++)
		{
			for(int i=0;i<numStates;i++)
			{
				max=dp[0][t-1]-Math.log(tr[0][i])-Math.log(em[i][obvsId[t]]);	
				argmax=0;		
				for(int j=1;i<numStates;j++)
				{
					val= dp[j][t-1]-Math.log(tr[i][j])-Math.log(em[i][t]);
					if(max < val)
					{
						max=val;
						argmax=j;
					}
				}
				dp[i][t]=max;
				bp[i][t]=argmax;
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
			obvsId[i]=lexicon.getId(observs.get(i));
		return obvsId;
	}
}
