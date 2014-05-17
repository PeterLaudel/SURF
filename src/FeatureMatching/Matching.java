package FeatureMatching;

import java.util.Vector;

import Features.InterestPoint;

public class Matching {

	public Matching() {
		// TODO Auto-generated constructor stub
	}
	
	public void Match(Vector<InterestPoint> interestPoints1, Vector<InterestPoint> interestPoints2, Vector<Matches> matches)
	{
		for(int i = 0; i < interestPoints1.size(); i++)
		{
			InterestPoint ip1 = interestPoints1.get(i);
			int index = -1;
			float tmpDistance = Float.MAX_VALUE;
			for(int j = 0; j < interestPoints2.size(); j++)
			{		
				InterestPoint ip2 = interestPoints2.get(j);
				float distance = GetEuclidianDistance(ip1, ip2);
				if(distance < tmpDistance)
				{
					index = j;
					tmpDistance = distance;
				}
					
			}
			matches.add(new Matches(i, index, tmpDistance));
		}
	}
	

	public float GetEuclidianDistance(InterestPoint ip1, InterestPoint ip2)
	{
		float[] descriptor1 = ip1.descriptor;
		float[] descriptor2 = ip2.descriptor;
		
		if(descriptor1.length != descriptor2.length)
			return Float.MAX_VALUE;
		
		float sum = 0;
		for(int i = 0; i < descriptor1.length; i++)
		{
			float value = descriptor1[i] - descriptor2[i];
			sum += value * value;
		}
		
		return sum;
	}
	
	public float GetEuclidianDistanceSqrt(InterestPoint ip1, InterestPoint ip2)
	{
		return (float) Math.sqrt(GetEuclidianDistance(ip1, ip2));
	}

}
