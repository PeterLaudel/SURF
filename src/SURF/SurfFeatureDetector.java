package SURF;

import java.util.Vector;

import Features.InterestPoint;
import IntegralImage.IntegralImage;
import Process.HarrisResponse;
import Process.Image;


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
		IntegralImage integralImage = new IntegralImage(image);
		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(integralImage, image.GetWidth(), image.GetHeight());
		}
		
		FindLocalMaximum(interestPoints);
		interestPoints.trimToSize();
	}
	
	public void Detect(IntegralImage integralImage, int width, int height, Vector<InterestPoint> interestPoints)
	{
		for (int i = 0; i < m_octaves.length; i++) {
			m_octaves[i].ComputeOctaves(integralImage, width, height);
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
				for (int x = 1; x < octaveLayerImage.GetWidth() - 1; x++)
					for (int y = 1; y < octaveLayerImage.GetHeight() - 1; y++) {
						boolean found = true;
						float response = octaveLayerImage.GetResponse(x, y);
						failed: for (int u = 0; u < neighborhood.length; u++)
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
	
	public Image GetOctaveImage(int octaveNumber, int octaveLayer) {
		if (octaveNumber >= m_octaveDepth || octaveLayer > 3)
			return null;

		return m_octaves[octaveNumber].GetOctaveImage(octaveLayer);
	}

}
