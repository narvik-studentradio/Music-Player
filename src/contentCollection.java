import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class contentCollection {
	private String path, type;
	private ArrayList<File> files;
	
	public contentCollection(String path, String type)
	{
		this.path = path;
		this.type = type;
		scan();
		shuffle();
	}
	
	public void shuffle()
	{
		 Collections.shuffle(files);
	}

	public void scan() {
		files = scan(new File(path));
		System.gc();
		for(File f : files)
		{
			System.out.println(f.getPath());
		}

	}
	
	private ArrayList<File> scan(File path)
	{
		/*
		 * Lets pork out on ArrayLists
		 */
		ArrayList<File> files = new ArrayList<File>();
		File[] listOfFiles = path.listFiles();
		
	    for (int i = 0; i < listOfFiles.length; i++) {
	        if (listOfFiles[i].isFile()) {
	          files.add(listOfFiles[i]);
	        } else if (listOfFiles[i].isDirectory()) {
	          files.addAll(scan(listOfFiles[i]));
	        }
	      }
		return files;
	}

}
