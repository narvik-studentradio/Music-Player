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
	
	public String getType()
	{
		return typeName;
	}
	

}
