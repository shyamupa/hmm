package cs446;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cs446.LogUtils;

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
	public Double[][][] computeEpsilon(List<String> observs, Double[][] fwd, Double[][] bwd){
		int[] obvsId=observToIds(observs);
		Double[][][] epsilon=new Double[observs.size()-1][numStates][numStates];	// reversed ORDER!!
		Double Z;
		for(int t=0;t<observs.size()-1;t++)
		{
			Z=Double.NEGATIVE_INFINITY;
			for(int i=0;i<numStates;i++)
			{
				for(int j=0;j<numStates;j++)
				{
//					System.out.println(obvsId[t+1]);
					epsilon[t][i][j]=fwd[i][t]+bwd[j][t+1]+Math.log(tr[i][j])+Math.log(em[j][obvsId[t+1]]);
					Z=LogUtils.logAdd(Z, epsilon[t][i][j]);
				}
			}
			for(int i=0;i<numStates;i++)
			{
				for(int j=0;j<numStates;j++)
				{
					epsilon[t][i][j]-=Z;
				}
			}
		}
//		for(int t=0;t<observs.size()-1;t++)
//		{
//			for(int i=0;i<numStates;i++)
//			{
//				for(int j=0;j<numStates;j++)
//				{
//					System.out.println(epsilon[t][i][j]);
////					if(epsilon[t][i][j]==null)
////						System.exit(-1);
//				}
//			}
//		}
		System.out.println("Epsilon computed!");
		return epsilon;
		
	}
	public Double[][] computeGamma(List<String> observs, Double[][] fwd, Double[][] bwd){
		Double[][] gamma=new Double[observs.size()][numStates];	// reversed ORDER!!
		Double Z;
		for(int t=0;t<observs.size();t++)
		{
			Z=Double.NEGATIVE_INFINITY;
			for(int i=0;i<numStates;i++)
			{
				gamma[t][i]=fwd[i][t]+bwd[i][t];
				Z=LogUtils.logAdd(Z, gamma[t][i]);
			}
			for(int i=0;i<numStates;i++)
			{
				gamma[t][i]-=Z;
			}
		}
//		for(int t=0;t<observs.size();t++)
//		{
//			for(int i=0;i<numStates;i++)
//			{
//				System.out.print(gamma[t][i]+" ");
//			}
//			System.out.println();
//		}
		
		System.out.println("Gamma computed!");
		return gamma;
	}
	public Double[][] computeBackward(List<String> observs){
		int[] obvsId=observToIds(observs);
		Double[][] bwd=new Double[numStates][observs.size()];
		for(int i=0;i<numStates;i++)
		{
			bwd[i][observs.size()-1]=Math.log(1);
		}
		for(int t=obvsId.length-2;t>=0;t--)
		{
			for(int s=0;s<numStates;s++)
			{
				Double val=Double.NEGATIVE_INFINITY;
				for(int j=0;j<numStates;j++)
				{
					val=LogUtils.logAdd(val,bwd[j][t+1]+Math.log(tr[s][j])+Math.log(em[j][obvsId[t+1]]));
				}
				bwd[s][t]=val;
			}
		}
//		for(int i=0;i<numStates;i++)
//			{
//				for(int j=0;j<observs.size();j++)
//					System.out.print(bwd[i][j]+" ");
//				System.out.println();
//			}
		System.out.println("backward pass finished!");
		return bwd;
	}
	public Double[][] computeForward(List<String> observs){
		int[] obvsId=observToIds(observs);
		Double[][] fwd=new Double[numStates][observs.size()];
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]=Math.log(init_state[i])+Math.log(em[i][obvsId[0]]);
		}
		for(int t=1;t<obvsId.length;t++)
		{
			for(int s=0;s<numStates;s++)
			{
				Double val=Double.NEGATIVE_INFINITY;
				for(int j=0;j<numStates;j++)
				{
					val=LogUtils.logAdd(val,fwd[j][t-1]+Math.log(tr[j][s]));
				}
				val+=em[s][obvsId[t]];
				fwd[s][t]=val;
			}
		}
//		for(int i=0;i<numStates;i++)
//		{
//			for(int j=0;j<observs.size();j++)
//				System.out.print(fwd[i][j]+" ");
//			System.out.println();
//		}
		System.out.println("forward pass finished!");
		return fwd;
	}
//	private List<Double[][]> computeForwardAndBackward(List<String> observs) 
//	{
//		int[] obvsId=observToIds(observs);
//		
//		Double[][] fwd=new Double[numStates][observs.size()];
//		Double[][] bwd=new Double[numStates][observs.size()];
////		Double[] C= new Double[observs.size()];	// partition function
////		C[0]=0.0;
////		ArrayList<Double> temp= new ArrayList<Double>();
//		for(int i=0;i<numStates;i++)
//		{
//			fwd[i][0]=Math.log(init_state[i])+Math.log(em[i][obvsId[0]]);
////			temp.add(fwd[i][0]);
//		}
////		LogUtils.logPartition(temp);
////		
////		assert C[0]!=0.0;
////		
////		for(int i=0;i<numStates;i++)
////		{
////			fwd[i][0]/=C[0];
////		}
//		
//		for(int t=1;t<obvsId.length;t++)
//		{
////			C[t]=0.0;
//			for(int s=0;s<numStates;s++)
//			{
//				Double val=0.0;
//				for(int j=0;j<numStates;j++)
//				{
//					val+=fwd[j][t-1]*tr[j][s];
//				}
//				val*=em[s][obvsId[t]];
//				fwd[s][t]=val;
//				C[t]+=fwd[s][t];
//			}
//			assert C[t]!=0.0;
//			for(int s=0;s<numStates;s++)
//			{
//				fwd[s][t]/=C[t];
//			}
//		}
//		// Z[t] computed for all time t !!
//		
//		for(int i=0;i<numStates;i++)
//		{
//			bwd[i][observs.size()-1]=1/C[observs.size()-1];
//		}
//		
//		for(int t=observs.size()-2;t>=0;t--)
//		{
//			for(int s=0;s<numStates;s++)
//			{
//				Double val = 0.0;
//				for(int j=0;j<numStates;j++)
//				{
//					val+=bwd[j][t+1]*tr[j][s]*em[j][obvsId[t+1]];
//				}
//				bwd[s][t]=val/C[t];
//			}
//		}
//		List<Double[][]> ls=new ArrayList<Double[][]>();
//		ls.add(fwd);
//		ls.add(bwd);
//		return ls;
//	}
	
	public List<Integer> Viterbi(List<String> observs)
	{
		int[] obvsId=observToIds(observs);
		
		Double[][] dp=new Double[numStates][observs.size()];
		// stores max probability of being in state s and seeing observation with id obv
		int[][] bp= new int[numStates][observs.size()];
		// backpointers
		
		for(int i=0;i<numStates;i++)
		{
			dp[i][0]=-Math.log(init_state[i])-Math.log(em[i][obvsId[0]]);	// init in i and emitting the first observation
//			System.out.println(dp[i][0]);
		}
		
		Double val,min;
		int argmin=-1;
		
		for(int t=1;t<observs.size();t++)
		{
			for(int i=0;i<numStates;i++)
			{
				min=dp[0][t-1]-Math.log(tr[0][i])-Math.log(em[i][obvsId[t]]);	
				argmin=0;		
				for(int j=1;j<numStates;j++)
				{
//					System.out.println(dp[j][t-1]);
					val= dp[j][t-1]-Math.log(tr[j][i])-Math.log(em[i][obvsId[t]]);
					if(min > val)
					{
//						System.out.println("UPDATING");
						min=val;
						argmin=j;
					}
//					System.out.println("STATE"+j+val+"and"+min);
//					if(min == Double.POSITIVE_INFINITY)
//					{
//						System.out.println("OH NO!!");
//						System.exit(-1);
//					}
				}
//				System.out.print("min should not be infinity! "+min+"argmin is "+argmin);
				dp[i][t]=min;
				bp[i][t]=argmin;
			}
//			System.out.println();
		}
		// print
		ArrayList<Integer> ans = new ArrayList<Integer>();
		int position=-1;
		for(int t=0;t<observs.size();t++)
		{
			min=Double.POSITIVE_INFINITY;
			for(int i=0;i<numStates;i++)
			{
//				System.out.print(dp[i][t]+" ");
				if(min>dp[i][t])
				{
					min=dp[i][t];
					position=i;
				}
			}
//			System.out.println(min+" at "+position);
			ans.add(position);
		}
//		assert (argmin!=-1) :  "bad end state";
//		
//		int[] answerIds= new int[observs.size()];
//		answerIds[observs.size()-1]=argmin;
//		int prev;
//		for(int i=observs.size()-2;i>=0;i--)
//		{
//			prev=answerIds[i+1];
//			answerIds[i]=bp[prev][i+1];
//		}
//		for(int i=0;i<answerIds.length;i++)
//			System.out.println(answerIds[i]);
//		return null;
//		System.out.println("Finished!");
		return ans;
		
	}
	public int[] observToIds(List<String>observs)
	{
		int[] obvsId=new int[observs.size()];	// map obvs to their ids
		for(int i=0;i<observs.size();i++)
			obvsId[i]=lexicon.getObservId(observs.get(i));
		return obvsId;
	}
//	public static void main(String[] args) {
////		HMM2 hmm=new HMM2();
//	}
	public void updateParameters(List<String> observs, Double[][] gamma,
			Double[][][] epsilon) {
		for(int i=0;i<numStates;i++)
			init_state[i]=gamma[0][i];
		double numerator=Double.NEGATIVE_INFINITY;
		double denominator=Double.NEGATIVE_INFINITY;
		for(int i=0;i<numStates;i++)
		{
			for (int j = 0; j <numStates; j++) 
			{
				for(int t=0;t<observs.size()-1;t++)
				{
					numerator=LogUtils.logAdd(numerator, epsilon[t][i][j]);
					denominator=LogUtils.logAdd(denominator, gamma[t][i]);
				}
				tr[i][j]=Math.exp(numerator-denominator);
			}
		}
		
		System.out.println("Update Done!");
	}
	public void printTr()
	{
		System.out.println("Printing TR");
		for (int i = 0; i < tr.length; i++) {
			for (int j = 0; j < tr[0].length; j++) {
				System.out.print(tr[i][j]+" ");
			}
			System.out.println();
		}
	}
}
