import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class contentCollection {
	private ArrayList<song> songs;
	
	public contentCollection()
	{
	}
	
	public void shuffle()
	{
		 Collections.shuffle(songs);
	}

	public void scan(String location, String name) {
		songs.addAll(scan(new File(location), name));
		System.gc();
	}
	
	private Collection<? extends song> scan(File path, String typeName)
	{
		/*
		 * Lets pork out on ArrayLists
		 */
		ArrayList<song> tempSongs = new ArrayList<song>();
		File[] listOfFiles = path.listFiles();
		
	    for (int i = 0; i < listOfFiles.length; i++) {
	        if (listOfFiles[i].isFile()) {
	          tempSongs.add(new song(listOfFiles[i], typeName));
	        } else if (listOfFiles[i].isDirectory()) {
	          tempSongs.addAll(scan(listOfFiles[i], typeName));
	        }
	      }
		return tempSongs;
	}

	public String get(int playCount) {
		return songs.get(playCount).toString();
	}

	public int size() {
		return songs.size();
	}

}
