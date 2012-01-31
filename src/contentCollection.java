/*
 * This file is part of nsr-mp.
 * 
 * nsr-mp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * nsr-mp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with nsr-mp.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class contentCollection {
	private ArrayList<song> songs;
	
	public contentCollection()
	{
		songs = new ArrayList<song>();
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

	public String getFile(int playCount) {
		return songs.get(playCount).getFile().toString();
	}
	
	public String getType(int playCount) {
		return songs.get(playCount).getType().toString();
	}

	public int size() {
		return songs.size();
	}

}
