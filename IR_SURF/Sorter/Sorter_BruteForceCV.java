package Sorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;

import Features.InterestPoint;
import PicPropertys.Pic;
import PicPropertys.PicSurf;
import app.Sorter;
import app.SurfBinaryFile;

public class Sorter_BruteForceCV implements Sorter {
	
	PicSurf[] m_picSurf;
	String m_path;
	int m_count;
	float m_threshold;

	public Sorter_BruteForceCV(Pic[] pics, String path, int count, float threshold) {
		// TODO Auto-generated constructor stub
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		m_count = count;
		m_threshold = threshold;
		getFeatureVectors();
	}

	@Override
	public void getFeatureVectors() {
		SurfBinaryFile sxmlf = new SurfBinaryFile(m_path, "descriptorCV");
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
	
	private MatOfDMatch doSymmetryTest(MatOfDMatch matches1, MatOfDMatch matches2)
	{
		DMatch[] matchesVector1 = matches1.toArray();
		DMatch[] matchesVector2 = matches2.toArray();
		List<DMatch> resultMatches = new ArrayList<DMatch>();
		for(int i = 0; i < matchesVector1.length; i++)
		{
			for(int j = 0; j < matchesVector2.length; j++)
			{
				if(matchesVector1[i].queryIdx == matchesVector2[j].trainIdx && matchesVector1[i].trainIdx == matchesVector2[j].queryIdx)
				{
					resultMatches.add(matchesVector1[i]);
					break;
				}
			}
		}
		MatOfDMatch resultMat = new MatOfDMatch();
		resultMat.fromList(resultMatches);
		return resultMat;
	}
	
	private MatOfDMatch doDistanceTest(MatOfDMatch matches, float distance)
	{
		DMatch[] matchesVector1 = matches.toArray();
		List<DMatch> resultMatches = new ArrayList<DMatch>();
		
		for(int i = 0; i < matchesVector1.length; i++)
		{
			if(matchesVector1[i].distance < 0.000001)
			{
				resultMatches.add(matchesVector1[i]);
			}
		}
		MatOfDMatch resultMat = new MatOfDMatch();
		resultMat.fromList(resultMatches);
		return resultMat;
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
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		DescriptorMatcher dm = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
		Mat queryDescriptors = new Mat(query.interestPoints.size(), query.interestPoints.get(0).descriptor.length, CvType.CV_32FC1);
		Mat trainDescriptors = new Mat(act.interestPoints.size(), act.interestPoints.get(0).descriptor.length, CvType.CV_32FC1);
		
		for(int i = 0; i < query.interestPoints.size(); i++)
			for(int j = 0; j < query.interestPoints.get(i).descriptor.length; j++)
			{
				queryDescriptors.put(i, j, query.interestPoints.get(i).descriptor[j]);
			}
		
		for(int i = 0; i < act.interestPoints.size(); i++)
			for(int j = 0; j < act.interestPoints.get(i).descriptor.length; j++)
			{
				trainDescriptors.put(i, j, act.interestPoints.get(i).descriptor[j]);
			}
		
		MatOfDMatch matches1 = new MatOfDMatch();
		dm.match(queryDescriptors, trainDescriptors, matches1);
		MatOfDMatch matches2 = new MatOfDMatch();
		dm.match(trainDescriptors, queryDescriptors, matches2);
		
		MatOfDMatch finalMatch = doSymmetryTest(matches1, matches2);
		//finalMatch = FeatureMatchFilter.
		//List<Matches> finalMatch = FeatureMatchFilter.DoSurfResponseTest(match1, actPic.interestPoints, queryPic.interestPoints);
		
		finalMatch = doDistanceTest(finalMatch, m_threshold);
		//finalMatch = FeatureMatchFilter.DoResponseRatioTest(finalMatch, actPic.interestPoints, queryPic.interestPoints);
		//double dist = getEuclidianDistance((Pic) actPic, (Pic) queryPic);
		
		System.out.println("Query:" + query.interestPoints.size() + "/" + queryDescriptors.rows());
		System.out.println("Act:" + act.interestPoints.size() + "/" + trainDescriptors.rows());
		act.pic.distance = query.interestPoints.size() - finalMatch.rows();
		System.out.println("Distance: "  + act.pic.distance);
		
		
		
	}


}
