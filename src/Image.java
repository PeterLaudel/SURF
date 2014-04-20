
public class Image {
	
	enum Channels
	{
		RGBA,
		Gray
	}
	
	private int[] m_imagePixels;
	private int m_imageWidth;
	private int m_imageHeight;
	
	
	Image(int[] pixels, int width, int height)
	{
		m_imagePixels = pixels;
		m_imageWidth = width;
		m_imageHeight = height;
	}
	
	Image(int width, int height)
	{
		m_imagePixels = new int[width * height];
		m_imageWidth = width;
		m_imageHeight = height;
	}
	
	public int GetWidth()
	{
		return m_imageWidth;
	}
	
	public int GetHeight()
	{
		return m_imageHeight;
	}
	
	public int[] GetImagePixels()
	{
		return m_imagePixels;
	}
	
	public int GetPixel(int x, int y)
	{
		return m_imagePixels[y * m_imageWidth +x];
	}
	
	public void SetPixel(int x, int y , int pixel)
	{
		m_imagePixels[y * m_imageWidth  + x] = pixel;
	}
}
