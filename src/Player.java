import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.gstreamer.Bus;
import org.gstreamer.ClockTime;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.TagList;
import org.gstreamer.elements.PlayBin2;


public class Player extends Thread implements Bus.EOS, Bus.ERROR, Bus.STATE_CHANGED, Bus.TAG {
	private PropertyParser parser;
	private ContentManager content;
	private List<MetadataServer> mdServers;
	private Object mdLock = new Object();
	private volatile String logDir;
	private PlayBin2 playbin;
	private String artist, album, title, length, type;
	private boolean quit = false;
	public Shotgun shotgun = new Shotgun();
	
	public Player() throws FileNotFoundException, IOException {
		super("Player");
		parser = new PropertyParser();
		
		this.content = new ContentManager(parser);
		this.mdServers = parser.getMetadataServers();
		this.logDir = parser.getLogDir();
		
		Gst.init("BusMessages", new String[]{});
		playbin = new PlayBin2("BusMessages");
		playbin.setVideoSink(ElementFactory.make("fakesink", "videosink"));
		Bus bus = playbin.getBus();
		bus.connect((Bus.EOS)this);
		bus.connect((Bus.ERROR)this);
		bus.connect((Bus.STATE_CHANGED)this);
		bus.connect((Bus.TAG)this);
	}
	
	public boolean rescan() {
		try {
			parser.reload();
		} catch (IOException e) {
			return false;
		}
		content.rescan();
		synchronized(mdLock) {
			mdServers = parser.getMetadataServers();
		}
		logDir = parser.getLogDir();
		return true;
	}

	@Override
	public void run() {
		// Playloop
		while(!quit) {
			Gst.init("BusMessages", new String[]{});
			playbin.setInputFile(new File(content.getNext()));
			type = content.getType();
			playbin.setState(org.gstreamer.State.PLAYING);
			Gst.main();
			playbin.setState(org.gstreamer.State.NULL);
			Gst.deinit();
		}
	}
	
	public void quit() {
		quit = true;
		interrupt();
		Gst.quit();
		content.close();
	}
	
	public void playOnceAt() {
		
	}

	@Override
	public void endOfStream(GstObject source) {
		artist = album = title = null;
		Gst.quit();
	}

	@Override
	public void errorMessage(GstObject source, int code, String message) {
		Gst.quit();
	}

	@Override
	public void stateChanged(GstObject source, org.gstreamer.State old,
			org.gstreamer.State current, org.gstreamer.State pending) {
		if(source != playbin)
			return;
		if(current != org.gstreamer.State.PLAYING)
			return;
		
		long sec = playbin.queryDuration().getSeconds();
		length = playbin.queryDuration().getMinutes() + ":" + (sec < 10 ? "0" : "") + sec;
		//printPlaying();
		printLog();
		updateMetadata();
		//System.out.println(Thread.activeCount() + " active threads");
	}

	@Override
	public void tagsFound(GstObject source, TagList tagList) {
		for (String tagName : tagList.getTagNames()) {
			if (tagName.equalsIgnoreCase("artist")) {
				artist = (String) tagList.getValues(tagName).get(0);
			} else if (tagName.equalsIgnoreCase("album")) {
				album = (String) tagList.getValues(tagName).get(0);
			} else if (tagName.equalsIgnoreCase("title")) {
				title = (String) tagList.getValues(tagName).get(0);
			}
		}
	}
	
	public void printPlaying() {
		// Displays Status of current song playing
		System.out.println("...................Playing....................");
		System.out.println("Title: " + title);
		System.out.println("Artist: " + artist);
		System.out.println("Album: " + album);
		System.out.println("Length: " + length);
	}
	
	private boolean printLog() {
		String CurrentDateTime = getDateTime();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(logDir, true));

			writer.write(CurrentDateTime + " - " + artist
					+ " - " + album + " - " + title + " - "
					+ length);
			writer.newLine();
			writer.close();
			/*
			System.out.println("............." + logDir + "..............");
			System.out.println("        ....|" + CurrentDateTime + "|....");
			*/
		}
		catch(IOException e) {
			return false;
		}
		return true;
	}
	
	private void updateMetadata() {
		// Update Icecast metadata with current track, see
		ClockTime dur = playbin.queryDuration();
		int duration = (int) (dur.getSeconds() +
				(dur.getMinutes() * 60) +
				(dur.getHours() * 3600));
		synchronized(mdLock) {
			for (MetadataServer mds : mdServers)
				mds.update(artist, title, album, duration, type);
		}
		/*
		System.out.println("             ¨Updating Icecast¨");
		System.out.println("              ¨¨¨¨¨¨¨¨§¨¨¨¨¨¨¨");*/
	}

	private String getDateTime() {
		// Simple date format :)
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public class Shotgun {
		public void fire() {
			quit();
		}
	}
}