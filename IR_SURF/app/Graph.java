package app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JFrame;


public class Graph 
{ 

	private final int width=500, height=400, margin_top=60, margin_right=300, margin_bottom=20, margin_left=20;
	private final int p=2, r=3;
	private Vector<float[][]> graphs = new Vector<float[][]>();
	private Vector<String> titles = new Vector<String>();
	private Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.ORANGE, Color.CYAN};
	private JFrame frame = new frame();
	public boolean closed = false;

	public Graph (float[][] pUeberR, String path, String type)	{ 
		graphs.add(pUeberR);
		titles.add(type);
		
		frame.setTitle("Precision-Recall-Graph");
		frame.setSize(width+margin_left+margin_right, height+margin_top+margin_bottom);
		frame.setLocation(500, 0);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
	}

	public void addGraph(float[][] pUeberR, String titel)
	{
		graphs.add(pUeberR);
		titles.add(titel);
		frame.repaint();
	}
		
	class frame extends JFrame implements WindowListener 
	{
		private static final long serialVersionUID = 5827852254290989616L;

		public frame() {
			addWindowListener( this ); 
		}

		public void paint (Graphics graphics)	{ 
//			System.out.println("graph paint");
			paintCoordinates(graphics);			

			for (int i=0; i<graphs.size(); i++) {
				Color c; 
				if(i >= 5) {
					int r = (i*200)%256;
					int g = (i*95)%256;
					int b = (i*35)%256;
					c = new Color(r, g, b);
				}
				else
					 c = colors[i];
				paintGraphs(graphs.elementAt(i), c, graphics, i, titles.elementAt(i));
			}
		} 

		public void paintGraphs(float[][] pUeberR, Color c, Graphics g, int z, String titel){
			int x1, x2, y1, y2;
			g.setColor(c);
			g.drawString(titel, margin_left+width+10, margin_top+z*15);
			for (int i = 1; i < pUeberR[p].length; i++) {
				x1 = margin_left + (int)(pUeberR[r][i-1]*width+0.5);
				x2 = margin_left + (int)(pUeberR[r][i]*width+0.5);
				
				y1 = height + margin_top - (int)(pUeberR[p][i-1]*height+0.5);
				y2 = height + margin_top - (int)(pUeberR[p][i]*height+0.5);
//				System.out.println("Draw: "+x1+","+y1+" -> "+x2+","+y2);
				
				g.drawLine(x1, y1, x2, y2);
				g.setColor(Color.RED);
				g.fillRect(x1-1, y1-1, 3, 3);
				g.setColor(c);
			}

		}

		private void paintCoordinates(Graphics g) {			
			g.setColor(Color.white);
			g.fillRect(0, 0, width+margin_left+margin_right, height+margin_bottom+margin_top);
			g.setColor(Color.black);
			g.drawLine(margin_left, margin_top, margin_left, height+margin_top);
			g.drawLine(margin_left, height+margin_top, width+margin_left, height+margin_top);
			g.drawString("P", 7, margin_top+10);
			g.drawString("R", margin_left+width-15, margin_top+height+12);
			
			int hl = 10; // 4 -> Hilfslinie alle viertel
			for(int z=0; z<=hl; z++) {
				//Hilfslinien zeichnen
				//vertikal
				g.drawLine(margin_left+z*width/hl, margin_top, margin_left+z*width/hl, height+margin_top);
				//horizontal
				g.drawLine(margin_left, margin_top+z*height/hl, width+margin_left, margin_top+z*height/hl);
			}
		}
		
		public void repaint()
		{
			super.repaint();
//			System.out.println("graph repaint");
		}

		public void update (Graphics g) 
		{
			paint(g);
//			System.out.println("graph update");
		}
		
		public void windowClosing(WindowEvent e) 
		{
			closed = true;
			this.dispose();
		}

		public void windowActivated(WindowEvent e) {}
//		public void windowActivated(WindowEvent e) {System.out.println("graph windows activated");}

		public void windowClosed(WindowEvent e) {}

		public void windowDeactivated(WindowEvent e) {}

		public void windowDeiconified(WindowEvent e) {}

		public void windowIconified(WindowEvent e) {}

		public void windowOpened(WindowEvent e) {}
	}
}






