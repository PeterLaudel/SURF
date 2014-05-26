package SURF;

import java.util.HashMap;
import java.util.Vector;

import Features.InterestPoint;
import Imageprocess.HarrisResponse;
import Imageprocess.Image;
import IntegralImage.IntegralImage;


public class SurfFeatureDetector {
	
	private int m_number;
	private Octave[] m_octaves;
	private int m_octaveDepth;
	private float m_max;
	

	public SurfFeatureDetector(int number, int octaveDepth) {
		// TODO Auto-generated constructor stub
		
		m_number = number;
		m_octaveDepth = octaveDepth;
		m_octaves = new Octave[m_octaveDepth];
		
		for (int i = 0; i < m_octaves.length; i++)
			m_octaves[i] = new Octave(i + 1);
		
		m_max = Float.MIN_VALUE;
	}
	
	public void Detect(Image image, Vector<InterestPoint> interestPoints)
	{
		HashMap<Integer, HarrisResponse> harrisResponse= new HashMap<Integer, HarrisResponse>();
		IntegralImage integralImage = new IntegralImage(image);
		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(integralImage, image.GetWidth(), image.GetHeight(), harrisResponse);
		}
		
		FindLocalMaximum(interestPoints);
		interestPoints.trimToSize();
	}
	
	public void Detect(IntegralImage integralImage, int width, int height, Vector<InterestPoint> interestPoints)
	{
		HashMap<Integer, HarrisResponse> harrisResponse= new HashMap<Integer, HarrisResponse>();
		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(integralImage, width, height, harrisResponse);
		}
		
		FindLocalMaximum(interestPoints);
		interestPoints.trimToSize();
	}
	
	public int GetOctaveDepth() {
		return m_octaveDepth;
	}

	public Octave[] GetOctaves() {
		return m_octaves;
	}
	
	public float GetMax()
	{
		return m_max;
	}
	
	public int GetNumber()
	{
		return m_number;
	}
	
	private void FindLocalMaximum(Vector<InterestPoint> interestPoints) {
		int[] neighborhood = new int[] { 1, 0, -1 };
		for (int i = 0; i < m_octaves.length; i++) {
			HarrisResponse[] octaveLayer = m_octaves[i].GetOctave();
			for (int j = 1; j < octaveLayer.length - 1; j++) {
				HarrisResponse octaveLayerImage = octaveLayer[j];
				int searchAreaOffset = (int) (octaveLayer[j + 1].GetFilterSize() * 0.5f + 0.5f);
				for (int x = searchAreaOffset; x < octaveLayerImage.GetWidth() - searchAreaOffset; x++)
					for (int y = searchAreaOffset; y < octaveLayerImage.GetHeight() - searchAreaOffset; y++) {
						boolean found = true;
						float response = octaveLayerImage.GetResponse(x, y);
						failed: 
						for (int u = 0; u < neighborhood.length; u++)
							for (int v = 0; v < neighborhood.length; v++)
								for (int w = 0; w < neighborhood.length; w++) {
									int layer = j + neighborhood[w];
									if (response <= octaveLayer[layer].GetResponse((x + neighborhood[u]), (y + neighborhood[v]))
										&& !(neighborhood[u] == 0 && neighborhood[v] == 0 && neighborhood[w] == 0)) {
										found = false;
										break failed;
									}
								}
						if (found)
						{
							interestPoints.add(new InterestPoint(x, y, octaveLayer[j]
									.GetScale(), response));
							m_max = Math.max(m_max, response);
							
						}
					}
			}
		}
	}
	private  Vector<Integer>  FindLocalMaximum(Image image)
	{
		 Vector<Integer> list = new Vector<Integer>();
	 	   int[] pixels = image.GetImagePixels();
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
