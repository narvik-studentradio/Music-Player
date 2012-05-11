import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;


public class PropertyParser {
	private Properties properties;
	private String fileName = "nsrmp.properties"; 

	public PropertyParser() throws FileNotFoundException, IOException {
		reload();
	}
	
	public PropertyParser(String fileName) throws FileNotFoundException, IOException {
		this.fileName = fileName;
		reload();
	}
	
	public void reload() throws FileNotFoundException, IOException {
		properties = new Properties();

		/*
		 * Laster inn crawler.properties slik at vi slipper ha alt hardkodet
		 */
		FileReader reader = new FileReader(new File(fileName));
		properties.load(reader);
		reader.close();
	}
	
	public ArrayList<MetadataServer> getMetadataServers() {
		/*
		 * An array with all the metadata Servers
		 */
		ArrayList<MetadataServer> mdServers = new ArrayList<MetadataServer>();

		//Start with Server 0
		int mdServerCounter = 0;
		while ((properties.getProperty("icecastServer" + mdServerCounter)) != null) {
			mdServers.add(
				new MetadataServer(
					Boolean.parseBoolean(properties.getProperty("icecastServer" + mdServerCounter + "Ssl")),
					properties.getProperty("icecastServer" + mdServerCounter),
					Integer.parseInt(properties.getProperty("icecastServer"	+ mdServerCounter + "Port")),
					properties.getProperty("icecastServer" + mdServerCounter + "Mount0"),
					properties.getProperty("icecastServer" + mdServerCounter + "User"),
					properties.getProperty("icecastServer" + mdServerCounter + "Password")
				)
			);
			//If there is more than 1 mount point
			int mountCounter = 1;
			while ((properties.getProperty("icecastServer" + mdServerCounter + "Mount" + mountCounter)) != null){
				mdServers.get(mdServerCounter).addMount(properties.getProperty("icecastServer" + mdServerCounter
									+ "Mount" + mountCounter));
				mountCounter++;
			}
			mdServerCounter++;
		}
		return mdServers;
	}
	
	public ContentCollection getMusic() {
		/*
		 * contentCollection with all the music
		 */
		ContentCollection music = new ContentCollection();

		//Start with musicType 0
		int contentTypeCounter = 0;
		while ((properties.getProperty("content" + contentTypeCounter + "Location")) != null) {
			music.scan(properties.getProperty("content" + contentTypeCounter + "Location"),
					properties.getProperty("content" + contentTypeCounter + "Name"));
			contentTypeCounter++;
		}
		//music.shuffle();
		return music;
	}
	
	public ContentCollection getSpots() {
		/*
		 * contentCollection with all the spots
		 */
		ContentCollection spots = new ContentCollection();

		//Start with spotType 0
		int spotTypeCounter = 0;
		while ((properties.getProperty("spot" + spotTypeCounter + "Location")) != null) {
			spots.scan(properties.getProperty("spot" + spotTypeCounter + "Location"),
					properties.getProperty("spot" + spotTypeCounter + "Name"));
			spotTypeCounter++;
		}
		//spots.shuffle();
		return spots;
	}
	
	public int getSongsPerSpot() {
		return Integer.parseInt(properties.getProperty("contentNo"));
	}
	
	public String getLogDir() {
		return properties.getProperty("logDir");
	}
}
