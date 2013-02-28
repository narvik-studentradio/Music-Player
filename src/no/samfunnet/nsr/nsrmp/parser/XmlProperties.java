/**
 * 
 */
package no.samfunnet.nsr.nsrmp.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christoffer
 *
 */
@XmlRootElement(name="properties")
public class XmlProperties {
	@XmlElement
	public List<Metadata> metadata = new ArrayList<Metadata>();
	@XmlElement
	public List<Content> content = new ArrayList<Content>();

	@XmlAttribute
	public int contentPerSpot;
	@XmlAttribute
	public String log;
	@XmlAttribute
	public String broadcastArtist;
	@XmlAttribute
	public String broadcastTitle;
	@XmlAttribute
	public String broadcastAlbum;
	@XmlAttribute
	public String spotType;
	@XmlAttribute
	public String watchType;

	XmlProperties() {
	}
}
