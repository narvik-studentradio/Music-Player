import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.gstreamer.Bus;
import org.gstreamer.ClockTime;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.TagList;
import org.gstreamer.elements.PlayBin2;


public class Player extends Thread implements Bus.EOS, Bus.ERROR, Bus.STATE_CHANGED, Bus.TAG {
	private ContentCollection music;
	private ContentCollection spots;
	private List<MetadataServer> mdServers;
	private Properties properties;
	private PlayBin2 playbin;
	private String artist, album, title, length, type;
	private int playCount = 0;
	private int promoCount = 0;
	private int promo = 0;
	
	public Player(ContentCollection music, ContentCollection spots, List<MetadataServer> mdServers, Properties properties) {
		super();
		this.music = music;
		this.spots = spots;
		this.mdServers = mdServers;
		this.properties = properties;
		
		Gst.init("BusMessages", new String[]{});
		playbin = new PlayBin2("BusMessages");
		playbin.setVideoSink(ElementFactory.make("fakesink", "videosink"));
		Bus bus = playbin.getBus();
		bus.connect((Bus.EOS)this);
		bus.connect((Bus.ERROR)this);
		bus.connect((Bus.STATE_CHANGED)this);
		bus.connect((Bus.TAG)this);
	}

	@Override
	public void run() {
		// Playloop, loads music from array, plays x-times++, play promo++ after
		// x-times, when list has ended, shuffle again and reset counter
		while(true) {
			Gst.init("BusMessages", new String[]{});
			playbin.setInputFile(new File(music.getFile(playCount)));
			type = music.getType(playCount);
			playbin.setState(org.gstreamer.State.PLAYING);
			Gst.main();
			playbin.setState(org.gstreamer.State.NULL);
			Gst.deinit();
			playCount++;
			promo++;

			if (playCount > music.size() - 1) {
				playCount = 0;
				music.shuffle();
			}

			if (promo == Integer.parseInt(properties.getProperty("contentNo"))) {
				Gst.init("BusMessages", new String[]{});
				playbin.setInputFile(new File(spots.getFile(promoCount)));
				type = spots.getType(promoCount);
				playbin.setState(org.gstreamer.State.PLAYING);
				Gst.main();
				playbin.setState(org.gstreamer.State.NULL);
				Gst.deinit();
				promoCount++;
				promo = 0;
			}
			if (promoCount > spots.size() - 1) {
				promoCount = 0;
				spots.shuffle();
			}
		}
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
		printPlaying();
		printLog();
		updateMetadata();
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
	
	private void printPlaying() {
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
			writer = new BufferedWriter(new FileWriter(properties.getProperty("logDir"), true));

			writer.write(CurrentDateTime + " - " + artist
					+ " - " + album + " - " + title + " - "
					+ length);
			writer.newLine();
			writer.close();
			
			System.out.println("............." + properties.getProperty("logDir") + "..............");
			System.out.println("        ....|" + CurrentDateTime + "|....");
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
		for (MetadataServer mds : mdServers)
			mds.update(artist, title, album, duration, type);
		System.out.println("             ¨Updating Icecast¨");
		System.out.println("              ¨¨¨¨¨¨¨¨§¨¨¨¨¨¨¨");
	}

	private String getDateTime() {
		// Simple date format :)
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
