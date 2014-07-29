package FeatureMatching;

import java.util.ArrayList;
import java.util.List;

import Features.InterestPoint;

public class FeatureMatchFilter {
	
	public static List<Matches> DoRatioTest(List<List<Matches>> knnMatches)
	{
		List<Matches> resultMatches = new ArrayList<Matches>((int) (knnMatches.size() * 0.7f));

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

		List<Matches> resultMatches = new ArrayList<Matches>((int) (matches1.size() * 0.7f));
		for(int i = 0; i < matches1.size(); i++)
		{
			Matches match1 = matches1.get(i);
			for(int j = 0; j < matches2.size(); j++)
			{
				
				Matches match2 = matches2.get(j);
				if((match1.idx1 == match2.idx1 && match1.idx2 == match2.idx2) ||
				   (match1.idx1 == match2.idx2 && match1.idx2 == match2.idx1))
				{
					resultMatches.add(match1);
					break;
				}
			}
		}

		return resultMatches;
	}
	
	public static List<Matches> DoSurfResponseTest(List<Matches> matches, List<InterestPoint> interestPoints1, List<InterestPoint> interestPoints2)
	{
		List<Matches> result = new ArrayList<Matches>((int) (matches.size() * 0.7f));
		
		for(int i = 0; i < matches.size(); i++)
		{
			Matches match = matches.get(i);
			InterestPoint ip1 = interestPoints1.get(match.idx1);
			InterestPoint ip2 = interestPoints2.get(match.idx2);
			
			if(ip1.negative != ip2.negative)
				continue;
			
			result.add(match);
		}
		
		return result;
	}
	
	public static List<Matches> DoDistanceThreshold(List<Matches> matches, float threshold)
	{
		List<Matches> result = new ArrayList<Matches>((int) (matches.size() * 0.7f));
		
		for(int i = 0; i < matches.size(); i++)
		{
			Matches match = matches.get(i);
			if(match.distance < threshold)
				result.add(match);
		}
		return result;
	}
	
	public static int CountDistanceThreshold(List<Matches> matches, float threshold)
	{
		int count = 0;
		for(int i = 0; i < matches.size(); i++)
		{
			Matches match = matches.get(i);
			if(match.distance < threshold)
				count++;
		}
		return count;
	}
	
	
	public static List<Matches> DoResponseRatioTest(List<Matches> matches, List<InterestPoint> interestPoints1, List<InterestPoint> interestPoints2)
	{
		List<Matches> result = new ArrayList<Matches>((int) (matches.size() * 0.7f));
		
		for(int i = 0; i < matches.size(); i++)
		{
			Matches match = matches.get(i);
			InterestPoint ip1 = interestPoints1.get(match.idx1);
			InterestPoint ip2 = interestPoints2.get(match.idx2);
			float valAbs1 = Math.abs(ip1.value);
			float valAbs2 = Math.abs(ip2.value);
			float responseMax = Math.max(valAbs1, valAbs2);
			float responseMin = Math.min(valAbs1, valAbs2);
			if(responseMax / responseMin > 0.8)
				continue;
			
			result.add(match);
		}
		return result;
	}

}
