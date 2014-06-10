package FeatureMatching;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import Features.InterestPoint;

public class KDTree extends Matching{

	

	/**
	 * 
	 * @author Peter Laudel
	 * 
	 * @version 1.0
	 *
	 */
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
	
	/**
	 * Method for knnmatching for Surf Feature descriptor. Find the n closest neighbors to the given interest points.
	 * @param interestPoints1 of the first image
	 * @param interestPoints2 of the second image
	 * @param neighbors says how many closest neighbors has to be find
	 * @return
	 */
	public List<List<Matches>> KnnMatching(List<InterestPoint> interestPoints1, List<InterestPoint> interestPoints2, int neighbors)
	{
		//create the result vector
		List<List<Matches>> result = new Vector<List<Matches>>(interestPoints2.size());
		//the vec vector holds the current closest feature points
		List<Matches> matchVec =  new Vector<Matches>(neighbors + 1);
		for(int i = 0; i < neighbors; i++)
			matchVec.add(new Matches(-1, -1, Float.MAX_VALUE));
		
		//create the a node with the negatice or positive values
		Vector<KDTreeContext> kdtreeContextPositive = new Vector<KDTree.KDTreeContext>(interestPoints1.size());
		Vector<KDTreeContext> kdtreeContextNegative = new Vector<KDTree.KDTreeContext>(interestPoints1.size());
		
		//sort the interest points by dark or light feature points
		for(int i = 0; i < interestPoints1.size(); i++)
		{
			InterestPoint ip = interestPoints1.get(i);
			if(ip.negative)
				kdtreeContextNegative.add(new KDTreeContext(ip, i));
			else
			    kdtreeContextPositive.add(new KDTreeContext(ip, i));
		}
			
		//compute the root nodes of the neg or pos
		Node rootNeg = ComputeKdTree(kdtreeContextNegative, 0);
		Node rootPos = ComputeKdTree(kdtreeContextPositive, 0);
		
		//now match the second interest points to the first interest points
		for(int i = 0; i < interestPoints2.size(); i++)
		{
			//create a clone of the matchvec list
			List<Matches> tmpMatchVec = new Vector<Matches>(matchVec);
			//Collections.copy(tmpMatchVec, matchVec);
			
			//get the interest point which has to match
			InterestPoint ip = interestPoints2.get(i);
			
			//check if to have to match with the negative or positive node
			if(ip.negative)
				NearestNeighbor(rootNeg, ip, tmpMatchVec, i);
			else
				NearestNeighbor(rootPos, ip, tmpMatchVec, i);
			
			//now sqrt the results for perfomance reason it isn't done yet
			for(int j = 0; j < tmpMatchVec.size(); j++)
				tmpMatchVec.get(j).distance = (float) Math.sqrt(tmpMatchVec.get(j).distance);
			
			//add the match
			result.add(tmpMatchVec);
		}
		
		//return the result
		return result;
	}
	
	/**
	 * Method for find the n nearest neighbor of the given interest point
	 * @param node the current node
	 * @param destination the destination interest point which we want to match
	 * @param matchVec the match vector which holds the current
	 * @param index
	 */
	private void NearestNeighbor(Node node, InterestPoint destination, List<Matches> matchVec, int index)
	{
		//check if we are at the end of the tree
		if(node == null)
			return;
		
		//creat an matching
		//Matching match = new Matching();
		
		//compute the distance between the node point and the interest point
		float distance = Matching.GetEuclidianDistance(node.interestPoint, destination);
		
		//check if the distance is smaller then the current biggest distance
		if(distance < matchVec.get(matchVec.size() - 1).distance);
			for(int i = 0; i < matchVec.size(); i++)
				if(matchVec.get(i).distance > distance)
				{
					matchVec.add(i, new Matches(node.index, index, distance));
					matchVec.remove(matchVec.size() - 1);
					break;
				}

		//compute the difference between the current axis between nod point and destination point
		float diff = destination.descriptor[node.axis] - node.interestPoint.descriptor[node.axis];
		
		//the clos far points
		Node close, far;
		
		//find the close and far nodes of the current node
		close = (diff <= 0) ? node.rightChild : node.leftChild;
		far = (diff <= 0) ? node.leftChild : node.rightChild;
		
		//go on with the close neighbor
		NearestNeighbor(close, destination, matchVec, index);
		
		//check if it is necessary to go on with the far node
		if(diff * diff  < ((Vector<Matches>) matchVec).firstElement().distance)
			NearestNeighbor(far, destination, matchVec, index);
		
	}

	/**
	 * Method for creating the kd tree of array of interest points.
	 * @param interestPoints the interest points to create the kd tree
	 * @param depth the current depth
	 * @return the created node
	 */
	private Node ComputeKdTree(Vector<KDTreeContext> interestPoints, int depth) {
		
		//check if interespoints is empty so the kd tree is complete
		if (interestPoints.size() == 0)
			return null;

		//create the axis index
		int index = depth % interestPoints.get(0).interestPoint.descriptor.length;

		//sort the collect to the current index
		Collections.sort(interestPoints, new KDTreeContextComparator(index));

		//compute the median index
		int medianIndex = (int) (interestPoints.size() * 0.5f);
		
		//now get kd tree context (the current median interest point and his index)
		KDTreeContext kd = interestPoints.get(medianIndex);
		
		//create an new node
		return new Node(kd.interestPoint.descriptor[index], depth, kd.interestPoint,
				ComputeKdTree(new Vector<KDTreeContext>(interestPoints.subList(0, medianIndex)), depth + 1),
				ComputeKdTree(new Vector<KDTreeContext>(interestPoints.subList(medianIndex + 1, interestPoints.size())), depth + 1), kd.index);

	}
	
	/**
	 * The Kdtree context struct which hold the interest point and the index in the origin array.
	 * @author Peter Laudel
	 *
	 */
	private class KDTreeContext
	{
		InterestPoint interestPoint; //< the interest point
		int index; //the index of the interest point in the original list
		
		public KDTreeContext(InterestPoint ip, int index)
		{
			interestPoint = ip;
			this.index = index;
		}
	}
	
	/**
	 * Class for comparing kdtree context by the interest point and the current descriptor axis.
	 * @author Peter Laudel
	 *
	 */
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
