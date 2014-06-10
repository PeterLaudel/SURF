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
	
	/**
     * Do a symmetry test by using the descriptor1, descriptor2 and descriptor2, descriptor1 matching.
     * 
     * @param matches1 the matches of the descriptor1, descriptor2 matches
     * @param matches2 the matches of the descriptor2, descriptor1 matches
     * @return mat of matches which have symmetry
     */
	public static List<Matches> DoSymmetryTest(List<Matches> matches1, List<Matches> matches2)
	{

		List<Matches> resultMatches = new Vector<Matches>();
		for(int i = 0; i < matches1.size(); i++)
		{
			Matches match1 = matches1.get(i);
			for(int j = 0; j < matches2.size(); j++)
			{
				
				Matches match2 = matches2.get(j);
				if(match1.idx1== match2.idx2 && match1.idx1 == match2.idx2)
				{
					resultMatches.add(match1);
					break;
				}
			}
		}

		return resultMatches;
	}

}
