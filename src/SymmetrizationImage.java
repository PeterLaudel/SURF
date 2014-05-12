
public class SymmetrizationImage extends Image{
	
	
	
	private int m_offset;

	public SymmetrizationImage(int[] pixels, int width, int height, int offset) 
	{
		super(pixels, width, height);
		
		m_offset = offset;
		m_imageHeight = height + (offset * 2);
		m_imageWidth = width + (offset * 2);
		m_imagePixels = new int[m_imageHeight * m_imageWidth];
		ComputeSymmetrizationImage(pixels, width, height);
	}
	
	private void ComputeSymmetrizationImage(int[] pixels, int width, int height)
	{
		int sizeX = width + m_offset;
		int sizeY = height + m_offset;
		
		for(int x = -m_offset; x < sizeX; x++)
			for(int y = -m_offset; y < sizeY; y++)
			{
				int originX = (x >= width ? width - (x - width) - 1 : x);
				int originY = (y >= height ? height - (y - height) - 1 : y);
				int originPos = (Math.abs(originY)) * width + (Math.abs(originX));
				int pos = (x + m_offset) + m_imageWidth * (y + m_offset);
				m_imagePixels[pos] = pixels[originPos];
			}
	}
	
	public int GetOffset()
	{
		return m_offset;
	}
	

}
