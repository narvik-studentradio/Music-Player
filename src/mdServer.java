import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class mdServer {
	private boolean ssl;
	private String hostname, user, pass;
	private int port;
	private ArrayList<String> mount;

	public mdServer(boolean ssl, String hostname, int port, String mount,
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

	public void update(String Artist, String Title, String Album, int seconds,
			String type) {
		for (String m : mount) {
			String request = "?mount=" + m + "&mode=updinfo&song=" + Artist
					+ " - " + Title + "&artist=" + Artist + "&title=" + Title
					+ "&album=" + Album + "&duration=" + seconds + "&type="
					+ type;
			URL url = null;
			try {
				url = new URL("http" + (ssl ? "s" : "") + "://" + user + ";"
						+ pass + "@" + hostname + ":" + port + "/"
						+ "/admin/metadata" + request);
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
		System.gc();
	}
}
