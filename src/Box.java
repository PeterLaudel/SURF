import java.awt.Point;


public class Box
{
	Point m_leftUp;
	Point m_rightDown;
	int m_weight;
	
	public Box(Point leftUp, Point rightDown, int weight)
	{
		m_leftUp = leftUp;
		m_rightDown = rightDown;
		m_weight = weight;
	}
	
	public Point GetLeftUpperPoint()
	{
		return m_leftUp;
	}
	
	public Point GetRightBottemPoint()
	{
		return m_rightDown;
	}
	
	public int GetWeight()
	{
		return m_weight;
	}
	
	public int GetArea()
	{
		int count  = 0;
		for(int x = m_leftUp.x; x <= m_rightDown.x; x++)
			for(int y = m_leftUp.y; y <= m_rightDown.y; y++)
				count++;
		
		return count;
	}
	
	public void Scale(int scaleFactor)
	{
		m_leftUp.x *= scaleFactor;
		m_leftUp.y *= scaleFactor;
		m_rightDown.x *= scaleFactor;
		m_rightDown.y *= scaleFactor;
	}
}
