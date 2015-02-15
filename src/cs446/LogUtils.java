package cs446;

import java.util.List;

public class LogUtils {

	public static float logAdd(float logX, float logY) {
		// 1. make X the max
	       if (logY > logX) {
	           float temp = logX;
	           logX = logY;
	           logY = temp;
	       }
	       // 2. now X is bigger
	       if (logX == Float.NEGATIVE_INFINITY) {
	           return logX;
	       }
	       // 3. how far "down" (think decibels) is logY from logX?
	       //    if it's really small (20 orders of magnitude smaller), then ignore
	       float negDiff = logY - logX;
	       if (negDiff < -20) {
	           return logX;
	       }
	       // 4. otherwise use some nice algebra to stay in the log domain
	       //    (except for negDiff)
	       return (float) (logX + java.lang.Math.log(1.0f + java.lang.Math.exp(negDiff)));
	}
	
	public static double logAdd(double logX, double logY) {
	       // 1. make X the max
	       if (logY > logX) {
	           double temp = logX;
	           logX = logY;
	           logY = temp;
	       }
	       // 2. now X is bigger
	       if (logX == Double.NEGATIVE_INFINITY) {
	           return logX;
	       }
	       // 3. how far "down" (think decibels) is logY from logX?
	       //    if it's really small (20 orders of magnitude smaller), then ignore
	       double negDiff = logY - logX;
	       if (negDiff < -20) {
	           return logX;
	       }
	       // 4. otherwise use some nice algebra to stay in the log domain
	       //    (except for negDiff)
	       return logX + java.lang.Math.log(1.0 + java.lang.Math.exp(negDiff));

	}
	public static double logPartition(double[] logArray)
	{
		double Z=logAdd(logArray[0],logArray[1]);
		for(int i=2;i<logArray.length;i++)
			Z+=logAdd(Z,logArray[i]);
		return Z;
	}
	public static double logPartition(List<Double> logArray)
	{
		double Z=logAdd(logArray.get(0),logArray.get(1));
		for(int i=2;i<logArray.size();i++)
			Z+=logAdd(Z,logArray.get(i));
		return Z;
	}
}
