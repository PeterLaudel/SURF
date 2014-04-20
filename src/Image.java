
public class Image {
	
	enum Channels
	{
		RGBA,
		Gray
	}
	
	public int[] imagePixels;
	public int imageWidth;
	public int imageHeight;
	
	
	Image(int[] pixels, int width, int height)
	{
		imagePixels = pixels;
		imageWidth = width;
		imageHeight = height;
	}
}
