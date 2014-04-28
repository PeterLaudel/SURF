import java.awt.Point;
import java.util.Vector;


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
    	/*
    	for(int y = 0; y < height; y++)
    	{
    		String s = new String();
    		for(int x = 0; x < width; x++)
    		{
    			int pos	= y * width + x;
    			s += "" + dstPixels[pos]+ ", ";
    		}
    		System.out.println(s+ "\n");
    	}
    	*/
    	
    	
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
    
    int GetBoxIntegral(int col, int row, int cols, int rows)
    {
    	row = Math.min(row, m_integralImage.GetHeight());
        col = Math.min(col, m_integralImage.GetWidth());
        rows= Math.min(rows, m_integralImage.GetHeight()-1);
        cols = Math.min(cols, m_integralImage.GetWidth()-1);

        int A = 0, B = 0, C = 0, D = 0;
        if (row > 0 && col > 0) A = m_integralImage.GetPixel(col-1, row-1);
        if (row > 0 && cols > 0) B = m_integralImage.GetPixel(cols, row-1);
        if (rows > 0 && col > 0) C = m_integralImage.GetPixel(col-1, rows);
        if (rows > 0 && cols > 0) D = m_integralImage.GetPixel(cols, rows);

        return Math.max(0, A + D - B - C);
    }
    
    
    float ApplyBoxFilter(BoxFilter boxFilter, int x, int y)
    {
    	
    	float result = 0;
    	
    	Vector<Box> boxes = boxFilter.GetBoxes();
    	
    	for(int i = 0; i < boxes.size(); i++)
    	{
    		Box b = boxes.get(i);
    		Point pos = new Point(x, y);
    		Point upPoint = b.GetLeftUpperPoint();
    		Point bottomPoint = b.GetRightBottemPoint();
    		
    		result += GetBoxIntegral(pos.x + upPoint.x, pos.y + upPoint.y, pos.x + bottomPoint.x, pos.y + bottomPoint.y) * b.GetWeight();
    	}
    	
    	float totalWeight = (float) boxFilter.GetWeight();
    	
    	return result / totalWeight;
    }
    
    int GetWidth()
    {
    	return m_integralImage.GetWidth();
    }
    
    int GetHeight()
    {
    	return m_integralImage.GetHeight();
    }
}
