import java.io.File;


public class RenameFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		File folderDst = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_big");

		File[] filesDst = folderDst.listFiles();
		
		for(int i = 0; i < folderDst.length(); i++)
		{
			
			String name = filesDst[i].getName();
			String together = name.substring(0, 4) + "_" + name.substring(4);
			File newFile = new File("D:\\HTW Berlin\\4. Semester\\IC\\workspace\\IR_SURF\\images\\holiday_big\\" + together);
			filesDst[i].renameTo(newFile);
		}

	}

}
