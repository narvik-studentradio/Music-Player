/*
 * This file is part of nsr-mp.
 * 
 * nsr-mp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * nsr-mp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with nsr-mp.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Import Nirvana, i used eclipse to manage all the .jar's i needed.
 * Might be major bugs inside this god-au-full code, but hey, it atleast has comments :)
 * To get it to work you need to change the icecast credentials under playing, or comment it out.
 * Nice reminder, i exported it to a runnable jar to package all the required jars.
 */

import java.io.BufferedWriter;
import java.io.Console;
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

import java.util.Date;
import java.util.Properties;
import java.util.Scanner;


public class main {

	// init counters for playlist managment
	static int PlayCount = 0;
	static int PromoCount = 0;
	static int Promo = 0;
	static ArrayList<MetadataServer> mdServers;
	static Properties configFile;

	public static String artist, album, title, length, type;

	public static void main(String[] args) {
		configFile = new Properties();

		/*
		 * Laster inn crawler.properties slik at vi slipper ha alt hardkodet
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
		mdServers = new ArrayList<MetadataServer>();

		//Start with Server 0
		int mdServerCounter = 0;
		while ((configFile.getProperty("icecastServer" + mdServerCounter)) != null) {
			mdServers.add(
				new MetadataServer(
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
		 * contentCollection with all the music
		 */
		ContentCollection music = new ContentCollection();

		//Start with musicType 0
		int contentTypeCounter = 0;
		while ((configFile.getProperty("content" + contentTypeCounter + "Location")) != null) {
			music.scan(configFile.getProperty("content" + contentTypeCounter + "Location"),
					configFile.getProperty("content" + contentTypeCounter + "Name"));
			contentTypeCounter++;
		}
		music.shuffle();
		
		/*
		 * contentCollection with all the spots
		 */
		ContentCollection spots = new ContentCollection();

		//Start with spotType 0
		int spotTypeCounter = 0;
		while ((configFile.getProperty("spot" + spotTypeCounter + "Location")) != null) {
			spots.scan(configFile.getProperty("spot" + spotTypeCounter + "Location"),
					configFile.getProperty("spot" + spotTypeCounter + "Name"));
			spotTypeCounter++;
		}
		spots.shuffle();
		
		Player player = new Player(music, spots, mdServers, configFile);
		player.start();
		
		Scanner scan = new Scanner(System.in);
		String command;
		while(true) {
			command = scan.nextLine();
		}
	}

	public static String getDateTime() {
		// Simple date format :)
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
