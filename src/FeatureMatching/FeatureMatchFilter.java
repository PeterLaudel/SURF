package FeatureMatching;

import java.util.Vector;

public class FeatureMatchFilter {
	
	public static Vector<Matches> DoRatioTest(Vector<Vector<Matches>> knnMatches)
	{
		Vector<Matches> resultMatches = new Vector<Matches>();

		for(int i = 0; i< knnMatches.size(); i++)
		{
			Vector<Matches> tmpMatches = knnMatches.get(i);
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
