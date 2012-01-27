/*  
	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 Import Nirvana, i used eclipse to manage all the .jar's i needed.
 Might be major bugs inside this god-au-full code, but hey, it atleast has comments :)
 To get it to work you need to change the icecast credentials under playing, or comment it out.
 Nice reminder, i exported it to a runnable jar to package all the required jars.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

//Require jna-3.2.4, gstreamer-java-1.4
import org.gstreamer.*;
import org.gstreamer.elements.PlayBin;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;


public class main {

	// init counters for playlist managment
	static int PlayCount = 0;
	static int PromoCount = 0;
	static int Promo = 0;

	public static String Artist, Album, Title, Length;

	public static void main(String[] args) {
		Properties configFile = new Properties();

		/*
		 * Laster inn crawler.properties slik at vi slipper å ha login definert
		 * i koden
		 */
		try {
			configFile.load(new FileReader(new File("nsrmp.properties")));
		} catch (IOException e) {
			System.err.println("Could not open nsrmp.properties. The file must be in the current working directory!");
			e.printStackTrace();
			System.exit(0);
		}

		/*
		 * An array with all the metadata Servers
		 */
		ArrayList<mdServer> mdServers = new ArrayList<mdServer>();

		//Start with Server 0
		int mdServerCounter = 0;
		while ((configFile.getProperty("icecastServer" + mdServerCounter)) != null) {
			mdServers.add(
				new mdServer(
					Boolean.parseBoolean(configFile.getProperty("icecastServer" + mdServerCounter + "Ssl")),
					configFile.getProperty("icecastServer" + mdServerCounter),
					Integer.parseInt(configFile.getProperty("icecastServer"	+ mdServerCounter + "Port")),
					configFile.getProperty("icecastServer" + mdServerCounter + "Mount0"),
					configFile.getProperty("icecastServer" + mdServerCounter + "User"),
					configFile.getProperty("icecastServer" + mdServerCounter + "Password")
				)
			);
			//If there is more than 1 mount point
			int mountCounter = 1;
			while ((configFile.getProperty("icecastServer" + mdServerCounter + "Mount" + mountCounter)) != null){
				mdServers.get(mdServerCounter).addMount(configFile.getProperty("icecastServer" + mdServerCounter
									+ "Mount" + mountCounter));
				mountCounter++;
			}
			mdServerCounter++;
		}

		/*
		 * An array with all the content cc
		 */
		ArrayList<contentCollection> contentCC = new ArrayList<contentCollection>();

		//Start with cc 0
		int contentCCCounter = 0;
		while ((configFile.getProperty("content" + contentCCCounter + "Location")) != null) {
			contentCC.add(new contentCollection(configFile.getProperty("content" + mdServerCounter + "Location"),
					configFile.getProperty("content" + mdServerCounter + "Type")));
			contentCCCounter++;
		}
		
		/*
		 * An array with all the spot cc
		 */
		ArrayList<contentCollection> spotCC = new ArrayList<contentCollection>();

		//Start with cc 0
		int spotCCCounter = 0;
		while ((configFile.getProperty("content" + spotCCCounter + "Location")) != null) {
			contentCC.add(new contentCollection(configFile.getProperty("content" + mdServerCounter + "Location"),
					configFile.getProperty("content" + mdServerCounter + "Type")));
			spotCCCounter++;
		}

		// Basic gstreamer stuff
		args = Gst.init("BusMessages", args);
		final PlayBin playbin = new PlayBin("BusMessages");
		playbin.setVideoSink(ElementFactory.make("fakesink", "videosink"));

		playbin.getBus().connect(new Bus.EOS() {
			public void endOfStream(GstObject source) {
				// Debug
				// System.out.println("Finished playing file");
				Gst.quit();
			}
		});

		playbin.getBus().connect(new Bus.ERROR() {
			public void errorMessage(GstObject source, int code, String message) {
				// Debug
				// System.out.println("Error occurred: " + message);
				Gst.quit();
			}
		});

		playbin.getBus().connect(new Bus.STATE_CHANGED() {
			public void stateChanged(GstObject source, State old,
					State current, State pending) {
				if (source == playbin) {
					// Debug
					// System.out.println("Pipeline state changed from " + old +
					// " to " + current);

					// The PLAYING LUMP, here we do alot of stuff when the music
					// starts playing
					if (current == State.PLAYING) {
						String Seconds = null;
						// Add a "0" to seconds, instead of 3:3, we get 3:30
						if (playbin.queryDuration().getSeconds() < 10) {
							Seconds = "0"
									+ playbin.queryDuration().getSeconds();
						} else {
							Seconds = "" + playbin.queryDuration().getSeconds();
						}
						// Get Duration, date
						Length = playbin.queryDuration().getMinutes() + ":"
								+ Seconds;
						String CurrentDateTime = getDateTime();
						// Displays Status of current song playing
						System.out
								.println("...................Playing....................");
						System.out.println("Title: " + Title);
						System.out.println("Artist: " + Artist);
						System.out.println("Album: " + Album);
						System.out.println("Length: " + Length);

						BufferedWriter writer;
						try {
							// Prints to a logfile called log.txt, see getDir()
							writer = new BufferedWriter(new FileWriter(
									getDir("Log"), true));

							writer.write(CurrentDateTime + " - " + Artist
									+ " - " + Album + " - " + Title + " - "
									+ Length);
							writer.newLine();
							writer.close();
							System.out.println("............." + getDir("Log")
									+ "..............");
							System.out.println("        ....|"
									+ CurrentDateTime + "|....");

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						try {
							// Update Icecast metadata with current track, see
							int duration = (int) (playbin.queryDuration().getSeconds() +
									(playbin.queryDuration().getMinutes() * 60) +
									(playbin.queryDuration().getHours() * 3600));
							for (mdServer mds : mdServers)
								mds.update(Artist, Title, Album, duration, type*);
							String Song = Artist + " - " + Title;
							System.out
									.println("             ¨Updating Icecast¨");
							System.out
									.println("              ¨¨¨¨¨¨¨¨§¨¨¨¨¨¨¨");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out
									.println("        ¨¨Problems Updating Icecast¨¨");
							System.out
									.println("          ¨¨¨¨¨¨¨¨¨¨¨¨§¨¨¨¨¨¨¨¨¨¨¨¨");
						}
					}
				}
			}
		});
		// Gets current tags of the files
		playbin.getBus().connect(new Bus.TAG() {
			public void tagsFound(GstObject source, TagList tagList) {
				for (String tagName : tagList.getTagNames()) {
					if (tagName.equalsIgnoreCase("artist")) {
						Artist = (String) tagList.getValues(tagName).get(0);
					} else if (tagName.equalsIgnoreCase("album")) {
						Album = (String) tagList.getValues(tagName).get(0);
					} else if (tagName.equalsIgnoreCase("title")) {
						Title = (String) tagList.getValues(tagName).get(0);
					}
				}
			}
		});

		// Playloop, loads music from array, plays x-times++, play promo++ after
		// x-times, when list has ended, shuffle again and reset counter
		while (true) {
			args = Gst.init("BusMessages", args);
			playbin.setInputFile(new File(music.get(PlayCount)));
			playbin.setState(State.PLAYING);
			Gst.main();
			playbin.setState(State.NULL);
			Gst.deinit();
			PlayCount++;
			Promo++;

			if (PlayCount > music.size() - 1) {
				PlayCount = 0;
				Collections.shuffle(music);
			}

			if (Promo == 2) {
				args = Gst.init("BusMessages", args);
				playbin.setInputFile(new File(promos.get(PromoCount)));
				playbin.setState(State.PLAYING);
				Gst.main();
				playbin.setState(State.NULL);
				Gst.deinit();
				PromoCount++;
				Promo = 0;
			}
			if (PromoCount > promos.size() - 1) {
				PromoCount = 0;
				Collections.shuffle(promos);
			}
		}
	}

	public static String getDateTime() {
		// Simple date format :)
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static String getDir(String dir) {
		// Returns folders for current dir.
		String NewDir = System.getProperty("user.dir");
		if (dir == "Music") {
			NewDir = NewDir + "/Music";
		}
		if (dir == "Promo") {
			NewDir = NewDir + "/Promo";
		}
		if (dir == "Log") {
			NewDir = NewDir + "/log.txt";
		}
		return NewDir;
	}
}
