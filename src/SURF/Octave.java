package SURF;
import java.util.HashMap;

import Imageprocess.HarrisResponse;
import Imageprocess.Image;
import IntegralImage.BoxFilter;
import IntegralImage.IntegralImage;



public class Octave {
	
	private static final int PIXEL_SCALE = 6;
	public final static int DEFAULT_FILTER_SIZE = 9;
	
	private int m_octaveNumber;
	private HarrisResponse[] m_octave;
	private int m_scalePixelSteps;
	private int m_startFilterSize;
	
	
	public Octave(int octaveNumber)
	{
		m_octaveNumber = octaveNumber;
		
		m_scalePixelSteps = Octave.PIXEL_SCALE;
		for(int i = 1; i <octaveNumber; i++)
			m_scalePixelSteps *=2;
		
		m_startFilterSize = m_scalePixelSteps + 3;
	}
	
	void ComputeOctaves(IntegralImage integralImage, int width, int height, HashMap<Integer, HarrisResponse> harrisMap)
	{
		int scalePixelSteps = m_scalePixelSteps;
		int startFilterSize = m_startFilterSize;
		
		//int min = Integer.MAX_VALUE;
		//int max = Integer.MIN_VALUE;
		m_octave = new HarrisResponse[4];
		for(int i = 0; i < 4; i++)
		{
			if(harrisMap.containsKey(startFilterSize))
			{
				m_octave[i] = harrisMap.get(startFilterSize);
				startFilterSize += scalePixelSteps;
				continue;
			}
			m_octave[i] = new HarrisResponse(width, height, ((float) startFilterSize / (float) Octave.DEFAULT_FILTER_SIZE) * 1.2f);
			float[] octavePixels = m_octave[i].GetResponseArray();
			BoxFilter dxxBoxFilter = BoxFilter.GetSURFxxFilter(startFilterSize);
			BoxFilter dyyBoxFilter = BoxFilter.GetSURFyyFilter(startFilterSize);
			BoxFilter dxyBoxFilter = BoxFilter.GetSURFxyFilter(startFilterSize);
			int halfSize = (int) (startFilterSize * 0.5f + 0.5f);
			//int halfSize = 0;
			for (int y = halfSize; y < height - halfSize; y++) {
				
				for (int x = halfSize; x < width - halfSize; x++) {
					int pos	= y * width + x;
					
					float Dxx =  (integralImage.ApplyBoxFilter(dxxBoxFilter, x, y));
					float Dyy =  (integralImage.ApplyBoxFilter(dyyBoxFilter, x, y));
					float Dxy =  (integralImage.ApplyBoxFilter(dxyBoxFilter, x, y));
					
					octavePixels[pos] = (float) Math.abs(Dxx*Dyy - 0.9f * (Dxy * Dxy));
				}
			}
			harrisMap.put(startFilterSize, m_octave[i]);
			startFilterSize += scalePixelSteps;
		}
	}
	
	HarrisResponse[] GetOctave()
	{
		if(m_octave != null)
			return m_octave;
		else
			return null;
	}
	
	Image GetOctaveImage(int octaveLayer)
	{

		return m_octave[octaveLayer].GetImage();
	}
	
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
	
	public int GetOctaveNumber()
	{
		return m_octaveNumber;
	}
	

}
