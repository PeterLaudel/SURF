package Imageprocess;


public class HarrisResponse {
	
	
	private float[] m_response;
	private int m_imageWidth;
	private int m_imageHeight;
	private float m_scale;
	private int m_filterSize;
	
	
	public HarrisResponse(float[] pixels, int width, int height, float scale, int filterSize)
	{
		m_response = pixels;
		m_imageWidth = width;
		m_imageHeight = height;
		m_scale = scale;
		m_filterSize = filterSize;
	}
	
	public HarrisResponse(int width, int height, float scale, int filterSize)
	{
		m_response = new float[width * height];
		m_imageWidth = width;
		m_imageHeight = height;
		m_scale = scale;
		m_filterSize = filterSize;
	}
	
	public int GetFilterSize()
	{
		return m_filterSize;
	}
	
	public int GetWidth()
	{
		return m_imageWidth;
	}
	
	public int GetHeight()
	{
		return m_imageHeight;
	}
	
	public float GetScale()
	{
		return m_scale;
	}
	
	public float[] GetResponseArray()
	{
		return m_response;
	}
	
	public float GetResponse(int x, int y)
	{
		return m_response[y * m_imageWidth +x];
	}
	
	public void SetResponse(int x, int y , float response)
	{
		m_response[y * m_imageWidth  + x] = response;
	}
	
	public Image GetImage()
	{
		Image image =  new Image(m_imageWidth, m_imageHeight);
		int[] pixels = image.GetImagePixels();
		for(int i = 0; i < pixels.length; i++)
			pixels[i] = (int) Math.round(m_response[i]);
		
		ImageProcess.NormalizeImage(image);
		ImageProcess.CastToRGB(image);
		
		return image;
	}
}
