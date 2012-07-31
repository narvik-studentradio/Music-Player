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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class main {
	private static Player player = null;
	private static volatile boolean streamDeath = false;

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
			 * playing
			 * rescan
			 * broadcast
			 * playstream
			 * start
			 * stop (soft)
			 * skip
			 * quit
			 * 
			 * TODO:
			 * metadata
			 * playonce
			 * schedule
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
					System.out.println("Invalid command, use \"soft\" or \"hard\"");
			}
			else if(command[0].equalsIgnoreCase("start")) {
				player.play();
				System.out.println("Player started");
			}
			else if(command[0].equals("playing"))
				player.printPlaying();
			else if(command[0].equals("playstream")) {
				streamLoop();
			}
			else
				System.out.println("Unknown command");
		}
		player.shotgun.fire();
	}
	
	public static void streamLoop() {
		final Scanner scan = new Scanner(System.in);
		String command = "null";
		System.out.println("***Entering stream mode***");
		
		URI uri = null;
		while(true) {
			System.out.print("Stream (" + player.getStreamUri().toString() + "): ");
			command = scan.nextLine();
			try {
				uri = new URI(command);
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
			}
			
			if(command.equals(""))
				break;
			if(uri != null) {
				player.setStreamUri(uri);
				break;
			}
		}
		
		streamDeath = false;
		Player.ErrorListener errorListener = new Player.ErrorListener() {
			@Override
			public void onError(Player.PlayerError error) {
				streamDeath = true;
			}
		};
		player.addErrorListener(errorListener);
		
		player.play();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		player.mode = PlayMode.Stream;
		System.out.println("Stream mode enabled after current song, \"q\" to quit");
		StreamLoop: do {
			try {
				while(!br.ready()) {
					try { Thread.sleep(200); } catch (InterruptedException e) {}
					if(streamDeath) {
						System.err.println("***Stream error, resuming normal play***");
						break StreamLoop;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			command = scan.nextLine();
		} while(!command.equals("q"));
		
		player.removeErrorListener(errorListener);
		player.mode = PlayMode.Default;
		player.skip();
		System.out.println("***Stream mode ended***");
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
		System.out.println("playing - print currently playing song");
		System.out.println("rescan - rescan properties file and content folders");
		System.out.println("broadcast - enter broadcast mode");
		System.out.println("playstream - play a stream");
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
