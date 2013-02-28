/**
 * 
 */
package no.samfunnet.nsr.nsrmp.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christoffer
 *
 */
@XmlRootElement(name="metadata")
public class Metadata {
	public String server;
	public int port;
	public String user;
	public String password;
	public boolean ssl;
	public List<String> mount = new ArrayList<String>();
	
	Metadata() {
	}
}
