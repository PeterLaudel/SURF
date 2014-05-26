package FeatureMatching;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import Features.InterestPoint;

public class KDTree {

	public class Node {
		float value;
		InterestPoint interestPoint;
		Node leftChild;
		Node rightChild;
		int axis;

		public Node(float value, int axis, InterestPoint interestPoint, Node leftChild, Node rightChild) {
			this.value = value;
			this.interestPoint = interestPoint;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
			this.axis = axis;
		}
	}
	
	public Vector<Vector<Matches>> KnnMatching(Vector<InterestPoint> interestPoints1, Vector<InterestPoint> interestPoints2, int neighbors)
	{
		Vector<Vector<Matches>> result = new Vector<Vector<Matches>>(interestPoints2.size());
		Vector<Matches> matchVec =  new Vector<Matches>(neighbors + 1);
		for(int i = 0; i < neighbors; i++)
			matchVec.add(new Matches(-1, -1, Float.MAX_VALUE));

		
		Node root = ComputeKdTree(interestPoints1, 0);
		
		for(int i = 0; i < interestPoints2.size(); i++)
		{
			Vector<Matches> tmpMatchVec = (Vector<Matches>) matchVec.clone();
			NearestNeighbor(root, interestPoints2.get(i), tmpMatchVec, i);
			for(int j = 0; j < tmpMatchVec.size(); j++)
				tmpMatchVec.get(j).distance = (float) Math.sqrt(tmpMatchVec.get(j).distance);
			result.add(tmpMatchVec);
		}
		
		return result;
		
	}
	
	public void NearestNeighbor(Node node, InterestPoint destination, Vector<Matches> matchVec, int index)
	{
		
		if(node == null)
			return;
		
		Matching match = new Matching();
		
		float distance = match.GetEuclidianDistance(node.interestPoint, destination);
		
		if(distance < matchVec.lastElement().distance)
			for(int i = 0; i < matchVec.size(); i++)
				if(matchVec.get(i).distance > distance)
				{
					matchVec.insertElementAt(new Matches(i, index, distance), i);
					matchVec.remove(matchVec.size() - 1);
					break;
				}

		float diff = destination.descriptor[node.axis] - node.interestPoint.descriptor[node.axis];
		Node close, far;
		
		if(diff <= 0)
		{
			close = node.rightChild;
			far = node.leftChild;
		}
		else
		{
			close = node.leftChild;
			far = node.rightChild;
		}
		
		NearestNeighbor(close, destination, matchVec, index);
		
		if(diff * diff  < matchVec.firstElement().distance)
			NearestNeighbor(far, destination, matchVec, index);
		
	}

	public Node ComputeKdTree(Vector<InterestPoint> interestPoints, int depth) {
		if (interestPoints.size() == 0)
			return null;

		int index = depth % interestPoints.get(0).descriptor.length;

		Collections.sort(interestPoints, new InterestPointComparator(index));

		int medianIndex = (int) (interestPoints.size() * 0.5f);
		InterestPoint ip = interestPoints.get(medianIndex);
		
		return new Node(ip.descriptor[index], depth, ip,
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
			Float obj1 = ip1.descriptor[m_index];
			Float obj2 = ip2.descriptor[m_index];
			return obj1.compareTo(obj2);
		}
		
	}

}
