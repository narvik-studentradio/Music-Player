package no.samfunnet.nsr.nsrmp;
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
import java.util.List;

public class ContentCollection {
	private ArrayList<Song> songs;

	public ContentCollection() {
		songs = new ArrayList<Song>();
	}

	public void shuffle() {
		Collections.shuffle(songs);
	}

	public void scan(String location, String name, List<String> extensions) {
		songs.addAll(scan(new File(location), name, extensions));
		System.gc();
	}

	private Collection<? extends Song> scan(File path, String typeName,
			List<String> extensions) {
		/*
		 * Lets pork out on ArrayLists
		 */
		ArrayList<Song> tempSongs = new ArrayList<Song>();
		File[] listOfFiles = path.listFiles();

		for(File f : listOfFiles) {
			if (f.isFile() && extensions.contains(f.getName().substring(f.getName().lastIndexOf(".")+1))) {
				tempSongs.add(new Song(f, typeName));
			} else if (f.isDirectory()) {
				tempSongs.addAll(scan(f, typeName, extensions));
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
