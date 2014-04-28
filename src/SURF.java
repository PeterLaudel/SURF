import java.util.ArrayList;
import java.util.Collection;


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
			m_octaves[i] = new Octave(i+1);
		
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
		
		ArrayList<Integer> result = FindLocalMaximum();		
		int[] imagePixels = m_image.GetImagePixels();
		for(int i = 0; i < result.size();i++)
			imagePixels[result.get(i)] = 0xFF000000 | (255<<16) | (0<<8) | 0;
	}
	
	public Image GetOctaveImage(int octaveNumber, int octaveLayer)
	{
		if(octaveNumber >= m_octaveDepth || octaveLayer > 3)
			return null;
		
		return m_octaves[octaveNumber].GetOctaveImage(octaveLayer);
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
   
   private ArrayList<Integer> FindLocalMaximum()
   {
	   ArrayList<Integer> result = new ArrayList<Integer>();
	   int[] neighborhood = new int[] {1, 0 ,-1};
	   for(int i = 0; i < m_octaves.length; i++)
	   {
		   Image[] octaveLayer = m_octaves[i].GetOctave();
		   for(int j = 1; j < octaveLayer.length - 1; j++)
		   {
			   Image octaveLayerImage = octaveLayer[j];
			   
			   
			   for(int x = 1; x < octaveLayerImage.GetWidth() - 1; x++)
				   for(int y = 1; y < octaveLayerImage.GetHeight() - 1; y++)
				   {
					   boolean found = true;
					   int pos = y * octaveLayerImage.GetWidth() + x;
					   failed:
					   for(int u = 0; u < neighborhood.length; u++)
						   for(int v = 0; v < neighborhood.length; v++)
							   for(int w = 0; w < neighborhood.length; w++)
							   {
								   int layer = j + neighborhood[w];
								   if(octaveLayerImage.GetPixel(x, y) <= octaveLayer[layer].GetPixel((x + neighborhood[u]), (y + neighborhood[v]))
										   && !(neighborhood[u] == 0 && neighborhood[v] == 0 && neighborhood[w] == 0))
								   {
									   found = false;
									   break failed;
								   }
							   }
					   if(found)
					    result.add(pos);
				   }
		   } 
	   }
	   return result;
   }
   
   private  ArrayList<Integer>  FindLocalMaximum(Image image)
   {
	   ArrayList<Integer> list = new ArrayList<Integer>();
	   int[] pixels = image.GetImagePixels();
	   int n = 1;
	   int step = 2*n +1;
	   
	   for(int i = n; i < image.GetWidth()-n; i+=step)
		   for(int j = n; j < image.GetHeight()-n; j+=step)
		   {
			   int mi = i;
			   int mj = j;
			   
			   for(int i2 = i; i2 < i + n; i2++)
				   for(int j2 = j; j2 < j + n; j2++)
					   if(image.GetPixel(i2, j2) > image.GetPixel(mi, mj))
					   {
						   mi = i2;
						   mj = j2;
					   }
			   boolean found = true;
			   failed:
			   for(int i2 = mi - n; i2 < mi + n; i2++)
				   for(int j2 = mj - n; j2 < mj + n; j2++)
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

}
