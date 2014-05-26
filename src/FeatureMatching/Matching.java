package FeatureMatching;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Vector;

import javax.swing.JComponent;

import Features.InterestPoint;
import Imageprocess.Image;

public class Matching {

	public Matching() {
		// TODO Auto-generated constructor stub
	}
	
	public void Match(Vector<InterestPoint> interestPoints1, Vector<InterestPoint> interestPoints2, Vector<Matches> matches)
	{
		for(int i = 0; i < interestPoints1.size(); i++)
		{
			InterestPoint ip1 = interestPoints1.get(i);
			int index = -1;
			float tmpDistance = Float.MAX_VALUE;
			for(int j = 0; j < interestPoints2.size(); j++)
			{		
				InterestPoint ip2 = interestPoints2.get(j);
				float distance = GetEuclidianDistance(ip1, ip2);
				if(distance < tmpDistance)
				{
					index = j;
					tmpDistance = distance;
				}
					
			}
			matches.add(new Matches(i, index, tmpDistance));
		}
		matches.trimToSize();
	}
	

	public float GetEuclidianDistance(InterestPoint ip1, InterestPoint ip2)
	{
		float[] descriptor1 = ip1.descriptor;
		float[] descriptor2 = ip2.descriptor;
		
		if(descriptor1.length != descriptor2.length)
			return Float.MAX_VALUE;
		
		float sum = 0;
		for(int i = 0; i < descriptor1.length; i++)
		{
			float value = descriptor1[i] - descriptor2[i];
			sum += value * value;
		}
		
		return sum;
	}
	
	public float GetEuclidianDistanceSqrt(InterestPoint ip1, InterestPoint ip2)
	{
		return (float) Math.sqrt(GetEuclidianDistance(ip1, ip2));
	}
	
	public JComponent DrawMatches(Image image1, Vector<InterestPoint> interestPoints1, Image image2, Vector<InterestPoint> interestPoints2, Vector<Matches> matches)
	{
		Image result = new Image(image1.GetWidth() + image2.GetWidth(), Math.max(image1.GetHeight(), image2.GetHeight()));
		int[] resultPixel = result.GetImagePixels();
		int[] image1Pxl = image1.GetImagePixels();
		int[] image2Pxl = image2.GetImagePixels();
		
		
		
		for(int y = 0; y < result.GetHeight(); y++)
		{
			int resultPos = y * result.GetWidth();
			int pos1 = y * image1.GetWidth();
			int pos2 = y * image2.GetWidth();
			
			if(y < image1.GetHeight())
				System.arraycopy(image1Pxl, pos1, resultPixel, resultPos, image1.GetWidth());
			
			if(y < image2.GetHeight())
				System.arraycopy(image2Pxl, pos2, resultPixel, resultPos + image1.GetWidth(), image2.GetWidth());
		}
		
		BufferedImage bf = new BufferedImage(result.GetWidth(), result.GetHeight(), BufferedImage.TYPE_INT_ARGB);
		
		bf.setRGB(0, 0, bf.getWidth(), bf.getHeight(), resultPixel, 0, bf.getWidth());
		
		SurfScreen ss = new SurfScreen(bf);
		
	
		

		for(int i = 0 ; i < matches.size(); i++)
		{
			
			Matches match = matches.get(i);
			if(match.distance > 0.2)
				continue;

			InterestPoint ip1 = interestPoints1.get(match.idx1);
			InterestPoint ip2 = interestPoints2.get(match.idx2);
			Line2D.Float line = new Line2D.Float(ip1.x, ip1.y, ip2.x + image1.GetWidth(), ip2.y);
			ss.AddShape(line);
		}
		ss.invalidate();
		ss.repaint();
		//g2d.dispose();

		return ss;
	}
	class SurfScreen extends JComponent {
		
		private static final long serialVersionUID = 1L;
		
		private BufferedImage image;
		Vector<Shape> m_shapes;

		public SurfScreen(BufferedImage bi) {
			super();
			image = bi;
			m_shapes = new Vector<Shape>();
		}
		
		public void AddShape(Shape shape)
		{
			m_shapes.add(shape);
		}
		
		public void ClearShapes()
		{
			m_shapes.clear();
		}
		
		public void paintComponent(Graphics g) {
			Rectangle r = this.getBounds();
			if (image != null)
				g.drawImage(image, 0, 0, r.width, r.height, this);
			Graphics2D g2d = (Graphics2D) g;
			Random rand = new Random();
			for(int i = 0; i < m_shapes.size(); i++)
			{
				float red = rand.nextFloat();
				float green = rand.nextFloat();
				float blue = rand.nextFloat();
				g2d.setColor(new Color(red, green, blue));
				g2d.draw(m_shapes.get(i));
			}

		}
		
		public Dimension getPreferredSize() {
			if(image != null) 
				return new Dimension(image.getWidth(), image.getHeight());
			else
				return new Dimension(100, 60);
		}
		 
	}

}
