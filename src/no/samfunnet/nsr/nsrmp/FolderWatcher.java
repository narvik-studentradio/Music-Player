package no.samfunnet.nsr.nsrmp;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class FolderWatcher extends Thread {
	private static final int SCAN_INTERVAL_MS = 10000;
	private static final int MAX_PROGRAM_DELAY_MS = 1800000;
	
	private File watchFolder;
	private List<ScheduledProgram> programs;
	private String fileExtensions = "";

	public FolderWatcher(String folder, String[] extensions) throws FileNotFoundException {
		super("Folder watcher");
		this.watchFolder = new File(folder);
		if(!watchFolder.isDirectory())
			throw new FileNotFoundException("Folder not found");
		for(int i=0; i<extensions.length; i++)
			fileExtensions += (i > 0 ? "|" : "") + extensions[i];
		this.programs = new ArrayList<ScheduledProgram>();
		setPriority(MIN_PRIORITY);
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
				programs.remove(toPlay);
				String parent = toPlay.file.getParent();
				String name = toPlay.file.getName();
				String newName = parent + File.separator + "_" + name;
				toPlay.file.renameTo(new File(newName));
				return newName;
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
			try {
				String name = file.getName();
				int year = Integer.parseInt(name.substring(0, 4));
				int month = Integer.parseInt(name.substring(5, 7)) - 1; //because java is stupid
				int day = Integer.parseInt(name.substring(8, 10));
				int hour = Integer.parseInt(name.substring(11, 13));
				int minute = Integer.parseInt(name.substring(14, 16));
				Calendar cal = new GregorianCalendar(year, month, day, hour, minute);
				ScheduledProgram shpr = new ScheduledProgram(cal, file);
				if(now.getTimeInMillis() - cal.getTimeInMillis() < MAX_PROGRAM_DELAY_MS)
					toPlay.add(shpr);
			} catch(NumberFormatException e) {
				continue;
			}
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
			if(file.isFile() && file.getName().matches("^[^_]*\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.(" + fileExtensions + ")$"))
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
