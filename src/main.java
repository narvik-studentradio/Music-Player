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
import java.util.Date;
import java.util.Scanner;


public class main {
	static Player player = null;

	public static void main(String[] args) {
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
			 * broadcast
			 * start
			 * stop (soft)
			 * skip
			 * metadata
			 * playstream
			 * 	should default to http://stream.sysrq.no:8000/01-greystream.ogg.m3u or
			 * 	http://stream.sysrq.no:8000/01-greystream.ogg
			 * 	The .m3u file consists of a "link" to the .ogg stream
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
			else if(command[0].equalsIgnoreCase("broadcast"))
				broadcastLoop();
			else if(command[0].equalsIgnoreCase("skip"))
				player.skip();
			else if(command[0].equalsIgnoreCase("stop")) {
				if(command.length < 2) {
					System.out.println("soft/hard specification required");
				}
				else if(command[1].equalsIgnoreCase("soft")) {
					System.out.println("Stopping after current song");
					player.pause();
				}
				else if(command[1].equalsIgnoreCase("hard")) {
					System.out.println("Hard stop");
					player.pause();
					player.interrupt();
				}
				else
					System.out.println("Invalid command use \"soft\" or \"hard\"");
			}
			else if(command[0].equalsIgnoreCase("start")) {
				player.play();
				System.out.println("Player started");
			}
		}
		player.shotgun.fire();
	}
	
	public static void broadcastLoop() {
		Scanner scan = new  Scanner(System.in);
		String command = "";
		System.out.println("***Entering boradcast mode***");
		System.out.print("Artist (Narvik Studentradio): ");
		command = scan.nextLine();
		if(command.equals(""))
			command = "Narvik Studentradio";
		String artist = command;
		System.out.println("Title (Mandagssendingen): ");
		command = scan.nextLine();
		if(command.equals(""))
			command = "Mandagssendingen";
		String title = command;
		System.out.println("Album (Live): ");
		command = scan.nextLine();
		if(command.equals(""))
			command = "Live";
		String album = command;
		System.out.println("***Starting broadcast***");
		
		while(true) {
			player.pause();
			player.setMetadata(artist, title, album);
			System.out.println("\r***Player stopped, metadata set, any command for music break, \"q\" to exit***");
			command = scan.nextLine();
			if(command.equalsIgnoreCase("q")) {
				System.out.println("\r***Broadcast ended, normal play will resume***");
				break;
			}
			else {
				player.mode = PlayMode.Music;
				player.play();
				System.out.println("\r***Music break started, any command to end on current song***");
				command = scan.nextLine();
				player.mode = PlayMode.Default;
			}
		}
		player.play();
	}
	
	public static void printHelp() {
		System.out.println("List of commands:");
		System.out.println("rescan - rescan properties file and content folders");
		System.out.println("broadcast - enter broadcast mode");
		System.out.println("skip - skip to next song, not advisable");
		System.out.println("stop soft/hard - stop the player, after current song or immedeately");
		System.out.println("start - start player after it has been stopped");
		System.out.println("quit - kill everything");
	}

	public static String getDateTime() {
		// Simple date format :)
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
