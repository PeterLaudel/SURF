package FeatureMatching;

import java.util.List;
import java.util.Vector;

public class FeatureMatchFilter {
	
	public static List<Matches> DoRatioTest(List<List<Matches>> knnMatches)
	{
		List<Matches> resultMatches = new Vector<Matches>();

		for(int i = 0; i< knnMatches.size(); i++)
		{
			List<Matches> tmpMatches = knnMatches.get(i);
			if(tmpMatches.size() > 1)
			{
				if(tmpMatches.get(0).distance / tmpMatches.get(1).distance < 0.8f)
				{
					resultMatches.add(tmpMatches.get(0));
				}
			}
		}
		
		
		return resultMatches;
	}

}
