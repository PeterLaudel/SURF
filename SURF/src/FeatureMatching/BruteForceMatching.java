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
			InterestPoint ip1 = interestPoints1.get(i);
			float tmpDistance = Float.MAX_VALUE;
			int tmpIndex = -1;
			for(int j = 0; j < interestPoints2.size(); j++)
			{
				
				InterestPoint ip2 = interestPoints2.get(j);
				
				if(ip1.negative != ip2.negative)
					continue;
				
				float distance = Matching.GetEuclidianDistance(ip1, ip2);
				
				if(distance < tmpDistance)
				{
					tmpIndex = j;
					tmpDistance = distance;
				}
			}
			if(tmpIndex != -1)
				result.add(new Matches(i, tmpIndex, (float) Math.sqrt(tmpDistance)));
		}
		
		return result;
	}
}
