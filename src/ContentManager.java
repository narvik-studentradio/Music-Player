import java.io.FileNotFoundException;
import java.io.IOException;


public class ContentManager {
	private PropertyParser parser;
	private ContentCollection music;
	private ContentCollection spots;
	private Object contentLock = new Object();
	private int songsPerSpot;
	private int songNr = 0;
	private int spotNr = 0;
	private int sinceSpot = 0;
	private String type = "";
	private FolderWatcher watcher;
	private Object watcherLock = new Object();
	
	public ContentManager(PropertyParser propertyParser) {
		this.parser = propertyParser;
		this.music = parser.getMusic();
		this.spots = parser.getSpots();
		this.songsPerSpot = parser.getSongsPerSpot();
		try {
			this.watcher = new FolderWatcher(parser.getWatchFolder(), parser.getWatchExtensions());
			watcher.start();
		} catch (FileNotFoundException e) {
		}
		music.shuffle();
		spots.shuffle();
	}
	
	public boolean rescan() {
		try {
			parser.reload();
		} catch (IOException e) {
			return false;
		}
		ContentCollection newMusic = parser.getMusic();
		ContentCollection newSpots = parser.getSpots();
		synchronized(contentLock) {
			this.music = newMusic;
			music.shuffle();
			this.spots = newSpots;
			spots.shuffle();
		}
		this.songsPerSpot = parser.getSongsPerSpot();
		try {
			FolderWatcher newWatcher = new FolderWatcher(parser.getWatchFolder(), parser.getWatchExtensions());
			newWatcher.start();
			synchronized(watcherLock) {
				watcher.interrupt();
				watcher = newWatcher;
			}
		} catch (FileNotFoundException e1) {
			return false;
		}
		return true;
	}
	
	public String getNext(PlayMode mode) {
		if(mode == PlayMode.Music) {
			synchronized(contentLock) {
				if(songNr >= music.size()) {
					songNr = 0;
					music.shuffle();
				}
				//Play song
				type = music.getType(songNr);
				return music.getFile(songNr++);
			}
		}
		
		//Check programs
		synchronized(watcherLock) {
			if(watcher != null) {
				String toPlay = watcher.toPlay();
				if(toPlay != null)
				{
					type = "program";
					return toPlay;
				}
			}
		}
		
		synchronized(contentLock) {
			if(spotNr >= spots.size()) {
				spotNr = 0;
				spots.shuffle();
			}
			if(songNr >= music.size()) {
				songNr = 0;
				music.shuffle();
			}
			if(sinceSpot >= songsPerSpot) {
				//Play spot
				sinceSpot = 0;
				type = spots.getType(spotNr);
				return spots.getFile(spotNr++);
			}
			else {
				//Play song
				sinceSpot++;
				type = music.getType(songNr);
				return music.getFile(songNr++);
			}
		}
	}

	public String getType() {
		return type;
	}
	
	public void close() {
		if(watcher != null)
			watcher.interrupt();
	}
}
