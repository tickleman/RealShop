package fr.crafter.tickleman.RealPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

//####################################################################################### RealTools
public class RealTools
{

	//---------------------------------------------------------------------------- extractDefaultFile
	public static void extractDefaultFile(RealPlugin plugin, String name)
	{
		File actual = new File(plugin.getDataFolder() + "/" + name);
		if (!actual.exists()) {
			InputStream input = plugin.getClass().getResourceAsStream("/default/" + name);
			if (input != null) {
				FileOutputStream output = null;
				try {
					output = new FileOutputStream(actual);
					byte[] buf = new byte[8192];
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}
					plugin.log.info("Default file written: " + name);
				} catch (Exception e) {
					e.printStackTrace();
					try { if (input != null) input.close(); } catch (Exception localException1) {}
					try { if (output != null) output.close(); } catch (Exception localException2) {}
				} finally {
					try { if (input != null) input.close(); } catch (Exception localException3) {}
					try { if (output != null) output.close(); } catch (Exception localException4) {}
				}
			}
		}
	}

	// ----------------------------------------------------------------------------------- fileExists
	public static boolean fileExists(String fileName)
	{
		return (new File(fileName)).exists();
	}

	// ---------------------------------------------------------------------------------------- mkDir
	// mkDir
	public static void mkDir(String dirName) {
		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	// ----------------------------------------------------------------------------------- renameFile
	// renameFile
	public static void renameFile(String fromFile, String toFile) {
		File from = new File(fromFile);
		File to = new File(toFile);
		if (from.exists() && !to.exists()) {
			from.renameTo(to);
		}
	}

}
