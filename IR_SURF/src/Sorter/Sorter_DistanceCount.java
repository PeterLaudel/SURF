package Sorter;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import FeatureMatching.BruteForceMatching;
import FeatureMatching.FeatureMatchFilter;
import FeatureMatching.Matches;
import Features.InterestPoint;
import PicPropertys.Pic;
import PicPropertys.PicSurf;
import app.Sorter;
import app.SurfBinaryFile;

public class Sorter_DistanceCount implements Sorter {
	
	PicSurf[] m_picSurf;
	String m_path;
	int m_count;
	float m_threshold;
	String m_fileName;

	public Sorter_DistanceCount(Pic[] pics, String path, int count, float threshold, String fileName) {
		// TODO Auto-generated constructor stub
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		m_count = count;
		m_threshold = threshold;
		m_fileName = fileName;
		getFeatureVectors();
	}

	@Override
	public void getFeatureVectors() {
		SurfBinaryFile sxmlf = new SurfBinaryFile(m_path, m_fileName);
		Map<Integer, List<InterestPoint>> fileMap = sxmlf.ReadSurfBinaryFile(m_count);
		if(fileMap == null)
			return;
		for(int i = 0; i < m_picSurf.length; i++)
		{
			PicSurf surfpic = m_picSurf[i];
			surfpic.interestPoints = fileMap.get(surfpic.pic.name.hashCode());
		}
	}

	@Override
	public void sortBySimilarity() {
		// TODO Auto-generated method stub
		int number = m_picSurf.length;

		int q = -1;
		for (int i = 0; i < m_picSurf.length; i++) {
			if (m_picSurf[i] != null && m_picSurf[i].pic.isSelected) {
				q = i;
				break;
			}
		}
		if (q == -1)
			return;
		
		DistComparator distComparator = new DistComparator();
		TreeSet<Pic> treeSet = new TreeSet<Pic>(distComparator);
		SortedSet<Pic> resultList = treeSet;

		for (int n = 0; n < number; n++) {
			PicSurf actPic = m_picSurf[n]; 
			if (actPic != null) {
				computeDistance(q, n);
				resultList.add(actPic.pic);
			}
		}

		Iterator<Pic> it = resultList.iterator();
		int n = 0; 
		while(it.hasNext()){

			Pic pic = (Pic) it.next();
			if (pic != null) {
				pic.rank = n++;
			}
		}	
	}
	
	class DistComparator implements Comparator<Object> {
		public int compare( java.lang.Object p1, java.lang.Object p2 ) {
			Double d1 = ((Pic) p1).distance;
			Double d2 = ((Pic) p2).distance;
			
			if( d1 < d2 ) 
				return -1;
			else if( d1 > d2 ) 
				return 1;
			else if ( ((Pic)p1).id == ((Pic) p2).id)
				return 0;
			else 
				return 1;
			
		}
	}

	@Override
	public void computeDistance(int queryPic, int actPic) {
		// TODO Auto-generated method stub
		PicSurf query = m_picSurf[queryPic];
		PicSurf act = m_picSurf[actPic]; 
		
		if(act.interestPoints == null || query.interestPoints == null)
		{
			act.pic.distance = Float.MAX_VALUE;
			return;
		}
		BruteForceMatching bfm = new BruteForceMatching();
		List<Matches> match = bfm.BFMatch(act.interestPoints, query.interestPoints);
		
		//finalMatch = FeatureMatchFilter.
		//List<Matches> finalMatch = FeatureMatchFilter.DoSurfResponseTest(match1, actPic.interestPoints, queryPic.interestPoints);
		
		int count = FeatureMatchFilter.CountDistanceThreshold(match, m_threshold);
		//finalMatch = FeatureMatchFilter.DoResponseRatioTest(finalMatch, actPic.interestPoints, queryPic.interestPoints);
		//double dist = getEuclidianDistance((Pic) actPic, (Pic) queryPic);
		act.pic.distance = m_count - count;
		
		
	}


}