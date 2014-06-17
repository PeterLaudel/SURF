package SURF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import Features.InterestPoint;
import Imageprocess.HarrisResponse;
import Imageprocess.Image;
import IntegralImage.IntegralImage;



/**
 * This is a class for the detection of SURF Feautures of an Image.
 * For detecting Feauture Points please call detect.
 * 
 * @author Peter Laudel
 * 
 * @version 1.0
 * 
 *
 *
 */
public class SurfFeatureDetector {
	
	
	private int m_number; //< number of to detectet features
	private Octave[] m_octaves; //< the octaves
	private int m_octaveDepth; //< the octave depth (3 or 4 is useful)
	private float m_max; //< the maximum detected response after detecting feature points
	
	/**
	 * Constructor for creating Surf Feature Detector.
	 * @param number of to detected features
	 * @param octaveDepth the octave depth (3 or 4 is useful)
	 */
	public SurfFeatureDetector(int number, int octaveDepth) {
		
		m_number = number;
		m_octaveDepth = octaveDepth;
		m_octaves = new Octave[m_octaveDepth];
		
		for (int i = 0; i < m_octaves.length; i++)
			m_octaves[i] = new Octave(i + 1);
		
		m_max = Float.MIN_VALUE;
	}
	
	/**
	 * Detect Method for SURF feauture detecting of the given image.
	 * @param image [in] for feature detecting
	 * @param interestPoints [out] the interest points array wich get filled 
	 */
	public void Detect(Image image, Vector<InterestPoint> interestPoints)
	{
		//hashmap for doubled computed harris response layers in the octave
		HashMap<Integer, HarrisResponse> harrisResponse= new HashMap<Integer, HarrisResponse>();
		
		//create the integral image
		IntegralImage integralImage = new IntegralImage(image);
		for (int i = 0; i < m_octaves.length; i++) {
			//compute the single octaves
			m_octaves[i].ComputeOctaves(integralImage, image.GetWidth(), image.GetHeight(), harrisResponse);
		}
		 //find the local maxima and optimize the interest points vector by memory
		Map<Float, InterestPoint> resultMap = new TreeMap<Float, InterestPoint>();
		
		FindLocalMaximum(resultMap);
		List<InterestPoint> result = (Vector<InterestPoint>) resultMap.values();
		interestPoints.addAll(result.subList(0, Math.max(result.size(), m_number)));
	}
	
	/**
	 * Detect Method for SURF feauture detecting of the given image.
	 * This Method is usefull for following feature descriptor extraction, 
	 * need only to compute on integral image.
	 * @param integralImage [in] for feature detecting
	 * @param interestPoints [out] the interest points array wich get filled 
	 */
	public void Detect(IntegralImage integralImage, int width, int height, List<InterestPoint> interestPoints)
	{
		//hashmap for doubled computed harris response layers in the octave
		HashMap<Integer, HarrisResponse> harrisResponse= new HashMap<Integer, HarrisResponse>();
		//compute the single octaves
		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(integralImage, width, height, harrisResponse);
		}
		
		 //find the local maxima and optimize the interest points vector by memory
		Map<Float, InterestPoint> resultMap = new TreeMap<Float, InterestPoint>();
		
		FindLocalMaximum(resultMap);
		List<InterestPoint> result = new Vector<InterestPoint>(resultMap.values());
		interestPoints.addAll(result.subList(Math.max(0, result.size()-m_number), result.size()));
	}
	
	/**
	 * Getter Method for the octave depth.
	 * @return the current octave depth.
	 */
	public int GetOctaveDepth() {
		return m_octaveDepth;
	}

	/**
	 * Getter for get the octave array
	 * @return the octave array. Could be empty if there wasn't a detecting yet
	 */
	public Octave[] GetOctaves() {
		return m_octaves;
	}
	
	/**
	 * Get the maximum computed response. Need it for thresholding.
	 * @return the highest found maximum
	 */
	public float GetMax()
	{
		return m_max;
	}
	
	/**
	 * Get the set number of to found feature points.
	 * @return number of feature points.
	 */
	public int GetNumber()
	{
		return m_number;
	}
	
	/**
	 * Method for local maxima estimation.
	 * @param interestPoints [out] vector of interest points.
	 */
	private void FindLocalMaximum(Map<Float, InterestPoint> interestPoints) {
		//vector for neighborhood translation
		int[] neighborhood = new int[] { 1, 0, -1 };
		//iterate ocer every octave
		for (int i = 0; i < m_octaves.length; i++) {
			//get the the response layers of the single octave
			HarrisResponse[] octaveLayer = m_octaves[i].GetOctave();
			//iterate in the middle layer of the octave to found local maxima with 3x3x3 neighborhood
			for (int j = 1; j < octaveLayer.length - 1; j++) {
				//get the current response layer
				HarrisResponse octaveLayerImage = octaveLayer[j];
				//compute the area offset (depents on the filter size is more or less the border handling)
				int searchAreaOffset = (int) (octaveLayer[j + 1].GetFilterSize() * 0.5f + 0.5f);
				//now search in the search area for a local maxima
				for (int x = searchAreaOffset; x < octaveLayerImage.GetWidth() - searchAreaOffset; x++)
					for (int y = searchAreaOffset; y < octaveLayerImage.GetHeight() - searchAreaOffset; y++) {
						boolean found = true;
						//get the response of the octave layer position
						float response = octaveLayerImage.GetResponse(x, y);
						float responseAbs = Math.abs(response);
						// now check the neighbors for a local maxima
						failed: 
						for (int u = 0; u < neighborhood.length; u++)
							for (int v = 0; v < neighborhood.length; v++)
								for (int w = 0; w < neighborhood.length; w++) {
									int layer = j + neighborhood[w];
									//if one of the neighbors response is higher then the current response leave the neighbor loops
									if (responseAbs <= Math.abs(octaveLayer[layer].GetResponse((x + neighborhood[u]), (y + neighborhood[v])))
										&& !(neighborhood[u] == 0 && neighborhood[v] == 0 && neighborhood[w] == 0)) {
										found = false;
										break failed;
									}
								}
						
						//if there was a local maxima add this as a interest points
						if (found)
						{
							interestPoints.put(Math.abs(response), new InterestPoint(x, y, octaveLayer[j]
									.GetScale(), response, (response < 0)));
							//check for maximum response
							m_max = Math.max(m_max, response);
							
						}
					}
			}
		}
	}
	
	//find local maxima after paper implementation not finished yet
	private  Vector<Integer>  FindLocalMaximum(Image image)
	{
		 Vector<Integer> list = new Vector<Integer>();
	 	   int n = 1;
	 	   int step = 2*n + 1;
	 	   
	 	   for(int i = n; i < image.GetWidth()-n; i =step)
	 		   for(int j = n; j < image.GetHeight()-n; j =step)
	 		   {
	 			   int mi = i;
	 			   int mj = j;
	 			   
	 			   for(int i2 = i; i2 < i + n; i2++  )
	 				   for(int j2 = j; j2 < j + n; j2++  )
	 					   if(image.GetPixel(i2, j2) > image.GetPixel(mi, mj))
	 					   {
	 						   mi = i2;
	 						   mj = j2;
	 					   }
	 			   boolean found = true;
	 			   failed:
	 			   for(int i2 = mi - n; i2 < mi + n; i2++  )
	 				   for(int j2 = mj - n; j2 < mj + n; j2++  )
	 					   if(image.GetPixel(i2, j2) > image.GetPixel(mi, mj))
	 					   {
	 						   found = false;
	 						   break failed;
	 					   }
	 			   
	 			  if(found)
	 			  {
	 				  int pos = mj * image.GetWidth() + mi;
	 				  list.add(pos);
	 			  }
	 		   }
	 	   
	   return list;
	}
	
	public Image GetOctaveImage(int octaveNumber, int octaveLayer) {
		if (octaveNumber >= m_octaveDepth || octaveLayer > 3)
			return null;

		return m_octaves[octaveNumber].GetOctaveImage(octaveLayer);
	}

}
