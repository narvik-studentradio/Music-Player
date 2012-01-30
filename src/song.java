import java.io.File;


public class song {
	private File file;
	private String typeName;
	
	public song (File file, String typeName)
	{
		this.file = file;
		this.typeName = typeName;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public String typeName()
	{
		return typeName;
	}
	

}
