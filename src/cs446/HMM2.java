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
	
	public Double[] init_update;
	ArrayList<int[]> non_zero_em;
	Double[][] tr_numerator;
	Double[][] tr_denominator;
	Double[][] em_numerator;
	Double[][] em_denominator;
	
	public HMM2(Double[][] tr, Double[][] em, Double[] init_state,
			int numStates, Lexicon lexicon) {
		this.tr = tr;
		this.em = em;
		this.init_state = init_state;
		this.numStates = numStates;
		this.lexicon = lexicon;
		non_zero_em=new ArrayList<int[]>();
		for (int i = 0; i < em.length; i++) {
			for (int j = 0; j < em[0].length; j++) {
				if(em[i][j]!=0.0)
					non_zero_em.add(new int[]{i,j});
			}
		}
//		for(int[] a:non_zero_em)
//			System.out.println(a[0]+" "+a[1]);
//		
		init_update=new Double[init_state.length];
		
		tr_numerator= new Double[tr.length][tr[0].length];
		tr_denominator= new Double[tr.length][tr[0].length];
		
		em_numerator= new Double[em.length][em[0].length];
		em_denominator= new Double[em.length][em[0].length];
		
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
//		System.out.println("Printing Epsilon");
//		for(int t=0;t<observs.size()-1;t++)
//		{
//			for(int i=0;i<numStates;i++)
//			{
//				for(int j=0;j<numStates;j++)
//				{
//					System.out.print(epsilon[t][i][j]+" ");
//				}
//			}
//			System.out.println();
//		}
//		System.out.println("Epsilon computed!");
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
//		System.out.println("Printing Gamma");
//		for(int t=0;t<observs.size();t++)
//		{
//			for(int i=0;i<numStates;i++)
//			{
//				System.out.print(gamma[t][i]+" ");
//			}
//			System.out.println();
//		}
		
//		System.out.println("Gamma computed!");
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
//		System.out.println("Printing Backward");
//		for(int i=0;i<numStates;i++)
//		{
//			for(int j=0;j<observs.size();j++)
//				System.out.print(bwd[i][j]+" ");
//			System.out.println();
//		}
//		System.out.println("backward pass finished!");
		return bwd;
	}
	public Double[][] computeForward(List<String> observs){
		int[] obvsId=observToIds(observs);
		Double[][] fwd=new Double[numStates][observs.size()];
		for(int i=0;i<numStates;i++)
		{
			fwd[i][0]=Math.log(init_state[i])+Math.log(em[i][obvsId[0]]);
			if(Double.isNaN(fwd[i][0]))
				System.out.println("VAL "+Math.log(init_state[i])+"VAL 2 "+Math.log(em[i][obvsId[0]]));
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
//		System.out.println("Printing Forward");
//		for(int i=0;i<numStates;i++)
//		{
//			for(int j=0;j<observs.size();j++)
//			{
//				if(Double.isNaN((fwd[i][j])))
//						System.out.println("ERROR! "+i+" "+j);
////				System.out.print(fwd[i][j]+" ");
//			}
//			System.out.println();
//		}
//		System.out.println("forward pass finished!");
		return fwd;
	}
//	
	
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
	public void resetUpdates(){
		for(int i=0;i<numStates;i++)
		{
			init_update[i]=0.0;
//			System.out.println(init_state[i]);
		}
		for (int i = 0; i < tr.length; i++) {
			for (int j = 0; j < tr[0].length; j++) {
				tr_numerator[i][j]=Double.NEGATIVE_INFINITY;
				tr_denominator[i][j]=Double.NEGATIVE_INFINITY;
			}
		}
		for (int i = 0; i < em.length; i++) {
			for (int j = 0; j < em[0].length; j++) {
				em_numerator[i][j]=Double.NEGATIVE_INFINITY;
				em_denominator[i][j]=Double.NEGATIVE_INFINITY;
			}
		}
		
	}
	public void updateParameters(List<String> observs, Double[][] gamma,
			Double[][][] epsilon) {
		int[] obvId = observToIds(observs);
		for(int i=0;i<numStates;i++)
		{
			init_update[i]+=Math.exp(gamma[0][i]);
//			init_state[i]=Math.exp(gamma[0][i]);
//			System.out.println(init_state[i]);
		}
		
		for(int i=0;i<numStates;i++)
		{
			for (int j = 0; j <numStates; j++) 
			{
				for(int t=0;t<observs.size()-1;t++)
				{
					tr_numerator[i][j]=LogUtils.logAdd(tr_numerator[i][j], epsilon[t][i][j]);
					tr_denominator[i][j]=LogUtils.logAdd(tr_denominator[i][j], gamma[t][i]);
//					System.out.println("Gamma added was "+gamma[t][i]);
				}
//				if(Double.isNaN(Math.exp(numerator-denominator)))
//				{
//					System.err.println("ALERT!! YOU are now entering NaN Domain! You have been warned!");
//					System.out.println(numerator+" "+denominator+" "+(numerator-denominator)+" exp"+Math.exp(numerator-denominator));
////					System.exit(-1);
//				}
////				System.out.println(numerator-denominator+" exp"+Math.exp(numerator-denominator));
//				tr_update[i][j]+=Math.exp(numerator-denominator);
////				tr[i][j]=numerator-denominator;
			}
		}
		
		for(int[]a:non_zero_em)
		{
			int j=a[0];
			int k=a[1];
			for(int t=0;t<observs.size();t++)
			{
				if(obvId[t]==k)
				{
					em_numerator[j][k]=LogUtils.logAdd(em_numerator[j][k], gamma[t][j]);
				}
				em_denominator[j][k]=LogUtils.logAdd(em_denominator[j][k], gamma[t][j]);
	//					System.out.println("Gamma added was "+gamma[t][i]);
			}
		}
//		System.out.println("Update Done!");
	}
	public void printTrUpdate(){
		for (int i = 0; i < tr.length; i++) {
			for (int j = 0; j < tr[0].length; j++) {
				System.out.print(Math.exp(tr_numerator[i][j]-tr_denominator[i][j])+" ");
			}
			System.out.println();
		}
	}
	public void printEmUpdate(){
		for(int[]a:non_zero_em)
		{
			int j=a[0];
			int k=a[1];
			System.out.print(Math.exp(em_numerator[j][k]-em_denominator[j][k])+"__");
		}
	}
	public void printInitUpdate()
	{
		System.out.println("Printing InitUpdate");
		for (int i = 0; i < init_update.length; i++) {
			System.out.print(init_update[i]+" ");
		}
		System.out.println();
	}
	
	
	public void updateInit(int counter) {
		double sum=0.0;
		for(int i=0;i<numStates;i++)
		{
//			init_update[i]+=Math.exp(gamma[0][i]);
			init_state[i]=init_update[i]/counter;
			sum+=init_state[i];
//			System.out.println(init_state[i]);
		}
		assert sum==1.0;
	}
	public void updateTr(int counter) {
		double sum;
		for (int i = 0; i < tr.length; i++) {
			sum=0.0;
			for (int j = 0; j < tr[0].length; j++) {
				tr[i][j]=Math.exp(tr_numerator[i][j]-tr_denominator[i][j])/counter;
				sum+=tr[i][j];
			}
			assert sum==1.0;
		}
	}
	public void updateEM(int counter){
		for(int[]a:non_zero_em)
		{
			int j=a[0];
			int k=a[1];
			em[j][k]=Math.exp(em_numerator[j][k]-em_denominator[j][k])/counter;
		}
		double sum;
		for (int i = 0; i < em.length; i++) {
			sum=0.0;
			for (int j = 0; j < em[0].length; j++) {
				sum+=em[i][j];
			}
			assert sum==1.0;
		}
	}
	
	public void printInit()
	{
		System.out.println("Printing Init");
		for (int i = 0; i < init_state.length; i++) {
			System.out.print(init_state[i]+" ");
		}
		System.out.println();
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
	public void printEm(){
		System.out.println("Printing EM");
		for (int i = 0; i < em.length; i++) {
			for (int j = 0; j < em[0].length; j++) {
				System.out.print(em[i][j]+" ");
			}
			System.out.println();
		}
		
	}
	
}
