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

public class Sorter_SurfDistance implements Sorter {
	
	PicSurf[] m_picSurf;
	String m_path;
	SurfBinaryFile m_xmlFile;
	int m_count;
	
	public Sorter_SurfDistance(Pic[] pics, String path, int count) {
		// TODO Auto-generated constructor stub
		
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		m_xmlFile = new SurfBinaryFile(m_path);
		m_count = count;
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		getFeatureVectors();
	}

	@Override
	public void getFeatureVectors() {
		SurfBinaryFile sxmlf = new SurfBinaryFile(m_path);
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
		
		PicSurf act = m_picSurf[actPic];
		PicSurf query = m_picSurf[queryPic];
		// TODO Auto-generated method stub
		//KDTree kdtree = new KDTree();
		//List<List<Matches>> knnMatch1 = kdtree.KnnMatching(actPic.interestPoints, queryPic.interestPoints, 2);
		//List<List<Matches>> knnMatch2 = kdtree.KnnMatching(queryPic.interestPoints, actPic.interestPoints, 2);
		
		//List<Matches> match1 = FeatureMatchFilter.DoRatioTest(knnMatch1);
		//List<Matches> match2 = FeatureMatchFilter.DoRatioTest(knnMatch2);
		
		//List<Matches> finalMatch = FeatureMatchFilter.DoSymmetryTest(match1, match2);
		if(act.interestPoints == null || query.interestPoints == null)
		{
			act.pic.distance = Float.MAX_VALUE;
			return;
		}
		
		List<Matches> match1 = BruteForceMatching.BFMatch(act.interestPoints, query.interestPoints);
		List<Matches> match2 = BruteForceMatching.BFMatch(query.interestPoints, act.interestPoints);
		
		List<Matches> finalMatch = FeatureMatchFilter.DoSymmetryTest(match1, match2);
		//finalMatch = FeatureMatchFilter.
		//List<Matches> finalMatch = FeatureMatchFilter.DoSurfResponseTest(match1, actPic.interestPoints, queryPic.interestPoints);
		//List<Matches> finalMatch = FeatureMatchFilter.DoDistanceThreshold(match1, 0.075f);
		//finalMatch = FeatureMatchFilter.DoResponseRatioTest(finalMatch, actPic.interestPoints, queryPic.interestPoints);
		//double dist = getEuclidianDistance((Pic) actPic, (Pic) queryPic);
		
		float distance = 0;
		for(int i = 0; i < finalMatch.size(); i++)
			distance += finalMatch.get(i).distance;
		
		act.pic.distance = distance * (float) query.interestPoints.size() / (float) finalMatch.size();
		
		
	}


}
