import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;


public class ContentManager {
	private PropertyParser parser;
	private ContentCollection music;
	private ContentCollection spots;
	private final Object contentLock = new Object();
	private int songsPerSpot;
	private int songNr = 0;
	private int spotNr = 0;
	private int sinceSpot = 0;
	private String type = "";
	private FolderWatcher watcher;
	private final Object watcherLock = new Object();
//	private final URI defaultStreamUri;
//	private URI streamUri = null;

	public ContentManager(PropertyParser propertyParser) {
		this.parser = propertyParser;
//		this.defaultStreamUri = parser.getStreamDefault();
//		this.streamUri = defaultStreamUri;
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
	
	public URI getNext(PlayMode mode) {
		if(mode == PlayMode.Music) {
			synchronized(contentLock) {
				if(songNr >= music.size()) {
					songNr = 0;
					music.shuffle();
				}
				//Play song
				type = music.getType(songNr);
				return new File(music.getFile(songNr++)).toURI();
			}
		}
//		else if(mode == PlayMode.Stream) {
//			if(streamUri == null) {
//				System.err.println("No valid stream URI available");
//				return getNext(PlayMode.Music);
//			}
//			type = "Stream";
//			return streamUri;
//		}
		
		//Check programs
		synchronized(watcherLock) {
			if(watcher != null) {
				String toPlay = watcher.toPlay();
				if(toPlay != null)
				{
					type = "program";
					return new File(toPlay).toURI();
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
				return new File(spots.getFile(spotNr++)).toURI();
			}
			else {
				//Play song
				sinceSpot++;
				type = music.getType(songNr);
				return new File(music.getFile(songNr++)).toURI();
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
	
//	public URI getStreamUri() {
//		return streamUri;
//	}
//
//	public void setStreamUri(URI streamUri) {
//		this.streamUri = streamUri;
//	}
//	
//	public void resetStreamUri() {
//		streamUri = defaultStreamUri;
//	}
}
