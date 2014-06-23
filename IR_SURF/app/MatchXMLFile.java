package app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import Features.InterestPoint;
import PicPropertys.Pic;

public class MatchXMLFile {
	
	String m_path;
	String m_filename;
	Element m_rootElement;
	Document m_document;
	
	public MatchXMLFile(String path, String filename) {
		// TODO Auto-generated constructor stub
		m_path = path;
		m_filename = filename;

	}
	
	public Map<Integer, Map<Integer, Float>> ReadMatches()
	{
		Map<Integer, Map<Integer, Float>> resultMap  = new HashMap<Integer, Map<Integer, Float>>();
		try
		{
			File fXmlFile = new File(m_path + "/" + m_filename);
			if(!fXmlFile.exists())
				return resultMap;
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
		 
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
		 
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		 
			NodeList nList = doc.getElementsByTagName("match");
		 
			//System.out.println("----------------------------");
			
			resultMap = new HashMap<Integer, Map<Integer, Float>>(nList.getLength());
		 
			for (int temp = 0; temp < nList.getLength(); temp++) {
		 
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					String attribute = eElement.getAttribute("id");
					int id = Integer.parseInt(attribute);
					
					Map<Integer, Float> distanceMap = new HashMap<Integer, Float>();
					
					NodeList distancenodeList = eElement.getElementsByTagName("distance");
					for(int i = 0; i < distancenodeList.getLength(); i++)
					{
						Element ipElement = (Element) distancenodeList.item(i);
						int distanceId = Integer.parseInt(ipElement.getAttribute("id"));
						float distance = Float.parseFloat(ipElement.getChildNodes().item(0).getTextContent());
						distanceMap.put(distanceId, distance);
					}
					resultMap.put(id, distanceMap);
				}
			}
			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		return resultMap;
	}
	
	public void WriteMatches(Map<Integer, Map<Integer, Float>> matches)
	{
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("matches");
			doc.appendChild(rootElement);
			
			Iterator it = matches.entrySet().iterator();
			
		
			while(it.hasNext())
			{
				Map.Entry<Integer, Map<Integer, Float>> entry = (Entry<Integer, Map<Integer, Float>>) it.next();
				
				Element matchElement = doc.createElement("match");
				matchElement.setAttribute("id", Integer.toString(entry.getKey()));
				rootElement.appendChild(matchElement);
				
		 
				Iterator it2 = entry.getValue().entrySet().iterator();
				
				while(it2.hasNext())
				{
					Map.Entry<Integer, Float> distance = (Entry<Integer, Float>) it2.next();
					
					
					
					Element distanceElement = doc.createElement("distance");
					distanceElement.setAttribute("id", Integer.toString(distance.getKey()));
					distanceElement.appendChild(doc.createTextNode(distance.getValue().toString()));
					matchElement.appendChild(distanceElement);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(m_path + "/" + m_filename));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
	 
		  } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		  } catch (TransformerException tfe) {
			tfe.printStackTrace();
		  }
	}
}
