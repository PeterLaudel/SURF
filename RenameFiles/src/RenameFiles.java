import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import Features.InterestPoint;
import app.SurfBinaryFile;

public class RenameFiles {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		/*

		*/
		LoadDescriptorFromOutputStream();
	}

	static void RenameFiles() {
		File folderDst = new File(
				"D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_big");

		File[] filesDst = folderDst.listFiles();

		for (int i = 0; i < folderDst.length(); i++) {

			String name = filesDst[i].getName();
			String together = name.substring(0, 4) + "_" + name.substring(4);
			File newFile = new File(
					"D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_big\\"
							+ together);
			filesDst[i].renameTo(newFile);
		}
	}

	static void SaveDescriptorAsOutputStream() {

		String startDirectory = "./images/";

		JFileChooser fc = new JFileChooser(startDirectory);

		// Nur komplette Ordner koennen ausgewaehlt werden
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(null);

		if (returnVal == -1)
			return;

		File folder = fc.getSelectedFile();
		String path = folder.getPath();
		File[] files = folder.listFiles();

		SurfBinaryFile sbf = new SurfBinaryFile(path, "descriptor");
		Map<Integer, List<InterestPoint>> map = sbf.ReadSurfBinaryFile(200);

		float[][] values = new float[files.length * 200][64];

		for (int i = 0; i < files.length; i++) {

			String filename = files[i].getName();
			if (!(filename.endsWith("jpg") || filename.endsWith("png") || filename
					.endsWith("gif")))
				continue;

			int hash = filename.hashCode();
			List<InterestPoint> list = map.get(hash);

			for (int j = 0; j < list.size(); j++) {
				InterestPoint ip = list.get(j);
				values[i * 200 + j] = ip.descriptor;
			}
			System.out.println("" + i);
		}

		// speichere auf die Festplatte
		// File file = new File(path +"/test.lol");
		try (ObjectOutputStream oos = new ObjectOutputStream(
				Files.newOutputStream(Paths.get(path + "\\file.ext")))) {
			oos.writeObject(values);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void LoadDescriptorFromOutputStream() throws ClassNotFoundException
	{
		String startDirectory = "./images/";
		
		JFileChooser fc = new JFileChooser(startDirectory);

		
		
		// Nur komplette Ordner koennen ausgewaehlt werden
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(null);
		
		if(returnVal == -1)
			return;
		
		
		
		File folder = fc.getSelectedFile();
		String path = folder.getPath();
		File[] files = folder.listFiles();
		
		
		SurfBinaryFile sbf = new SurfBinaryFile(path, "descriptor");
		Map<Integer, List<InterestPoint>> map = sbf.ReadSurfBinaryFile(200);
		
		float[][] values = new float[files.length * 200][64];
		
		for(int i = 0; i < files.length; i++)
		{
			
			String filename = files[i].getName();
			if (!(filename.endsWith("jpg") || filename.endsWith("png") || filename.endsWith("gif")))
				continue;
			
			int hash = filename.hashCode();
			List<InterestPoint> list = map.get(hash);
			
			for(int j = 0; j < list.size(); j++)
			{
				InterestPoint ip = list.get(j);
				values[i * 200 + j] = ip.descriptor;
			}
			System.out.println("" + i);
		}
		
		
		// speichere auf die Festplatte
		//File file = new File(path +"/test.lol");
        try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(path + "\\file.output")))) {
        	float[][] values32 = (float[][]) ois.readObject();
           sbf = new SurfBinaryFile(path, "descriptor32");
           Map<Integer, List<InterestPoint>> finalMap = new HashMap<Integer, List<InterestPoint>>();
           for(int i = 0; i< files.length; i++)
           {
        	   String filename = files[i].getName();
   			if (!(filename.endsWith("jpg") || filename.endsWith("png") || filename.endsWith("gif")))
   				continue;
   			
   			int hash = filename.hashCode();
   			List<InterestPoint> list = map.get(hash);
   			
   			for(int j = 0; j < list.size(); j++)
			{
				InterestPoint ip = list.get(j);
				ip.descriptor = values32[i * 200 + j];
			}
   			finalMap.put(hash, list);
           }
           sbf.WriteSurfBinaryFile(finalMap);
        } catch (IOException e) {
             e.printStackTrace();
        }
	}
}
