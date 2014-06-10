package Imageprocess;

import java.util.Vector;

public class Matrix {
	
	/**
	 * Method creates an 2D Gaussian kernel by the input values
	 * 
	 * @param length of the kernel
	 * @param sigma varianz of the kernel
	 * @return 2D gaussian kernel
	 */
    public static double[][] get2DGaussianKernel(int length, double sigma)
    {
    	double[][] kernel = new double[length][length]; //create the kernel
    	
    	//calculate the half length for 
    	int half = (int) ((length - 1) / 2.0);
    	
    	//c
    	double calculatedEuler = 1.0 / (2.0 * Math.PI * (sigma * sigma)); 
    	
    	
    	//iterate over the kernel and calculate the single values depending on the positions
    	double sum = 0;
    	for(int x = -half; x <= half; x++)
    		for(int y = -half; y <= half; y++)
    		{
    			double distance = -(((x * x) + (y * y)) / (2.0 * sigma * sigma));
    			kernel[x + half][y + half] = calculatedEuler * Math.exp(distance);
    			sum += kernel[x + half][y + half];
    		}
    	
    	//normalize the kernel
    	for(int x = 0; x < kernel.length; x++)
    		for(int y = 0; y < kernel[x].length; y++)
    			kernel[x][y] /= sum;
    	
    	return kernel; // return ready kernel
    }
    
    /**
	 * Method creates an 1D Gaussian kernel by the input values
	 * 
	 * @param length of the kernel
	 * @param sigma varianz of the kernel
	 * @param xDirection boolean for kernel in x direction or y direcion
	 * @return 1D gaussian kernel
	 */
    public static double[][] get1DGaussianKernel(int length, double sigma, Boolean xDirection)
    {
    	//create the kernel depending of the direction
    	double[][] kernel;
    	if(xDirection)
    		kernel = new double[length][1];
    	else
    		kernel = new double[1][length];
    	
    	//calculate the half size of the kerneld directions
    	int halfX = (int) ((kernel.length - 1) / 2.0);
    	int halfY = (int) ((kernel[0].length - 1) / 2.0);
    	
    	//the 1D formula of the gaussian filter
    	double calculatedEuler = 1.0 / Math.sqrt(2.0 * Math.PI * sigma); 
    	
    	//iterate over the kernel and calculate the single values depending on the positions
    	double sum = 0;
    	for(int x = -halfX; x <= halfX; x++)
    		for(int y = -halfY; y <= halfY; y++)
    		{
    			double distance = -(((x * x) + (y * y)) / (2.0 * sigma * sigma));
    			kernel[x + halfX][y + halfY] = calculatedEuler * Math.exp(distance); 			
    			sum += kernel[x + halfX][y + halfY];
    		}
    	
    	//normalize the kernel
    	for(int x = 0; x < kernel.length; x++)
    		for(int y = 0; y < kernel[x].length; y++)
    			kernel[x][y] /= sum;
    	
    	return kernel; //return ready kernel
    }
    
    static float[][] getGaussianKernel(float sigma)
    {
    	//List<List<Float>> list = new Vector<Vector<Float>>();
    	return null;
    }

    /**
     * Method for getting 3x3 sobel filter for gradient images in x direction.
     * @return a 3x3 Sobel filter
     */
    static double[][] getSobelXKernel()
    {
    	return new double[][] {{-1, 0, 1}};
    }
    
    /**
     * Method for getting 3x3 sobel filter for gradient images in y direction.
     * @return a 3x3 Sobel filter
     */
    static double[][] getSobelYKernel()
    {
    	return new double[][] {{1} , {0}, {-1}};
    }
}
