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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


public class main {

	// init counters for playlist managment
	static int PlayCount = 0;
	static int PromoCount = 0;
	static int Promo = 0;
	static ArrayList<MetadataServer> mdServers;

	public static String artist, album, title, length, type;

	public static void main(String[] args) {
		Player player = null;
		try {
			player = new Player();
		} catch(IOException ex) {
			System.out.println("Could not read config file.");
			return;
		}
		player.start();
		
		Scanner scan = new Scanner(System.in);
		String[] command;
		while(true) {
			/* Commands:
			 * 
			 * help
			 * rescan
			 * start
			 * stop (soft)
			 * skip
			 * metadata
			 * streammode
			 * playonce
			 * quit
			 * 
			 */
			command = scan.nextLine().split(" +?");
			if(command.length == 0)
				continue;
			else if(command[0].equalsIgnoreCase("help"))
				printHelp();
			else if(command[0].equalsIgnoreCase("rescan")) {
				if(player.rescan())
					System.out.println("Properties and folders re-scanned");
				else
					System.out.println("Unable to re-scan");
			}
			else if(command[0].equalsIgnoreCase("quit")) {
				break;
			}
		}
		player.shotgun.fire();
	}
	
	public static void printHelp() {
		System.out.println("List of commands:");
		System.out.println("rescan - rescan properties file and content folders");
		System.out.println("quit - stop stuff");
	}

	public static String getDateTime() {
		// Simple date format :)
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
