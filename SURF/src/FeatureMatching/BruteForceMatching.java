package FeatureMatching;

import java.util.List;
import java.util.Vector;

import Features.InterestPoint;

public class BruteForceMatching {

	public static List<Matches> BFMatch(List<InterestPoint> interestPoints1, List<InterestPoint> interestPoints2)
	{
		List<Matches> result = new Vector<Matches>();
		
		for(int i = 0; i < interestPoints1.size(); i++)
		{
			float tmpDistance = Float.MAX_VALUE;
			int tmpIndex = -1;
			for(int j = 0; j < interestPoints2.size(); j++)
			{
				float distance = Matching.GetEuclidianDistance(interestPoints1.get(i), interestPoints2.get(j));
				
				if(distance < tmpDistance)
				{
					tmpIndex = j;
					tmpDistance = distance;
				}
			}
			result.add(new Matches(i, tmpIndex, tmpDistance));
		}
		
		return result;
	}
}
