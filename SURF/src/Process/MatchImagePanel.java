package Process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import FeatureMatching.BruteForceMatching;
import FeatureMatching.FeatureMatchFilter;
import FeatureMatching.KDTree;
import FeatureMatching.Matches;
import Features.InterestPoint;
import Imageprocess.Image;
import SURF.SurfFeatureDetector;


@SuppressWarnings("serial")
public class MatchImagePanel extends JPanel {
	
	ImageView m_matchView;
	private static final int maxWidth = 600;
	private static final int maxHeight = 600;
	
	JSlider m_thresholdSlider;
	List<Matches> m_matches;
	List<InterestPoint> m_interestPoints1;
	List<InterestPoint> m_interestPoints2;
	
	int m_offset;
	
	
	public MatchImagePanel(List<InterestPoint> interestPoints1, Image image1, List<InterestPoint> interestPoints2, Image image2)
	{
		super(new BorderLayout());
		KDTree kdTree = new KDTree();
		
		String outputString = new String();
		SurfFeatureDetector sfd = new SurfFeatureDetector(200, 4);
		outputString += "Matching ";

		long startTime = System.currentTimeMillis();
		//List<List<Matches>> matches = kdTree.KnnMatching(interestPoints1, interestPoints2, 2);
		
		List<Matches> matches = BruteForceMatching.BFMatch(interestPoints1, interestPoints2);
		List<Matches> matches2 = BruteForceMatching.BFMatch(interestPoints2, interestPoints1);
		outputString += "matching: " + (System.currentTimeMillis() - startTime) + " ms  " + "befor filtering: " + matches.size() + " ";
		
		startTime = System.currentTimeMillis();
		//m_matches = FeatureMatchFilter.DoRatioTest(matches);
		m_matches = FeatureMatchFilter.DoSymmetryTest(matches, matches2);
		outputString += "filter: " + (System.currentTimeMillis() - startTime) + " ms  ";
		outputString += "matches: " + m_matches.size();
		Image mergedImage = kdTree.GetHorizontalMergedImage(image1, image2);
		
		m_matchView = new ImageView(maxWidth, maxHeight);
		m_matchView.setPixels(mergedImage.GetImagePixels(), mergedImage.GetWidth(), mergedImage.GetHeight());
		
		m_thresholdSlider = new JSlider(0, 1000, 1000);
		
		m_thresholdSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				ApplyThreshold( (float) m_thresholdSlider.getValue()/ (float) m_thresholdSlider.getMaximum());
			}
		});
		
		m_interestPoints1 = interestPoints1;
		m_interestPoints2 = interestPoints2;
		
		m_offset = image1.GetWidth();
		
		JLabel thresholdLabel = new JLabel("Threshold: ");
		
		JPanel controls = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 6, 0, 0);
		controls.add(thresholdLabel, c);
		controls.add(m_thresholdSlider, c);
		
		JPanel tmp = new JPanel();
		tmp.add(m_matchView);
		add(controls, BorderLayout.NORTH);
		add(tmp, BorderLayout.CENTER);
		add(new JLabel(outputString), BorderLayout.SOUTH);
		
		ApplyThreshold(1.0f);
	}
	
	private void ApplyThreshold(float threshold)
	{
		m_matchView.ClearShape();
    	for(int i = 0; i <m_matches.size(); i++)
    	{
    		Matches match = m_matches.get(i);
    		if((match.distance) >= threshold)
    			continue;
    		
    		Shape s;
    		InterestPoint ip1 = m_interestPoints1.get(match.idx1);
    		InterestPoint ip2 = m_interestPoints2.get(match.idx2);
    		
    		float r = (float) Math.random();
    		float g = (float) Math.random();
    		float b = (float) Math.random();

    		s = new Line2D.Float(ip1.x, ip1.y, ip2.x+m_offset, ip2.y);
    		m_matchView.AddShape(s, new Color(r, g, b));

    	}
	}

}
