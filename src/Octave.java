

public class Octave {
	
	private static final int PIXEL_SCALE = 6;
	public final static int DEFAULT_FILTER_SIZE = 9;
	
	private int m_octaveNumber;
	private Image[] m_octave;
	
	
	public Octave(int octaveNumber)
	{
		m_octaveNumber = octaveNumber;
		
	}
	
	
	void ComputeOctaves(IntegralImage integralImage)
	{
		int scalePixelSteps = m_octaveNumber * Octave.PIXEL_SCALE;
		int startFilterSize = Octave.DEFAULT_FILTER_SIZE + (m_octaveNumber - 1) * Octave.PIXEL_SCALE;
		
		//int min = Integer.MAX_VALUE;
		//int max = Integer.MIN_VALUE;
		m_octave = new Image[4];
		for(int i = 0; i < 4; i++)
		{
			m_octave[i] = new Image(integralImage.GetWidth(), integralImage.GetHeight());
			int[] octavePixels = m_octave[i].GetImagePixels();
			BoxFilter dxxBoxFilter = BoxFilter.GetSURFxxFilter(startFilterSize);
			BoxFilter dyyBoxFilter = BoxFilter.GetSURFyyFilter(startFilterSize);
			BoxFilter dxyBoxFilter = BoxFilter.GetSURFxyFilter(startFilterSize);
			
			for (int y = 0; y < integralImage.GetHeight(); y++) {
				
				for (int x = 0; x < integralImage.GetWidth(); x++) {
					int pos	= y * integralImage.GetWidth() + x;
					
					float Dxx =  (integralImage.ApplyBoxFilter(dxxBoxFilter, x, y));
					float Dyy =  (integralImage.ApplyBoxFilter(dyyBoxFilter, x, y));
					float Dxy =  (integralImage.ApplyBoxFilter(dxyBoxFilter, x, y));
					
					octavePixels[pos] = (int) Math.max(0, Dxx*Dyy - Math.pow(Dxy*0.9, 2));
					
				}
			}
			
			startFilterSize += scalePixelSteps;
		}
	}
	
	Image[] GetOctave()
	{
		if(m_octave != null)
			return m_octave;
		else
			return null;
	}
	
	Image GetMergedOctave()
	{
		if(m_octave == null)
			return null;
		
		Image result = new Image(m_octave[0].GetWidth(), m_octave[0].GetHeight() * 4);

		int[] resultPixels = result.GetImagePixels();
		for(int i = 0; i < m_octave.length; i++)
		{
			int[] pixels = m_octave[i].GetImagePixels();
			System.arraycopy(pixels, 0, resultPixels, i * pixels.length, pixels.length);
		}
		return result;
	}

}
