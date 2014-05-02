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
		
		ArrayList<InterestPoint> result = FindLocalMaximum();		
		int[] imagePixels = m_image.GetImagePixels();
		for(int i = 0; i < result.size();i++)
		{
			InterestPoint ip = result.get(i);
			int pos = ip.y * m_image.GetWidth() + ip.x;
			imagePixels[pos] = 0xFF000000 | (255<<16) | (0<<8) | 0;
		}
	}
	
	public Image GetOctaveImage(int octaveNumber, int octaveLayer)
	{
		if(octaveNumber >= m_octaveDepth || octaveLayer > 3)
			return null;
		
		return m_octaves[octaveNumber].GetOctaveImage(octaveLayer);
	}
   
   private ArrayList<InterestPoint> FindLocalMaximum()
   {
	   ArrayList<InterestPoint> result = new ArrayList<InterestPoint>();
	   int[] neighborhood = new int[] {1, 0 ,-1};
	   for(int i = 0; i < m_octaves.length; i++)
	   {
		   HarrisResponse[] octaveLayer = m_octaves[i].GetOctave();
		   for(int j = 1; j < octaveLayer.length - 1; j++)
		   {
			   HarrisResponse octaveLayerImage = octaveLayer[j];
			   for(int x = 1; x < octaveLayerImage.GetWidth() - 1; x++)
				   for(int y = 1; y < octaveLayerImage.GetHeight() - 1; y++)
				   {
					   boolean found = true;
					   failed:
					   for(int u = 0; u < neighborhood.length; u++)
						   for(int v = 0; v < neighborhood.length; v++)
							   for(int w = 0; w < neighborhood.length; w++)
							   {
								   int layer = j + neighborhood[w];
								   if(octaveLayerImage.GetResponse(x, y) <=  octaveLayer[layer].GetResponse((x + neighborhood[u]), (y + neighborhood[v]))
										   && !(neighborhood[u] == 0 && neighborhood[v] == 0 && neighborhood[w] == 0))
								   {
									   found = false;
									   break failed;
								   }
							   }
					   if(found)
						   result.add(new InterestPoint(x, y, octaveLayer[i].GetScale()));
				   }
		   } 
	   }
	   return result;
   }
   
   private  ArrayList<Integer>  FindLocalMaximum(Image image)
   {
	   ArrayList<Integer> list = new ArrayList<Integer>();
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
