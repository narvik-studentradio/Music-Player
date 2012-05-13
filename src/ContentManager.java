import java.io.FileNotFoundException;


public class ContentManager {
	private PropertyParser parser;
	private ContentCollection music;
	private ContentCollection spots;
	private int songsPerSpot;
	private int songNr = 0;
	private int spotNr = 0;
	private int sinceSpot = 0;
	private String type = "";
	private FolderWatcher watcher;
	
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
	
	public String getNext() {
		//Check programs
		if(watcher != null) {
			String toPlay = watcher.toPlay();
			if(toPlay != null)
			{
				type = "program";
				return toPlay;
			}
		}
		
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

	public String getType() {
		return type;
	}
	
	@Override
	protected void finalize() throws Throwable {
		// Not sure if needed, but why not.
		if(watcher != null)
			watcher.interrupt();
		super.finalize();
	}
}
