package Imageprocess;

/**
 * Class for creating an Image which is mirrored at his borders.
 * @author Nilsson
 *
 * @version 1.0
 */

public class SymmetrizationImage extends Image{

	private int m_offset; //< size how much get mirrored at the border of the image

	/**
	 * Constructer which create a symmetrization Image by initialization. 
	 * @param pixels which get mirrored
	 * @param width the original image width
	 * @param height the original image height
	 * @param offset size how much get mirrored at the border of the image
	 */
	public SymmetrizationImage(int[] pixels, int width, int height, int offset) 
	{
		super(pixels, width, height);
		
		if(offset > height && height < width)
			m_offset = height;
		else if (offset > width)
			m_offset = width;
		else
			m_offset = offset;
		m_imageHeight = height + (offset * 2);
		m_imageWidth = width + (offset * 2);
		m_imagePixels = new int[m_imageHeight * m_imageWidth];
		ComputeSymmetrizationImage(pixels, width, height);
	}
	
	/**
	 * Method for computing the mirrored border
	 * @param pixels Image pixel which get mirrored
	 * @param width the original image width
	 * @param height the original image height
	 */
	private void ComputeSymmetrizationImage(int[] pixels, int width, int height)
	{
		int sizeX = width + m_offset;
		int sizeY = height + m_offset;
		
		for(int x = -m_offset; x < sizeX; x++)
			for(int y = -m_offset; y < sizeY; y++)
			{
				int originX = (x >= width ? width - (x - width) - 1 : x) % width;
				int originY = (y >= height ? height - (y - height) - 1 : y) % height;
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
