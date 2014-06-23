package Sorter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import PicPropertys.Pic;
import PicPropertys.PicSurf;
import Sorter.Sorter_SurfDistance.DistComparator;
import app.MatchXMLFile;
import app.Sorter;

public class Sorter_XMLFile implements Sorter {
	
	Pic[] m_pics;
	String m_path;
	Sorter m_sorter;
	String m_filename;

	public Sorter_XMLFile(Pic[] pics, String path, String filename) {
		// TODO Auto-generated constructor stub
		m_sorter = new Sorter_SurfDistance(pics, path);
		m_filename = filename;
		m_path = path;
		m_pics = pics;
		
		getFeatureVectors();
	}

	@Override
	public void getFeatureVectors() {
		m_sorter.getFeatureVectors();
	}

	@Override
	public void sortBySimilarity() {
		
		MatchXMLFile matchXMLFile = new MatchXMLFile(m_path, m_filename);
	
		Map<Integer, Map<Integer, Float>> matches = matchXMLFile.ReadMatches();
		int number = m_pics.length;

		int q = -1;
		for (int i = 0; i < m_pics.length; i++) {
			if (m_pics[i] != null && m_pics[i].isSelected) {
				q = i;
				break;
			}
		}
		if (q == -1)
			return;
		
		DistComparator distComparator = new DistComparator();
		TreeSet<Pic> treeSet = new TreeSet<Pic>(distComparator);
		SortedSet<Pic> resultList = treeSet;
		
		int queryHashId = m_pics[q].name.hashCode();
		Map<Integer, Float> distanceMap = matches.get(queryHashId);
		if(distanceMap == null)
		{
			distanceMap = new HashMap<Integer, Float>(m_pics.length);
			matches.put(queryHashId, distanceMap);
		}

		for (int n = 0; n < number; n++) {
			int actId = m_pics[n].name.hashCode();
			if(distanceMap.containsKey(actId))
			{
				m_pics[n].distance = distanceMap.get(actId);
			}
			else
			{
				m_sorter.computeDistance(q, n);
				distanceMap.put(actId, (float) m_pics[n].distance);
			}
			resultList.add(m_pics[n]);
			
		}

		Iterator<Pic> it = resultList.iterator();
		int n = 0; 
		while(it.hasNext()){

			Pic pic = (Pic) it.next();
			if (pic != null) {
				pic.rank = n++;
			}
		}	
		
		matchXMLFile.WriteMatches(matches);
		//m_sorter.sortBySimilarity();
		
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
		
	}


}
