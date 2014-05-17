package SURF;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Vector;

import Features.InterestPoint;
import IntegralImage.BoxFilter;
import IntegralImage.IntegralImage;
import Process.Image;
import Process.Matrix;

public class SurfFeatureDescriptor {

	private Image m_image;
	

	public SurfFeatureDescriptor() {
		
	}

	public Image GetImage() {
		return m_image;
	}



	public void Compute(Image image, Vector<InterestPoint> interestPoints) {

		IntegralImage integralImage = new IntegralImage(image);

		CreateDescriptorWindow(interestPoints, integralImage);
	}
	
	public void Compute(IntegralImage integralImage, Vector<InterestPoint> interestPoints)
	{
		CreateDescriptorWindow(interestPoints, integralImage);
	}

	private void CreateDescriptorWindow(Vector<InterestPoint> interestPoints, IntegralImage integralImage) {

		for (int i = 0; i < interestPoints.size(); i++) {
			
			InterestPoint ip = interestPoints.get(i);
			// ip.x = 90;
			// ip.y = 90;
			float radius = ip.scale * 6;
			int Left = (int) Math.round(ip.x - radius);
			int Right = (int) Math.round(ip.x + radius);
			int Top = (int) Math.round(ip.y - radius);
			int Bottom = (int) Math.round(ip.y + radius);
			double radius2 = radius * radius;

			BoxFilter xWavelet = BoxFilter.GetWaveletX(ip.scale * 4);
			BoxFilter yWavelet = BoxFilter.GetWaveletY(ip.scale * 4);

			float xResponse = 0;
			float yResponse = 0;
			
			double[][] gaussian = Matrix.get2DGaussianKernel((int) Math.round(radius+1), 2 * ip.scale);

			ArrayList<Point2D.Float> angles = new ArrayList<Point2D.Float>();
			for (int j = Top; j <= Bottom; j+= ip.scale) {
				for (int k = Left; k <= Right; k+= ip.scale) {
					double dist = Math.pow(ip.x - k, 2.0)
							+ Math.pow(ip.y - j, 2.0);
					if (dist <= radius2) {
						int x = (int) Math.round(k);
						int y = (int) Math.round(j);

						xResponse = integralImage.ApplyBoxFilter(xWavelet, x,
								y);
						yResponse = integralImage.ApplyBoxFilter(yWavelet, x,
								y);

						int idxX = Math.abs(j - ip.y);
						int idxY = Math.abs(k - ip.x);
						xResponse *= gaussian[idxX][idxY];
						yResponse *= gaussian[idxX][idxY];

						if (xResponse != 0 || yResponse != 0)
							angles.add(new Point2D.Float(xResponse, yResponse));
						/*
						 * System.out.println("xResponse: " + xResponse +
						 * "yResponse: " + yResponse);
						 * 
						 * 
						 * float ang = (float) (Math.atan2(yResponse,
						 * xResponse)); if(j < 0 || k < 0 || j>=
						 * m_image.GetHeight() || k >= m_image.GetWidth())
						 * continue; int posTmp = j * m_image.GetWidth() + k;
						 * int response = (int) xResponse +128;
						 * imagePixels[posTmp] = 0xFF000000 | (response << 16) |
						 * (response << 8) | response;
						 */
					}
				}
			}

			// calculate the dominant direction
			float sumX = 0.f, sumY = 0.f;
			float max = 0.f, orientation = 0.f;
			float ang1 = 0.f, ang2 = 0.f;
			float pi2 = (float) (2 * Math.PI);
			float pi3 = (float) (Math.PI / 3.0);

			// loop slides pi/3 window around feature point
			for (ang1 = 0; ang1 < pi2; ang1 += 0.15f) {

				ang2 = ang1 + pi3;

				if (ang2 > pi2)
					ang2 -= (pi2);
				// ang2 = (float) (ang1 + Math.PI / 3.0f > Math.PI ? -(ang1
				// + Math.PI / 3.0f - Math.PI) : ang1 + Math.PI / 3.0f);
				sumX = sumY = 0.f;
				for (int k = 0; k < angles.size(); k++) {
					Point2D.Float angle = angles.get(k);
					// get angle from the x-axis of the sample point
					float ang = (float) (Math.atan2(angle.y, angle.x));

					if (ang < 0)
						ang += pi2;

					// determine whether the point is within the window
					if (ang1 < ang && ang2 > ang) {
						sumX += angle.x;
						sumY += angle.y;
					} else if (ang2 < ang1 && (0 > ang && ang < ang2)
							|| (ang1 < ang && ang < 2 * Math.PI)) {
						sumX += angle.x;
						sumY += angle.y;
					}
				}

				// if the vector produced from this window is longer than all
				// previous vectors then this forms the new dominant direction
				if (sumX * sumX + sumY * sumY > max) {
					// store largest orientation
					max = sumX * sumX + sumY * sumY;
					orientation = (float) (Math.atan2(sumY, sumX));
					if (orientation < 0)
						orientation += pi2;
				}
			}

			// assign orientation of the dominant response vector
			ip.orientation = orientation;
			CreateDescriptor(ip, integralImage);
		}
		
		
		
		
	}
	
	void CreateDescriptor(InterestPoint ip, IntegralImage integralImage)
	{	
		float halfSize = 20.0f* ip.scale;
		Point2D dirX = new Point2D.Float(1.0f, 0.0f);
		Point2D dirY = new Point2D.Float(0.0f, 1.0f);
		Point2D pos = new Point2D.Float(ip.x, ip.y);

		AffineTransform at = new AffineTransform();
		at.rotate(ip.orientation, 0.0f, 0.0f);
		at.transform(dirX, dirX);
		at.transform(dirY, dirY);
		
		BoxFilter xWavelet = BoxFilter.GetWaveletX(2 * ip.scale);
		BoxFilter yWavelet = BoxFilter.GetWaveletY(2 * ip.scale);
		
		
		double[][] gauss = Matrix.get2DGaussianKernel(40, 3.3f * ip.scale);
		
		for(float i = -1.0f; i <1.0f; i += 0.1f)
			for(float j = -1.0f; j < 1.0f; j += 0.1f)
			{
				float stepX= halfSize * i;
				float stepY= halfSize * j;
				
				AffineTransform atTmp = new AffineTransform();
				atTmp.translate(stepX * dirX.getX(), stepX * dirX.getY());
				atTmp.translate(stepY * dirY.getX(), stepY * dirY.getY());
				Point2D targetPos = new Point2D.Float();
				atTmp.transform(pos, targetPos);
				
				float xResponse = integralImage.ApplyBoxFilter(xWavelet, (int) Math.round(targetPos.getX()), (int) Math.round(targetPos.getY()));
				float yResponse = integralImage.ApplyBoxFilter(yWavelet, (int) Math.round(targetPos.getX()), (int) Math.round(targetPos.getY()));
				
				float normalizedI = ((i + 1.0f) / 2.0f);
				float normalizedJ = ((j + 1.0f) / 2.0f);
				int idx1 = (int) (normalizedI * 4.0f);
				int idx2 = (int) (normalizedJ * 4.0f);
				
				int idx = 4 * (idx1 + idx2 * 4);
				
				double gaussWeight = gauss[(int) (normalizedI * 40.0f)][(int) (normalizedJ * 40.0f)];
				xResponse *= gaussWeight;
				yResponse *= gaussWeight;
				
				ip.descriptor[idx] += xResponse;
				ip.descriptor[idx+1] += Math.abs(xResponse);
				ip.descriptor[idx+2] += yResponse;
				ip.descriptor[idx+3] += Math.abs(yResponse);
			}
		
		
		NormalizeVector(ip.descriptor);
	}
	
	private void NormalizeVector(float[] vector)
	{
		float tmp = 0;
		for(int i = 0; i < vector.length; i++)
			tmp += vector[i] * vector[i];
		
		float length = (float) Math.sqrt(tmp);
		for(int i = 0; i < vector.length; i++)
			vector[i] /= length;
			
		
	}

	/*
	 * private ArrayList<Integer> FindLocalMaximum(Image image) {
	 * ArrayList<Integer> list = new ArrayList<Integer>(); int n = 1; int step =
	 * 2*n +1;
	 * 
	 * for(int i = n; i < image.GetWidth()-n; i+=step) for(int j = n; j <
	 * image.GetHeight()-n; j+=step) { int mi = i; int mj = j;
	 * 
	 * for(int i2 = i; i2 < i + n; i2++) for(int j2 = j; j2 < j + n; j2++)
	 * if(image.GetPixel(i2, j2) > image.GetPixel(mi, mj)) { mi = i2; mj = j2; }
	 * boolean found = true; failed: for(int i2 = mi - n; i2 < mi + n; i2++)
	 * for(int j2 = mj - n; j2 < mj + n; j2++) if(image.GetPixel(i2, j2) >
	 * image.GetPixel(mi, mj)) { found = false; break failed; }
	 * 
	 * if(found) { int pos = mj * image.GetWidth() + mi; list.add(pos); } }
	 * 
	 * return list; }
	 */

}
