import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.net.URLEncoder;

public class MetadataServer {
	private boolean ssl;
	private String hostname, user, pass;
	private int port;
	private ArrayList<String> mount;

	public MetadataServer(boolean ssl, String hostname, int port, String mount,
			String user, String pass) {
		// Create mount array
		this.mount = new ArrayList<String>();

		this.ssl = ssl;
		this.hostname = hostname;
		this.port = port;
		this.mount.add(mount);
		this.user = user;
		this.pass = pass;
	}

	public void addMount(String mount) {
		this.mount.add(mount);
	}

	public void update(String artist, String title, String album, int seconds,
			String type) {
		/*
		 * Lets prevent null's
		 */

		if (artist == null)
			artist = "Ukjent";
		if (title == null)
			title = "Ukjent";
		if (album == null)
			album = "Ukjent";
		if (type == null)
			type = "Ukjent";

		/*
		 * URLencode all input data
		 */
		try {
			artist = URLEncoder.encode(artist, "UTF-8");
			title = URLEncoder.encode(title, "UTF-8");
			album = URLEncoder.encode(album, "UTF-8");
			type = URLEncoder.encode(type, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (String m : mount)
			new Updater(m, artist, title, album, seconds, type).start();
		
		System.gc();
	}
	
	private class Updater extends Thread {
		private String mount;
		private String artist;
		private String title;
		private String album;
		private int seconds;
		private String type;
		
		public Updater(String mount, String artist, String title, String album,
				int seconds, String type) {
			super();
			this.mount = mount;
			this.artist = artist;
			this.title = title;
			this.album = album;
			this.seconds = seconds;
			this.type = type;
		}
		
		@Override
		public void run() {
			String request = "?mount=" + mount
			+ "&mode=updinfo&charset=UTF-8&song=" + artist + "+-+"
			+ title + "&artist=" + artist + "&title=" + title
			+ "&album=" + album + "&duration=" + seconds + "&type="
			+ type;
			URL url = null;
			try {
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(user, pass
								.toCharArray());
					}
				});
				url = new URL("http" + (ssl ? "s" : "") + "://" + hostname
						+ ":" + port + "/admin/metadata" + request);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			/*
			 * Reads and outputs content of url
			 */
			BufferedReader in = null;
			String inputLine;
			try {
				in = new BufferedReader(new InputStreamReader(url.openStream()));
				while ((inputLine = in.readLine()) != null)
					System.out.println(inputLine);
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
