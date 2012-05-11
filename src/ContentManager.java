import java.io.File;

//TODO: A work in progress
public class ContentManager {
	private PropertyParser parser;
	private ContentCollection music;
	private ContentCollection spots;
	
	public ContentManager(PropertyParser propertyParser) {
		this.parser = propertyParser;
		this.music = parser.getMusic();
		this.spots = parser.getSpots();
	}
	
	public File getNext() {
		//TODO
		return null;
	}
}
