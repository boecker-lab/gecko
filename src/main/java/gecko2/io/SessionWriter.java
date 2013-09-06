package gecko2.io;

import gecko2.GeckoInstance;

import java.io.*;


/**
 * The class implements a writer which write a gecko session to a file.
 * The code of this file is exported from GeckoInstance.java and modified.
 * 
 * @author Hans-Martin Haase <hans-martin dot haase at uni-jena dot de>
 * @version 0.03
 */
public class SessionWriter 
{
	/**
	 * Saves the current gecko session to a given file
	 * @param f The file to write to
	 */
	public static boolean saveSessionToFile(File f) 
	{
		GeckoInstance.getInstance().setLastSavedFile(f);
        ObjectOutputStream o = null;
        boolean returnValue = true;
		try 
		{
			o = new ObjectOutputStream(new FileOutputStream(f));
			o.writeObject(GeckoInstance.getInstance().getGenomes());
			o.writeObject(GeckoInstance.getInstance().getGenLabelMap());
			o.writeObject(GeckoInstance.getInstance().getColormap());
			o.writeObject(GeckoInstance.getInstance().getClusters());
			o.writeObject(GeckoInstance.getInstance().getMaxIdLength());
			o.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
            returnValue = false;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
            returnValue = false;
		}
		finally 
		{
			try 
			{
				if (o!=null)
				{
					o.close();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				returnValue = false;
			}
		}
        return returnValue;
	}
}
