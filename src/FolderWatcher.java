import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class FolderWatcher extends Thread {
	private static final int SCAN_INTERVAL_MS = 10000;
	
	private File watchFolder;
	private List<ScheduledProgram> programs;
	private List<ScheduledProgram> playedPrograms;

	public FolderWatcher(String folder) throws FileNotFoundException {
		super("Folder watcher");
		this.watchFolder = new File(folder);
		if(!watchFolder.isDirectory())
			throw new FileNotFoundException("Folder not found");
		this.programs = new ArrayList<ScheduledProgram>();
		this.playedPrograms = new ArrayList<ScheduledProgram>();
	}

	@Override
	public void run() {
		try {
			while(!Thread.interrupted()) {
				scanWatchFolder();
				Thread.sleep(SCAN_INTERVAL_MS);
			}
		} catch(InterruptedException e) {
		}
	}
	
	public String toPlay() {
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
		return null;
	}
	
	private void scanWatchFolder() {
		if(!watchFolder.isDirectory())
			return;
		ArrayList<File> files = getFiles(watchFolder);
		
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
	
	private ArrayList<File> getFiles(File folder) {
		if(!folder.isDirectory())
			return new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();
		for(File file : folder.listFiles()) {
			if(file.isFile() && file.getName().matches("^\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.(ogg|mp3|wav)$"))
				files.add(file);
			else if(file.isDirectory())
				files.addAll(getFiles(file));
		}
		return files;
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
