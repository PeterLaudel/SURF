package app;

public interface Sorter {

	public void getFeatureVectors();
	public void sortBySimilarity();
	public void computeDistance(int queryPic, int actPic);
	
}
