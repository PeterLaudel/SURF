import java.awt.Point;
import java.util.Vector;

public class BoxFilter {

	Vector<Box> m_boxes;
	private int m_size;

	private final static int DEFAULT_SIZE = 9;
	
	
	public BoxFilter(int size)
	{
		m_boxes = new Vector<Box>();
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
	
	static BoxFilter GetSURFxxFilter()
	{
		return BoxFilter.GetSURFxxFilter(BoxFilter.DEFAULT_SIZE);
	}
	
	static BoxFilter GetSURFxxFilter(int size)
	{
		
		if(IsEven(size))
			return null;
		
		BoxFilter filter = new BoxFilter(size);
		int scaleFactor = (int) (Math.round((float) size / (float) BoxFilter.DEFAULT_SIZE) + 0.5f);
		filter.AddBox(new Box(new Point(-2, -4), new Point(2, -2), 1));
		filter.AddBox(new Box(new Point(-2, -1), new Point(2, 1), -2));
		filter.AddBox(new Box(new Point(-2, 2), new Point(2, 4), 1));
		filter.Scale(scaleFactor);
		
		return filter;
	}
	
	static BoxFilter GetSURFyyFilter(int size)
	{
		if(IsEven(size))
			return null;
		
		BoxFilter filter = new BoxFilter(size);
		filter.AddBox(new Box(new Point(-4, -2), new Point(-2, 2), 1));
		filter.AddBox(new Box(new Point(-1, -2), new Point(1, 2), -2));
		filter.AddBox(new Box(new Point(2, -2), new Point(4, 2), 1));
		
		return filter;
	}
	
	static BoxFilter GetSURFxyFilter(int size)
	{
		if(IsEven(size))
			return null;
		
		BoxFilter filter = new BoxFilter(size);
		filter.AddBox(new Box(new Point(-3, -3), new Point(-1, -1), 1));
		filter.AddBox(new Box(new Point(1, -3), new Point(3, -1), -1));
		filter.AddBox(new Box(new Point(-3, 1), new Point(-1, 3), -1));
		filter.AddBox(new Box(new Point(1, 1), new Point(3, 3), 1));
		
		return filter;
	}
}