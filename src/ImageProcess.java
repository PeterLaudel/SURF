public class ImageProcess {
	
	
	/**
	 * @brief Static Method for applie a kernel to the income image 
	 * 
	 * @param image where mask gets applied
	 * @param divisor sum of the mask get divided by this divisor
	 * @param offset shift size of the result value
	 * @param mask the applied mask
	 * @param pure boolean for checking if result gets written pure in the pixel or as gray color 
	 * @return
	 */
    static void applyMask(Image image, int divisor, int offset, double[][] mask, boolean pure)
    {
    	int[] dstPixels = image.GetImagePixels();
    	//calculate the middle value of the array for example
    	int xNegativeValue = (int) (mask.length / 2.0);
    	int yNegativeValue = (int) (mask[0].length / 2.0);
    	
    	//iterate over the image
    	for(int y = 0; y < image.GetHeight(); y++)
		{
	    	for(int x = 0; x < image.GetWidth(); x++)	
	    	{
	    		
    			//calculate the current position -> the center of the mask
    			int realPos = y * image.GetWidth() + x;
    			
    			//go from -index over 0 to +index
    			int value = 0;
    			for(int i = -yNegativeValue; i<= yNegativeValue; i++)	
    			{
    				for(int j = -xNegativeValue; j <= xNegativeValue; j++)
    				{
    					//compute the current position (the neighbor of considered pixel)
    					int xIndex = x + i;
    					int yIndex = y + j;
    					
    					//out of bounds check
    					if(yIndex >= image.GetHeight() || yIndex < 0)
    			    		continue;
    			    	if(xIndex >= image.GetWidth() || xIndex < 0)
    			    		continue;

    			    	//compute the position
						int pos = yIndex * image.GetWidth() + xIndex;
						
						//get the gray value
						int gray = image.GetImagePixels()[pos] & 0xFF;
						
						//accumulate the result to the value
						value += (int) ((gray * mask[j + xNegativeValue][i + yNegativeValue]));
    				}
    			}
    			//normalize the value
    			value = (int) (value / divisor) + offset;
    			
    			//set as gray value in the new image
    			//dstPixels[realPos] = 0xFF000000 | (value<<16) | (value<<8) | value;
    			if(pure)
    				dstPixels[realPos] = value;
    			else
    				dstPixels[realPos] = 0xFF000000 | (value<<16) | (value<<8) | value;
    		}
    	}
    }
    
    /**
     * @brief Function binarize an image by computing the best "middle" position
     * in the histogram and sets the pixels with value on the left side to zero
     * and on the right side to one 
     * @param image which get binarized
     */
    static void isoDataAlgorithm(Image image) 
    {
    	//create a histogram array with buckets
    	//in java it get filled by zeros by the initialization
    	int[] histogram = new int[256];
    	
    	//get the pixels
    	int[] pixels = image.GetImagePixels();
    	int mean = 0;
    	
    	//iterate over all pixels and update the histogram and calculate the mean
    	for(int i = 0; i < pixels.length; i++) {
    		int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
    		mean += gray;
    		histogram[gray] += 1;
    	}
    	// set the t to the mean value to prevent the outlier of t and so a wrong decide valie
    	int t = (int) ((double) mean / (double) pixels.length);
    	
    	//initialize variables
    	double sumLeft = 0, sumRight = 0, countLeft = 0, countRight = 0;
    	int tnew = 0;
    	
    	//initialize count value to prevent infinity loop normaly the loop should break before
    	int count = 0;
    	while(count < histogram.length)
    	{
    		//now we iterate over the histogram and count the values of each side of t
			for(int i = 0; i < histogram.length; i++)
			{
				if(i <  t)
				{
					sumLeft += i * histogram[i];
					countLeft += histogram[i];
				}
				else
				{
					sumRight += i * histogram[i];
					countRight += histogram[i];
				}
			}
			//calculate a new t
			tnew = (int) ((((1.0 / countLeft) * sumLeft) + ((1.0 / countRight) * sumRight)) / 2.0);
			//if the tnew is the same lik t we can brak up we ar in the center of the values in the hisogram
			if(tnew == t)
				break;
			else
				t = tnew; //else repeat procedure
			
			count++;
    	}
    	
    	//now iterate over the image and again and decide if the pixel gets 1 (white) or a 0 (black) 
    	for(int i = 0; i < pixels.length; i++)
    	{
    		int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
    		pixels[i] = gray < t ? 0xff000000 : 0xffffffff;
    	}
    }
    
    /**
     * @brief Method creates an image (accumulator Array) where the maximas
     * represent the most frequently line positions 
     * @param image black-white image for line detection
     * @return an image (accumulator array) with sinus vibrations 
     */
    static Image accumulatorImageForLineDetection(Image image)
    {
    	//initialize propertys
    	int height = image.GetHeight();
    	int width = image.GetWidth();
    	int[] srcPixels = image.GetImagePixels();
    	
    	//the arrayHeight is the diagonal of the image
    	int arrayHeight = (int) (Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2)) + 1); 
    	int arrayWidth = 180; //size of 180 degrees could be other value
    	int maxRadius = (int) (arrayHeight * 0.5); //the maximum radius which is possible
    	
    	//initialize the akkumulatorArray 
    	int []akkumulatorArray = new int[arrayHeight * arrayWidth];
    	
    	//compute the half height and width to center the position
    	double halfHeight = height / 2.0;
    	double halfWidth = width /2.0;
    	
    	//remember the maximal value in the akkumulator array for normalize later
    	int maxValue = 0;
    	for (int y = 0; y < height; y++)
    	{
			for (int x = 0; x < width; x++) 
			{
				//compute the position
				int pos	= y * width + x;
				//if the pixel is white it is a potential line
				if(srcPixels[pos] == 0xffffffff)
				{
					//center the coordinate
					double coordX = x - halfWidth;
					double coordY = y - halfHeight;
					
					//iterate over all angle to create the Sinus curve in the accumulator array 
					for(int angle = 0; angle < arrayWidth; angle++)
					{
						//calculate the angle between the 0 and 180 degrees this is for a bigger array
						double newAngle = angle / (double) arrayWidth * 180.0;
						double rad = newAngle / 180.0 * Math.PI; // cast to radians
						//compute the radius
						int r = (int) Math.round(coordX * Math.cos(rad) + coordY * Math.sin(rad));
						
						//compute the position in the accumulator array
						int akkuPos = (r + maxRadius) * arrayWidth + angle;
						//accumulate
						akkumulatorArray[akkuPos]++;
						
						//remember the max value
						if(akkumulatorArray[akkuPos] > maxValue)
							maxValue = akkumulatorArray[akkuPos];
					}
				}
			}
			
    	}
    	
    	//now normalize the array
    	for (int y = 0; y < arrayHeight; y++)
    	{
			for (int x = 0; x < arrayWidth; x++) 
			{
				int pos	= y * arrayWidth + x;
				int gray = (int) (akkumulatorArray[pos] / (double) maxValue * 255.0);
				akkumulatorArray[pos] = 0xFF000000 | (gray<<16) | (gray<<8) | gray;
			}
    	}
    	
    	//return a new image
    	return new Image(akkumulatorArray, arrayWidth, arrayHeight);
	}
    
    static Image CastToRGB(Image image)
    {
    	Image result = new Image(image.GetWidth(), image.GetHeight());
    	int[] resultPixels = result.GetImagePixels();
    	for(int x = 0; x < image.GetWidth(); x++)
    		for(int y = 0; y < image.GetHeight(); y++)
    		{
    			int pos	= y * image.GetWidth() + x;
    			int value = image.GetImagePixels()[pos];
    			resultPixels[pos] = 0xFF000000 | (value<<16) | (value<<8) | value;
    		}
    	return result;
    }
}
