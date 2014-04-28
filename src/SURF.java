import java.util.ArrayList;


public class SURF {
	
	private Image m_image;
	private IntegralImage m_integralImage;
	private int m_octaveDepth;
	private Octave[] m_octaves;

	public SURF(Image image, int octaveDepth) {
		m_image = image;
		m_octaveDepth = octaveDepth;
		m_octaves = new Octave[m_octaveDepth];
		
		for(int i = 0; i< m_octaves.length; i++)
			m_octaves[i] = new Octave(i);
		
		m_integralImage = new IntegralImage(m_image);
		
	}
	
	public Image GetImage()
	{
		return m_image;
	}
	
	public int GetOctaveDepth()
	{
		return m_octaveDepth;
	}
	
	public Octave[] GetOctaves()
	{
		return m_octaves;
	}
	
	public void Process()
	{
		
		for(int i = 0; i < m_octaves.length; i++)
		{
			m_octaves[i].ComputeOctaves(m_integralImage);
		}
	}
	
	public Image GetOctaveImage(int octaveNumber, int octaveLayer)
	{
		if(octaveNumber >= m_octaveDepth || octaveLayer > 3)
			return null;
		
		Image[] octaveImages = m_octaves[octaveNumber].GetOctave();
		return octaveImages[octaveLayer];
	}
	
   private ArrayList<Integer> findMaximum(Image[] octave)
    {
    	ArrayList<Integer> list = new ArrayList<Integer>();
    	int[] octavePixels = octave[1].GetImagePixels();
    	for (int y = 1; y < octave[1].GetHeight() - 1; y++) {
			for (int x = 1; x < octave[1].GetWidth() - 1; x++) {
				int pos	= y * octave[1].GetWidth() + x;
				int[] neighborhood = new int[] {1, 0 ,-1};
				
				boolean found = true;
				outerloop:
				for(int i = 0; i< neighborhood.length; i++)
					for(int j = 0; j <neighborhood.length; j++)
						for(int k = 0; k < neighborhood.length; k++)
						{
							int[] tmpOctavePixels = octave[1-neighborhood[k]].GetImagePixels();
							int tmpPos = (y - neighborhood[i]) * octave[1].GetWidth() + (x - neighborhood[j]);
							if(tmpOctavePixels[tmpPos] >=  octavePixels[pos] && (tmpPos != pos ))
							{
								found = false;
								break outerloop;
							}
						}
				
				if(found)
					list.add(pos);
					
			}
			
    	}
    	return list;
    }

}
