import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;



public class ContentManager {
	private static final int SCAN_INTERVAL_MS = 10000;
	
	private PropertyParser parser;
	private ContentCollection music;
	private ContentCollection spots;
	private int songsPerSpot;
	private int songNr = 0;
	private int spotNr = 0;
	private int sinceSpot = 0;
	private String type = "";
	private Thread watcher;
	private File watchFolder;
	private List<ScheduledProgram> programs;
	private List<ScheduledProgram> playedPrograms;
	
	public ContentManager(PropertyParser propertyParser) {
		this.parser = propertyParser;
		this.music = parser.getMusic();
		this.spots = parser.getSpots();
		this.songsPerSpot = parser.getSongsPerSpot();
		this.watchFolder = new File("Watch"); //TODO: get from properties
		this.programs = new ArrayList<ScheduledProgram>();
		this.playedPrograms = new ArrayList<ScheduledProgram>();
		
		music.shuffle();
		spots.shuffle();
		
		watcher = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					try {
						scanWatchFolder();
						Thread.sleep(SCAN_INTERVAL_MS);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		watcher.start();
	}
	
	public String getNext() {
		//Check programs
		Calendar now = GregorianCalendar.getInstance();
		ScheduledProgram toPlay = null;
		synchronized(programs) {
			for(ScheduledProgram prog : programs) {
				if(prog.time.getTimeInMillis() < now.getTimeInMillis()) {
					if(toPlay == null)
						toPlay = prog;
					else if(prog.time.getTimeInMillis() < toPlay.time.getTimeInMillis())
						toPlay = prog;
				}
			}
			if(toPlay != null) {
				playedPrograms.add(toPlay);
				programs.remove(toPlay);
				return toPlay.file.toString();
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
	
	private void scanWatchFolder() {
		if(!watchFolder.isDirectory())
			return;
		File[] files = watchFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(!pathname.isFile())
					return false;
				// Correct syntax: yyyy-mm-dd-hh-mm.(ogg/mp3)
				if(pathname.getName().matches("^\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.(ogg|mp3)$"))
					return true;
				return false;
			}
		});
		ArrayList<ScheduledProgram> toPlay = new ArrayList<ScheduledProgram>();
		Calendar now = GregorianCalendar.getInstance();
		for (File file : files) {
			String name = file.getName();
			int year = Integer.parseInt(name.substring(0, 4));
			int month = Integer.parseInt(name.substring(5, 7));
			int day = Integer.parseInt(name.substring(8, 10));
			int hour = Integer.parseInt(name.substring(11, 13));
			int minute = Integer.parseInt(name.substring(14, 16));
			Calendar cal = new GregorianCalendar(year, month, day, hour, minute);
			ScheduledProgram shpr = new ScheduledProgram(cal, file);
			if(cal.getTimeInMillis() - now.getTimeInMillis() > - SCAN_INTERVAL_MS
					&& playedPrograms.contains(shpr))
				toPlay.add(shpr);
		}
		synchronized(programs) {
			programs.clear();
			programs.addAll(toPlay);
		}
	}
	
	private class ScheduledProgram {
		Calendar time;
		File file;
		
		public ScheduledProgram(Calendar time, File file) {
			super();
			this.time = time;
			this.file = file;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ScheduledProgram) {
				if(((ScheduledProgram)obj).time.equals(time)
						&& ((ScheduledProgram)obj).file.equals(file))
					return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return time.hashCode() ^ file.hashCode();
		}
	}
}
