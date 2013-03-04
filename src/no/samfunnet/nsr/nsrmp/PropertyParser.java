package no.samfunnet.nsr.nsrmp;
import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import no.samfunnet.nsr.nsrmp.parser.Content;
import no.samfunnet.nsr.nsrmp.parser.Metadata;
import no.samfunnet.nsr.nsrmp.parser.XmlProperties;



public class PropertyParser {
	private volatile XmlProperties properties;
	private String fileName = "properties.xml"; 

	public PropertyParser() throws JAXBException {
		reload();
	}
	
	public PropertyParser(String fileName) throws JAXBException {
		this.fileName = fileName;
		reload();
	}
	
	public void reload() throws JAXBException {
		File file = new File(fileName);
		if(!file.isFile())
			throw new IllegalArgumentException("Invalid property file.");
		
		final JAXBContext jaxbContext = JAXBContext.newInstance(XmlProperties.class);
		properties = (XmlProperties) jaxbContext.createUnmarshaller().unmarshal(file);
	}
	
	public ArrayList<MetadataServer> getMetadataServers() {
		ArrayList<MetadataServer> mdServers = new ArrayList<MetadataServer>();

		for(Metadata md : properties.metadata) {
			mdServers.add(new MetadataServer(md.ssl, md.server, md.port, md.mount, md.user, md.password));
		}
		
		return mdServers;
	}
	
	public ContentCollection getMusic() {
		/*
		 * contentCollection with all the music
		 */
		ContentCollection music = new ContentCollection();

		for(Content c : properties.content) {
			if(c.type.equals(properties.spotType) || c.type.equals(properties.watchType))
				continue;
			music.scan(c.location, c.type, c.filetypes);
		}
		return music;
	}
	
	public ContentCollection getSpots() {
		/*
		 * contentCollection with all the spots
		 */
		ContentCollection spots = new ContentCollection();

		for(Content c : properties.content) {
			if(c.type.equals(properties.spotType)) {
				spots.scan(c.location, c.type, c.filetypes);
			}
		}
		return spots;
	}
	
	public int getSongsPerSpot() {
		return properties.contentPerSpot;
	}
	
	public String getLogDir() {
		return properties.log;
	}
	
	public String getWatchFolder() {
		for(Content c : properties.content)
			if(c.type.equals(properties.watchType))
				return c.location;
		return null;
	}
	
	public String[] getWatchExtensions() {
		for(Content c : properties.content)
			if(c.type.equals(properties.watchType))
				return c.filetypes.toArray(new String[c.filetypes.size()]);
		return new String[0];
	}
	
	public String getBroadcastArtist() {
		return properties.broadcastArtist;
	}
	
	public String getBroadcastTitle() {
		return properties.broadcastTitle;
	}
	
	public String getBroadcastAlbum() {
		return properties.broadcastAlbum;
	}
	
//	public URI getStreamDefault() {
//		URI uri = null;
//		try {
//			uri = new URI(properties.getProperty("streamDefault"));
//		} catch (URISyntaxException e) {
//			System.err.println("Invalid stream url: " + e.getMessage());
//		}
//		return uri;
//	}
}
