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
		/*
		 * Lets prevent null's
		 */
		
		if (Artist == null)
			Artist = "Ukjent";
		if (Title == null)
			Title = "Ukjent";
		if (Album == null)
			Album = "Ukjent";
		if (type == null)
			type = "Ukjent";
		
		/*
		 * URLencode all input data
		 */
		try {
			Artist = URLEncoder.encode(Artist, "UTF-8");
			Title = URLEncoder.encode(Title, "UTF-8");
			Album = URLEncoder.encode(Album, "UTF-8");
			type = URLEncoder.encode(type, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (String m : mount) {
			String request = "?mount=" + m + "&mode=updinfo&song=" + Artist
					+ "+-+" + Title + "&artist=" + Artist + "&title=" + Title
					+ "&album=" + Album + "&duration=" + seconds + "&type="
					+ type;
			URL url = null;
			try {
				Authenticator.setDefault(new Authenticator() {
				    @Override
				    protected PasswordAuthentication getPasswordAuthentication() {
				        return new PasswordAuthentication(
				            user,
				            pass.toCharArray());
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
		System.gc();
	}
}
