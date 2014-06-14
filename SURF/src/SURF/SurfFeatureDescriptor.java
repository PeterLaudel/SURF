package SURF;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import Features.InterestPoint;
import Imageprocess.Image;
import Imageprocess.Matrix;
import IntegralImage.BoxFilter;
import IntegralImage.IntegralImage;

/**
 * Class is for SURF descriptor extraction.
 * @author Peter Laudel
 * 
 * @version 1.0
 *
 */

public class SurfFeatureDescriptor {

	public SurfFeatureDescriptor() {
	}


	/**
	 * Compute the descriptors of the given interest points
	 * @param image [in] the given image
	 * @param interestPoints [in/out] the interest points where the descriptor part get filled
	 */
	public void Compute(Image image, List<InterestPoint> interestPoints) {

		//create the integral image
		IntegralImage integralImage = new IntegralImage(image);
		//compute the descriptor window
		CreateDescriptorWindow(interestPoints, integralImage);
		
		
	}
	
	/**
	 * Compute the descriptors of the given interest points
	 * @param integralImage [in] the given integral image of the researched image
	 * @param interestPoints [in/out] the interest points where the descriptor part get filled
	 */
	public void Compute(IntegralImage integralImage, List<InterestPoint> interestPoints)
	{
		//compute the descriptor window
		CreateDescriptorWindow(interestPoints, integralImage);
	}

	/**
	 * Method for creating the descriptor window. That means the size and the direction of the
	 * descriptor window gets computed by computing the haar wavelet response in a circle and
	 * weight it with gaussian. For more details look at the SURF Paper.
	 * @param interestPoints [in\out] the interest points where the values direction and descriptor get set
	 * @param integralImage [in] the integral image
	 */
	private void CreateDescriptorWindow(List<InterestPoint> interestPoints, IntegralImage integralImage) {
		//iterate over each interest point
		for (int i = 0; i < interestPoints.size(); i++) {
			//get the interest poit
			InterestPoint ip = interestPoints.get(i);
			
			//compute the circle radius for haar wavelet response
			float radius = ip.scale * 6;
			//now compute the search area as a rectangle
			int Left = (int) Math.round(ip.x - radius);
			int Right = (int) Math.round(ip.x + radius);
			int Top = (int) Math.round(ip.y - radius);
			int Bottom = (int) Math.round(ip.y + radius);
			
			//radius² for distance equaltion
			double radius2 = radius * radius;

			//get the box filter for x and y haar wavelet response
			BoxFilter xWavelet = BoxFilter.GetWaveletX(ip.scale * 4);
			BoxFilter yWavelet = BoxFilter.GetWaveletY(ip.scale * 4);

			//sum values floats
			float xResponse = 0;
			float yResponse = 0;
			
			//compute the gaussian matrix for weight the responses
			float[][] gaussian = Matrix.getGaussianKernel(2 * ip.scale);

			//angles array
			ArrayList<Point2D.Float> angles = new ArrayList<Point2D.Float>();
			for (int j = Top; j <= Bottom; j+= ip.scale) {
				for (int k = Left; k <= Right; k+= ip.scale) {
					//compute the distance from the circel center
					double dist = Math.pow(ip.x - k, 2.0)
							+ Math.pow(ip.y - j, 2.0);
					//check if point is inside the circle radius
					if (dist <= radius2) {
						//get the wavelet response for x and y
						xResponse = integralImage.ApplyBoxFilter(xWavelet, k,
								j);
						yResponse = integralImage.ApplyBoxFilter(yWavelet, k,
								j);

						//compute the index position in the 2x2 gaussian array
						int idxX = Math.abs(k - ip.x);
						int idxY = Math.abs(j - ip.y);
						
						//weight the response
						xResponse *= gaussian[idxX][idxY];
						yResponse *= gaussian[idxX][idxY];

						//add the "gradient" to the angles
						if (xResponse != 0 || yResponse != 0)
							angles.add(new Point2D.Float(xResponse, yResponse));
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
	
	/**
	 * Method for create the descriptor of the given feature point.
	 * @param ip [in\out] the current interest point
	 * @param integralImage [in] the current integral image
	 */
	private void CreateDescriptor(InterestPoint ip, IntegralImage integralImage)
	{	
		//the half size of the descriptor rect
		float halfSize = 10.0f* ip.scale;
		//the intial position
		Point2D dirX = new Point2D.Float(1.0f, 0.0f);
		Point2D dirY = new Point2D.Float(0.0f, 1.0f);
		//the positions
		Point2D pos = new Point2D.Float(ip.x, ip.y);

		//rotate the initial vectors for computing the rectangle direction
		AffineTransform at = new AffineTransform();
		at.rotate(ip.orientation, 0.0f, 0.0f);
		at.transform(dirX, dirX);
		at.transform(dirY, dirY);
		
		//create the gauss kernel for weights
		float[][] gauss = Matrix.getGaussianKernel(3.3f * ip.scale);
		
		Image image = integralImage.GetImage();
		
		//BoxFilter xWavelet = BoxFilter.GetWaveletX(2 * ip.scale);
		//BoxFilter yWavelet = BoxFilter.GetWaveletY(2 * ip.scale);
		
		//now go through the descriptor rectangle
		for(float i = -1.0f; i < 1.0f; i += 0.1f)
			for(float j = -1.0f; j < 1.0f; j += 0.1f)
			{
				//compute the step which has to be done
				float stepX= halfSize * i;
				float stepY= halfSize * j;
				
				//now add the translation which has to get done
				AffineTransform atTmp = new AffineTransform();
				atTmp.translate(stepX * dirX.getX(), stepX * dirX.getY()); // in x direction
				atTmp.translate(stepY * dirY.getX(), stepY * dirY.getY()); // in y direction
				//now shift from the center to the target pos
				Point2D targetPos = new Point2D.Float();
				atTmp.transform(pos, targetPos);
				
				//now compute the response
				float xResponse = 0, yResponse = 0;
				//we have to scan an area of an rectangle of 2 * ip.scal
				for(float x = -ip.scale; x < ip.scale; x++)
					for(float y = -ip.scale; y < ip.scale; y++)
					{
						//create new affine transformation
						atTmp = new AffineTransform();
						//compute the wavelet pos
						atTmp.translate(x * dirX.getX(), x * dirX.getY()); // in x direction
						atTmp.translate(y * dirY.getX(), y * dirY.getY()); // in y direction
						Point2D waveletPos = new Point2D.Float();
						//shift now from the targetpos to the waveletpos
						atTmp.transform(targetPos, waveletPos);
						
						//get the value if it is inside the picture
						float value = 0;
						if(waveletPos.getX() >= 0 && waveletPos.getX() < image.GetWidth() && waveletPos.getY() >= 0 && waveletPos.getY() < image.GetHeight())
							value = image.GetPixel((int) waveletPos.getX(), (int) waveletPos.getY());
						
						//add or subtract the value
						xResponse += (y < 0) ? -value : value;
						yResponse += (x < 0) ? -value : value;
					}
				
				//xResponse = integralImage.ApplyBoxFilter(xWavelet, (int) Math.round(targetPos.getX()), (int) Math.round(targetPos.getY()));
				//yResponse = integralImage.ApplyBoxFilter(yWavelet, (int) Math.round(targetPos.getX()), (int) Math.round(targetPos.getY()));
				 				
				
				//normalize the index and shift it over zero to a positive value
				float normalizedI = ((i + 1.0f) / 2.0f);
				float normalizedJ = ((j + 1.0f) / 2.0f);
				
				// now we have to compute the index position in the descriptor array
				// or the positin in the splitted descriptor rectangle (see paper)
				int idx1 = (int) (normalizedI * 4.0f);
				int idx2 = (int) (normalizedJ * 4.0f);
				
				//compute the position in the descriptor array
				int idx = 4 * (idx1 + idx2 * 4);
				
				//now find the weight and weight the response
				double gaussWeight = gauss[(int) (normalizedI * gauss.length)][(int) (normalizedJ * gauss.length)];
				xResponse *= gaussWeight;
				yResponse *= gaussWeight;
				
				//fill the descriptor vector
				ip.descriptor[idx] += xResponse;
				ip.descriptor[idx+1] += Math.abs(xResponse);
				ip.descriptor[idx+2] += yResponse;
				ip.descriptor[idx+3] += Math.abs(yResponse);
			}
		
		//normalize the descriptor vector to make it invariant against brightness differnce
		NormalizeVector(ip.descriptor);
	}
	
	/**
	 * Method for normalize the vector
	 * @param vector the descriptor array which get normalized
	 */
	private void NormalizeVector(float[] vector)
	{
		float tmp = 0;
		for(int i = 0; i < vector.length; i++)
			tmp += vector[i] * vector[i];
		
		float length = (float) Math.sqrt(tmp);
		for(int i = 0; i < vector.length; i++)
			vector[i] /= length;

	}
	

}
