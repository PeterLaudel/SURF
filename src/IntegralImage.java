
public class IntegralImage {
	
	Image m_image;
	Image m_integralImage;
	
	public IntegralImage(Image image)
	{
		m_image = image;
		m_integralImage = ComputeIntegralImage(m_image);
	}
	
	private Image ComputeIntegralImage(Image srcImage)
    {
		int height = srcImage.GetHeight();
		int width = srcImage.GetWidth();
		int[] srcPixels = srcImage.GetImagePixels();
		int[] dstPixels = new int[width * height];
    	for(int y = 0; y < height; y++)
    	{
    		for(int x = 0; x < width; x++)
    		{
    			
    			int pos	= y * width + x;
    			if(x != 0 && y != 0)
    			{
    				int a = (y - 1) * width + (x - 1);
    				int b = (y - 1) * width + x;
    				int c = y * width + (x - 1);
    				dstPixels[pos] = dstPixels[b] + dstPixels[c] + ((srcPixels[pos]>>16)&0xFF) - dstPixels[a];
    				continue;
    			}
    			else if(x == 0 && y != 0)
    			{
    				int pos2 = (y - 1) * width + x;
    				dstPixels[pos] = dstPixels[pos2] + ((srcPixels[pos]>>16)&0xFF);
    				continue;
    			}
    			else if(y == 0 && x != 0)
    			{
    				int pos2 = y * width + (x - 1);
    				dstPixels[pos] = dstPixels[pos2] + ((srcPixels[pos]>>16)&0xFF);
    				continue;
    			} 
    			else if (pos == 0)
    			{
    				dstPixels[0] = ((srcPixels[0]>>8)&0xFF);
    				continue;
    			}
    		}
    	}
    	
    	return new Image(dstPixels, width, height);
    }
	 
    Image GetDrawableIntegraleImage()
    {
    	
    	float min = (float) m_integralImage.GetPixel(0, 0);
    	float max = (float) m_integralImage.GetPixel(m_integralImage.GetWidth()-1, m_integralImage.GetHeight()-1);
    	Image resultImage = new Image(m_integralImage.GetWidth(), m_integralImage.GetHeight());
    	
    	for(int y = 0; y < m_integralImage.GetHeight(); y++)
    	{
    		for(int x = 0; x < m_integralImage.GetWidth(); x++)
    		{
				int value = (int)(((m_integralImage.GetPixel(x, y) - min) / max) * 255.0);	
				resultImage.SetPixel(x, y, 0xFF000000 | (value<<16) | (value<<8) | value);
    		}
    	}
    	return resultImage;
    }
}
