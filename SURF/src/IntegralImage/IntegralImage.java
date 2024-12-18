package IntegralImage;

import java.awt.Point;
import java.util.Vector;

import Imageprocess.Image;
import Imageprocess.SymmetrizationImage;

/**
 * Class which create an integral Image
 * @author Peter Laudel
 *
 * @version 1.0 
 */


public class IntegralImage {
	
	Image m_image; //< the original image
	Image m_integralImage; //< the computed integral image
	
	private int m_offset; //< the offset when it is not a SymmetrizationImage it is normally 1
	
	/**
	 * Constructor for integral image
	 * @param image where the integral image get computed
	 */
	public IntegralImage(Image image)
	{
		m_image = image;
		m_integralImage = ComputeIntegralImage(m_image);
		m_offset = 1;
	}
	
	/**
	 * Constructor for integral image by using a symmetrization image
	 * @param image here input the symmetrization image
	 */
	public IntegralImage(SymmetrizationImage image)
	{
		m_image = image;
		m_integralImage = ComputeIntegralImage(m_image);
		m_offset = image.GetOffset() + 1;
	}
	
	
	/**
	 * Method which compute the integral image
	 * @param srcImage the source image
	 * @return the computed integral image
	 */
	private Image ComputeIntegralImage(Image srcImage)
    {
		int height = srcImage.GetHeight() + 1;
		int width = srcImage.GetWidth() + 1;
		int[] srcPixels = srcImage.GetImagePixels();
		int[] dstPixels = new int[width * height];
    	for(int y = 1; y < height; y++)
    	{
    		for(int x = 1; x < width; x++)
    		{
    			
    			int pos	= y * width + x;
    			int pos2 = (y - 1) * (width - 1) + (x-1);
				int a = (y - 1) * width + (x - 1);
				int b = (y - 1) * width + x;
				int c = y * width + (x - 1);
				dstPixels[pos] = dstPixels[b] + dstPixels[c] + ((srcPixels[pos2]>>16)&0xFF) - dstPixels[a];
    		}
    	}
    	
    	return new Image(dstPixels, width, height);
    }
	 
    public Image GetDrawableIntegraleImage()
    {
    	
    	float min = (float) m_integralImage.GetPixel(1, 1);
    	float max = (float) m_integralImage.GetPixel(m_integralImage.GetWidth()-1, m_integralImage.GetHeight()-1);
    	Image resultImage = new Image(m_integralImage.GetWidth(), m_integralImage.GetHeight());
    	
    	for(int y = 1; y < m_integralImage.GetHeight(); y++)
    	{
    		for(int x = 1; x < m_integralImage.GetWidth(); x++)
    		{
				int value = (int)(((m_integralImage.GetPixel(x, y) - min) / max) * 255.0);	
				resultImage.SetPixel(x, y, 0xFF000000 | (value<<16) | (value<<8) | value);
    		}
    	}
    	return resultImage;
    }
    
    private int GetBoxIntegral(int xUp, int yUp, int xBot, int yBot)
    {
    	
    	yUp = Math.min(Math.max(0, yUp), m_integralImage.GetHeight() - 1); 
        xUp = Math.min(Math.max(0, xUp), m_integralImage.GetWidth() - 1);
        yBot = Math.min(Math.max(0, yBot), m_integralImage.GetHeight() - 1);
        xBot = Math.min(Math.max(0, xBot), m_integralImage.GetWidth() - 1);
        


        int A = 0, B = 0, C = 0, D = 0;
        A = m_integralImage.GetPixel(xUp, yUp);
        B = m_integralImage.GetPixel(xBot, yUp);
        C = m_integralImage.GetPixel(xUp, yBot);
        D = m_integralImage.GetPixel(xBot, yBot);

        return Math.max(0, A + D - B - C);
    }
    
    
    public float ApplyBoxFilter(BoxFilter boxFilter, int x, int y)
    {
    	
    	float result = 0;
    	
    	Vector<Box> boxes = boxFilter.GetBoxes();
    	
    	for(int i = 0; i < boxes.size(); i++)
    	{
    		Box b = boxes.get(i);
    		Point pos = new Point(x + m_offset, y + m_offset);
    		Point upPoint = b.GetLeftUpperPoint();
    		Point bottomPoint = b.GetRightBottemPoint();
    		
    		result += GetBoxIntegral(pos.x + upPoint.x, pos.y + upPoint.y, pos.x + bottomPoint.x, pos.y + bottomPoint.y) * b.GetWeight();
    	}
    	
    	return result / boxFilter.GetWeight();
    }
    

    public int GetWidth()
    {
    	return m_integralImage.GetWidth() - 1;
    }
    
    public int GetHeight()
    {
    	return m_integralImage.GetHeight() - 1;
    }
    
    public Image GetImage()
    {
    	return m_image;
    }
}
