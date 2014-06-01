package FeatureMatching;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import Features.InterestPoint;

public class KDTree extends Matching{


	public class Node {
		float value;
		InterestPoint interestPoint;
		Node leftChild;
		Node rightChild;
		int axis;
		int index;

		public Node(float value, int axis, InterestPoint interestPoint, Node leftChild, Node rightChild, int index) {
			this.value = value;
			this.interestPoint = interestPoint;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
			this.axis = axis;
			this.index = index;
		}
	}
	
	public Vector<Vector<Matches>> KnnMatching(Vector<InterestPoint> interestPoints1, Vector<InterestPoint> interestPoints2, int neighbors)
	{
		Vector<Vector<Matches>> result = new Vector<Vector<Matches>>(interestPoints2.size());
		Vector<Matches> matchVec =  new Vector<Matches>(neighbors + 1);
		for(int i = 0; i < neighbors; i++)
			matchVec.add(new Matches(-1, -1, Float.MAX_VALUE));
		
		Vector<KDTreeContext> kdtreeContextPositive = new Vector<KDTree.KDTreeContext>(interestPoints1.size());
		Vector<KDTreeContext> kdtreeContextNegative = new Vector<KDTree.KDTreeContext>(interestPoints1.size());
		for(int i = 0; i < interestPoints1.size(); i++)
		{
			InterestPoint ip = interestPoints1.get(i);
			if(ip.negative)
				kdtreeContextNegative.add(new KDTreeContext(ip, i));
			else
			    kdtreeContextPositive.add(new KDTreeContext(ip, i));
		}
			

		
		
		Node rootNeg = ComputeKdTree(kdtreeContextNegative, 0);
		Node rootPos = ComputeKdTree(kdtreeContextPositive, 0);
		
		for(int i = 0; i < interestPoints2.size(); i++)
		{
			@SuppressWarnings("unchecked")
			Vector<Matches> tmpMatchVec = (Vector<Matches>) matchVec.clone();
			InterestPoint ip = interestPoints2.get(i);
			
			if(ip.negative)
				NearestNeighbor(rootNeg, ip, tmpMatchVec, i);
			else
				NearestNeighbor(rootPos, ip, tmpMatchVec, i);
			
			for(int j = 0; j < tmpMatchVec.size(); j++)
				tmpMatchVec.get(j).distance = (float) Math.sqrt(tmpMatchVec.get(j).distance);
			
			result.add(tmpMatchVec);
		}
		
		return result;
		
	}
	
	private void NearestNeighbor(Node node, InterestPoint destination, Vector<Matches> matchVec, int index)
	{
		
		if(node == null)
			return;
		
		Matching match = new Matching();
		
		float distance = match.GetEuclidianDistance(node.interestPoint, destination);
		
		if(distance < matchVec.lastElement().distance)
			for(int i = 0; i < matchVec.size(); i++)
				if(matchVec.get(i).distance > distance)
				{
					matchVec.insertElementAt(new Matches(node.index, index, distance), i);
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

	private Node ComputeKdTree(Vector<KDTreeContext> interestPoints, int depth) {
		if (interestPoints.size() == 0)
			return null;

		int index = depth % interestPoints.get(0).interestPoint.descriptor.length;

		Collections.sort(interestPoints, new KDTreeContextComparator(index));

		int medianIndex = (int) (interestPoints.size() * 0.5f);
		KDTreeContext kd = interestPoints.get(medianIndex);
		
		return new Node(kd.interestPoint.descriptor[index], depth, kd.interestPoint,
				ComputeKdTree(new Vector<KDTreeContext>(interestPoints.subList(0, medianIndex)), depth + 1),
				ComputeKdTree(new Vector<KDTreeContext>(interestPoints.subList(medianIndex + 1, interestPoints.size())), depth + 1), kd.index);

	}
	
	
	private class KDTreeContext
	{
		InterestPoint interestPoint;
		int index;
		
		public KDTreeContext(InterestPoint ip, int index)
		{
			interestPoint = ip;
			this.index = index;
		}
	}
	
	private class KDTreeContextComparator implements Comparator<KDTreeContext>
	{
		private int m_index;
		public KDTreeContextComparator(int index)
		{
			m_index = index;
		}
		@Override
		public int compare(KDTreeContext ip1, KDTreeContext ip2) {
			Float obj1 = ip1.interestPoint.descriptor[m_index];
			Float obj2 = ip2.interestPoint.descriptor[m_index];
			return obj1.compareTo(obj2);
		}
		
	}

}
