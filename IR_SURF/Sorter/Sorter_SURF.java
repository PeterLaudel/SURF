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
import Imageprocess.Image;
import IntegralImage.IntegralImage;
import PicPropertys.Pic;
import PicPropertys.PicSurf;
import SURF.SurfFeatureDescriptor;
import SURF.SurfFeatureDetector;
import app.Sorter;
import app.SurfXMLFile;

public class Sorter_SURF implements Sorter {
	
	PicSurf[] m_picSurf;
	String m_path;

	public Sorter_SURF(Pic[] pics, String path) {
		// TODO Auto-generated constructor stub
		m_path = path;
		m_picSurf = new PicSurf[pics.length];
		for(int i = 0; i < m_picSurf.length; i++)
		{
			m_picSurf[i] = new PicSurf(pics[i]);
		}
		getFeatureVectors();
	}

	@Override
	public void getFeatureVectors() {
		SurfFeatureDetector sfd = new SurfFeatureDetector(200, 4);
		SurfFeatureDescriptor sfdesc = new SurfFeatureDescriptor();
		SurfXMLFile sxmlf = new SurfXMLFile(m_path);
		Map<String, List<InterestPoint>> fileMap = sxmlf.ReadSurfXMLFile();
		for(int i = 0; i < m_picSurf.length; i++)
		{
			PicSurf surfpic = m_picSurf[i];
			if(fileMap != null && fileMap.containsKey(surfpic.pic.name))
			{
				surfpic.interestPoints = fileMap.get(surfpic.pic.name);
				continue;
			}
			
			Image image = new Image(surfpic.pic.bImage.getWidth(), surfpic.pic.bImage.getHeight());
			surfpic.pic.bImage.getRGB(0, 0, image.GetWidth(), image.GetHeight(), image.GetImagePixels(), 0, image.GetWidth());
			IntegralImage ii = new IntegralImage(image);
			sfd.Detect(ii, image.GetWidth(), image.GetHeight(), surfpic.interestPoints);
			sfdesc.Compute(ii, surfpic.interestPoints);
		}
		
		if(fileMap == null)
			sxmlf.WriteXMLFile(m_picSurf);
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


		PicSurf queryPic = m_picSurf[q];
		for (int n = 0; n < number; n++) {
			PicSurf actPic = m_picSurf[n]; 
			if (actPic != null) {
				List<Matches> match1 = BruteForceMatching.BFMatch(actPic.interestPoints, queryPic.interestPoints);
				List<Matches> match2 = BruteForceMatching.BFMatch(queryPic.interestPoints, actPic.interestPoints);
				
				List<Matches> finalMatch = FeatureMatchFilter.DoSymmetryTest(match1, match2);
				//double dist = getEuclidianDistance((Pic) actPic, (Pic) queryPic);
				actPic.pic.distance = actPic.interestPoints.size() -  finalMatch.size();
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
			double d1 = ((Pic) p1).distance;
			double d2 = ((Pic) p2).distance;	

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


}
