package Sorter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import PicPropertys.Pic;
import app.Sorter;



public class Sorter_ColorMean implements Sorter {

	private Pic[] pics;

	public Sorter_ColorMean(Pic[] pics) {
		this.pics = pics;
		getFeatureVectors();
	}


	public void sortBySimilarity_alt() {
		int number = pics.length;

		double[] distance = new double[number];
		int[] index = new int[number];

		for (int n = 0; n < number; n++) {
			distance[n] = Double.MAX_VALUE;
			index[n] = -1;
		}

		for (int n = 0; n < number; n++) {
			if (pics[n] != null) {
				double errMin = Double.MAX_VALUE;
				for (int s = 0; s < number; s++) {
					if (pics[s] != null) {
						if (pics[s].isSelected) {
							double errS = getEuclidianDistance(pics[n], pics[s]);

							if (errS < errMin)
								errMin = errS;
						}
					}
				}

				// sort according to their distance
				for (int m = 0; m < number; m++) {
					if (distance[m] > errMin) {
						for (int k = number - 2; k >= m; k--) {
							distance[k + 1] = distance[k];
							index[k + 1] = index[k];
						}
						distance[m] = errMin;
						index[m] = n;
						break;
					}
				}
			}
		}

		for (int n = 0; n < number; n++) {
			if (pics[n] != null)
				pics[index[n]].rank = n;
		}
	}
	
	public void sortBySimilarity() {

		int number = pics.length;

		int q = -1;
		for (int i = 0; i < pics.length; i++) {
			if (pics[i] != null && pics[i].isSelected) {
				q = i;
				break;
			}
		}
		if (q == -1)
			return;
		
		DistComparator distComparator = new DistComparator();
		TreeSet<Pic> treeSet = new TreeSet<Pic>(distComparator);
		SortedSet<Pic> resultList = treeSet;


		Pic queryPic = pics[q];
		for (int n = 0; n < number; n++) {
			Pic actPic = pics[n]; 
			if (actPic != null) {

				double dist = getEuclidianDistance(actPic, queryPic);
				actPic.distance = dist;
				resultList.add(actPic);
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
			else if( d2 > d2 ) 
				return 1;
			else if ( ((Pic)p1).id == ((Pic) p2).id)
				return 0;
			else 
				return 1;
		}
	}

	private double getEuclidianDistance(Pic actPic, Pic searchPic) {

		double[] actFeatureData = actPic.featureVector;
		double[] searchFeatureData = searchPic.featureVector;

		double ra = actFeatureData[0];
		double ga = actFeatureData[1];
		double ba = actFeatureData[2];

		double rs = searchFeatureData[0];
		double gs = searchFeatureData[1];
		double bs = searchFeatureData[2];

		double d0 = ra - rs;
		double d1 = ga - gs;
		double d2 = ba - bs;

		double dist = d0 * d0 + d1 * d1 + d2 * d2;

		return dist;
	}

	public void getFeatureVectors() {

		for (int n = 0; n < pics.length; n++) {
			if (pics[n] != null) {
				BufferedImage bi = pics[n].bImage;

				int width = bi.getWidth();
				int height = bi.getHeight();

				int[] rgbValues = new int[width * height];

				bi.getRGB(0, 0, width, height, rgbValues, 0, width);

				double[] featureVector = new double[3];

				// loop over the block
				int r = 0;
				int g = 0;
				int b = 0;
				int sum = 0;

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;

						r += (rgbValues[pos] >> 16) & 255;
						g += (rgbValues[pos] >> 8) & 255;
						b += (rgbValues[pos]) & 255;

						sum++;
					}
				}

				// compute the mean color
				featureVector[0] = r / sum;
				featureVector[1] = g / sum;
				featureVector[2] = b / sum;

				pics[n].featureVector = featureVector;
				pics[n].featureImage = getFeatureImage(pics[n].featureVector);
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////
	// visualize the feature data as image
	//
	private BufferedImage getFeatureImage(double[] featureVector) {

		int w = 1;
		int h = 1;

		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();

		int[] pixels = new int[h * w];

		int r = (int) featureVector[0];
		int g = (int) featureVector[1];
		int b = (int) featureVector[2];

		pixels[0] = (0xFF << 24) | (r << 16) | (g << 8) | b;

		BufferedImage bThumb = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		bThumb.setRGB(0, 0, w, h, pixels, 0, w);

		big.drawImage(bThumb, 0, 0, w, h, null);
		big.dispose();
		return bi;
	}


	@Override
	public void computeDistance(int queryPic, int actPic) {
		// TODO Auto-generated method stub
		
	}

}
