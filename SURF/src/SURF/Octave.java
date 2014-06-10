package SURF;
import java.util.HashMap;

import Imageprocess.HarrisResponse;
import Imageprocess.Image;
import IntegralImage.BoxFilter;
import IntegralImage.IntegralImage;

/**
 * Class for computing octaves.
 * @author Nilsson
 *
 *@version 1.0
 */

public class Octave {
	
	
	private static final int PIXEL_SCALE = 6; //< the pixel scale by the first octave in surf
	public final static int DEFAULT_FILTER_SIZE = 9; //< the default filter size
	
	private int m_octaveNumber; //<the current octave number
	private HarrisResponse[] m_octave; //< the resonse layers of the octave
	private int m_scalePixelSteps; //< the pixels steps by growing filter size
	private int m_startFilterSize; //< the initial filter size of the first response layer
	
	/**
	 * Constructor for Octave class.
	 * @param octaveNumber the current octave number
	 */
	public Octave(int octaveNumber)
	{
		
		m_octaveNumber = octaveNumber;
		//compute the pixel steps
		m_scalePixelSteps = Octave.PIXEL_SCALE;
		for(int i = 1; i <octaveNumber; i++)
			m_scalePixelSteps *=2;
	
		//compute the initial start filter size
		m_startFilterSize = m_scalePixelSteps + 3;
	}
	
	
	/**
	 * Method for creating the different harris response layers of the whole octave
	 * @param integralImage which is for harris respnse layer creation is necessary
	 * @param width the original image width
	 * @param height the original image height
	 * @param harrisMap a map with all actual computed response layers to determine double 
	 * harris response layer computation
	 */
	void ComputeOctaves(IntegralImage integralImage, int width, int height, HashMap<Integer, HarrisResponse> harrisMap)
	{
		//initial filter size an dfilter  grow size
		int scalePixelSteps = m_scalePixelSteps;
		int startFilterSize = m_startFilterSize;
		
		//int min = Integer.MAX_VALUE;
		//int max = Integer.MIN_VALUE;
		m_octave = new HarrisResponse[4];
		
		//create all harris response
		for(int i = 0; i < 4; i++)
		{
			//check if harris response layer was already computed in a other octave
			if(harrisMap.containsKey(startFilterSize))
			{
				//get this harris response layer
				m_octave[i] = harrisMap.get(startFilterSize);
				startFilterSize += scalePixelSteps;
				continue;
			}
			//otherwise compute now harris response layer
			m_octave[i] = new HarrisResponse(width, height, ((float) startFilterSize / (float) Octave.DEFAULT_FILTER_SIZE) * 1.2f, startFilterSize);
			
			//get the response pixels
			float[] octavePixels = m_octave[i].GetResponseArray();
			//create the box filter for xx yy and xy response
			BoxFilter dxxBoxFilter = BoxFilter.GetSURFxxFilter(startFilterSize);
			BoxFilter dyyBoxFilter = BoxFilter.GetSURFyyFilter(startFilterSize);
			BoxFilter dxyBoxFilter = BoxFilter.GetSURFxyFilter(startFilterSize);
			
			//copute the half size of the image to stay inside the image and don't shift over the border
			int halfSize = (int) (startFilterSize * 0.5f + 0.5f);
			//int halfSize = 0;
			//iterate over the the response area/image
			for (int y = halfSize; y < height - halfSize; y++) {
				for (int x = halfSize; x < width - halfSize; x++) {
					int pos	= y * width + x;
					
					//get the response of all single filters
					float Dxx =  (integralImage.ApplyBoxFilter(dxxBoxFilter, x, y));
					float Dyy =  (integralImage.ApplyBoxFilter(dyyBoxFilter, x, y));
					float Dxy =  (integralImage.ApplyBoxFilter(dxyBoxFilter, x, y));
					
					//now comute the response
					octavePixels[pos] = Dxx*Dyy - 0.9f * (Dxy * Dxy);
				}
			}
			harrisMap.put(startFilterSize, m_octave[i]);
			startFilterSize += scalePixelSteps;
		}
	}
	
	/**
	 * Getter Method for get th octave array
	 * @return the harris response ovtave array
	 */
	HarrisResponse[] GetOctave()
	{
		if(m_octave != null)
			return m_octave;
		else
			return null;
	}
	
	/**
	 * Get the current octave layer as an IMage
	 * @param octaveLayer the layer of the octave
	 * @return the image of the layer
	 */
	Image GetOctaveImage(int octaveLayer)
	{

		return m_octave[octaveLayer].GetImage();
	}
	
	/**
	 * Method for get a octave image with all octaves
	 * @return the octave image
	 */
	Image GetMergedOctave()
	{
		if(m_octave == null)
			return null;
		
		Image result = new Image(m_octave[0].GetWidth(), m_octave[0].GetHeight() * 4);

		int[] resultPixels = result.GetImagePixels();
		for(int i = 0; i < m_octave.length; i++)
		{
			int[] pixels = m_octave[i].GetImage().GetImagePixels();
			System.arraycopy(pixels, 0, resultPixels, i * pixels.length, pixels.length);
		}
		return result;
	}
	
	/**
	 * Getter method for getting the octave number
	 * @return the octave number of the octave
	 */
	public int GetOctaveNumber()
	{
		return m_octaveNumber;
	}
}
