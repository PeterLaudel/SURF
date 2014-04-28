import java.awt.Point;
import java.util.Vector;

public class BoxFilter {

	Vector<Box> m_boxes;
	private int m_size;

	
	
	
	public BoxFilter(int size)
	{
		m_boxes = new Vector<Box>(4);
		m_size = size;
	}
	
	public void AddBox(Box box)
	{
		m_boxes.add(box);
	}
	
	public int GetSize()
	{
		return m_size;
	}
	
	public int GetWeight()
	{
		return m_size * m_size;
	}
	
	public Vector<Box> GetBoxes()
	{
		return m_boxes;
	}
	
	private static boolean IsEven(int value)
	{
		return ((value & 1) == 0 );
	}
	
	private void Scale(int scaleFactor)
	{
		for(int i = 0; i < m_boxes.size(); i++)
		{
			Box b = m_boxes.get(i);
			b.Scale(scaleFactor);
		}
	}

	
	static BoxFilter GetSURFxxFilter(int size)
	{
		
		if(IsEven(size))
			return null;
		
		
		
		BoxFilter filter = new BoxFilter(size);
		int scaleFactor = (int) ((size - Octave.DEFAULT_FILTER_SIZE) / 6.0);
		int xBorder = 2 * (scaleFactor + 1);

		filter.AddBox(new Box(new Point(-xBorder, -4 - (scaleFactor * 3)), new Point(xBorder, -2 - scaleFactor), 1));
		filter.AddBox(new Box(new Point(-xBorder, -1 - scaleFactor), new Point(xBorder, 1 + scaleFactor), -2));
		filter.AddBox(new Box(new Point(-xBorder, 2 + scaleFactor), new Point(xBorder, 4 + (scaleFactor * 3)), 1));
		//filter.Scale(scaleFactor);
		
		return filter;
	}
	
	static BoxFilter GetSURFyyFilter(int size)
	{
		if(IsEven(size))
			return null;
		
		int scaleFactor = (int) ((size - Octave.DEFAULT_FILTER_SIZE) / 6.0);
		int yBorder = 2 * (scaleFactor + 1);
		
		BoxFilter filter = new BoxFilter(size);
		filter.AddBox(new Box(new Point(-4  - (scaleFactor * 3), -yBorder), new Point(-2 - scaleFactor, yBorder), 1));
		filter.AddBox(new Box(new Point(-1 - scaleFactor, -yBorder), new Point(1 + scaleFactor, yBorder), -2));
		filter.AddBox(new Box(new Point(2 + scaleFactor, -yBorder), new Point(4  + (scaleFactor * 3), yBorder), 1));
		
		return filter;
	}
	
	static BoxFilter GetSURFxyFilter(int size)
	{
		if(IsEven(size))
			return null;
		
		int scaleFactor = (int) ((size - Octave.DEFAULT_FILTER_SIZE) / 6.0) * 2;
		
		BoxFilter filter = new BoxFilter(size);
		filter.AddBox(new Box(new Point(-3 - scaleFactor, -3 - scaleFactor), new Point(-1, -1), 1));
		filter.AddBox(new Box(new Point(1, -3 - scaleFactor), new Point(3 + scaleFactor, -1), -1));
		filter.AddBox(new Box(new Point(-3 - scaleFactor, 1), new Point(-1, 3 + scaleFactor), -1));
		filter.AddBox(new Box(new Point(1, 1), new Point(3 + scaleFactor, 3 + scaleFactor), 1));
		
		return filter;
	}
}
