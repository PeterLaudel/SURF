package FeatureMatching;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import Features.InterestPoint;

public class KDTree {

	public class Node {
		float value;
		Node leftChild;
		Node rightChild;

		public Node(float value, Node leftChild, Node rightChild) {
			this.value = value;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
		}
	}

	public Node ComputeKdTree(Vector<InterestPoint> interestPoints, int depth) {
		if (interestPoints.size() == 0)
			return null;

		int index = depth % interestPoints.get(0).descriptor.length;

		Collections.sort(interestPoints, new InterestPointComparator(index));

		int medianIndex = (int) (interestPoints.size() * 0.5f);

		return new Node(interestPoints.get(medianIndex).descriptor[index], 
				ComputeKdTree(new Vector<InterestPoint>(interestPoints.subList(0, medianIndex)), depth + 1),
				ComputeKdTree(new Vector<InterestPoint>(interestPoints.subList(medianIndex + 1, interestPoints.size())), depth + 1));

	}
	
	private class InterestPointComparator implements Comparator<InterestPoint>
	{
		private int m_index;
		public InterestPointComparator(int index)
		{
			m_index = index;
		}
		@Override
		public int compare(InterestPoint ip1, InterestPoint ip2) {
			// TODO Auto-generated method stub
			Float obj1 = ip1.descriptor[m_index];
			Float obj2 = ip2.descriptor[m_index];
			return obj1.compareTo(obj2);
		}
		
	}

}
