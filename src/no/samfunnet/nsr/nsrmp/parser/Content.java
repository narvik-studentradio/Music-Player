/**
 * 
 */
package no.samfunnet.nsr.nsrmp.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Christoffer
 *
 */
@XmlRootElement(name="content")
public class Content {
	public String location;
	public String type;
	@XmlElement(name="filetype")
	public List<String> filetypes = new ArrayList<String>();
	
	Content() {
	}
}
