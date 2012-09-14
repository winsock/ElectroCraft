/*
 * $Id: NativeSupport.java 121 2012-01-22 01:40:14Z andre@naef.com $
 * See LICENSE.txt for license terms.
 */

package com.naef.jnlua;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Loads the JNLua native library.
 * 
 * The class provides and configures a default loader implementation that loads
 * the JNLua native library by means of the <code>System.loadLibrary</code>
 * method. In some situations, you may want to override this behavior. For
 * example, when using JNLua as an OSGi bundle, the native library is loaded by
 * the OSGi runtime. Therefore, the OSGi bundle activator replaces the loader by
 * a no-op implementaion. Note that the loader must be configured before
 * LuaState is accessed.
 */
public final class NativeSupport {
	// -- Static
	private static final NativeSupport INSTANCE = new NativeSupport();

	// -- State
	private Loader loader = new DefaultLoader();

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static NativeSupport getInstance() {
		return INSTANCE;
	}

	// -- Construction
	/**
	 * Private constructor to prevent external instantiation.
	 */
	private NativeSupport() {
	}

	// -- Properties
	/**
	 * Return the native library loader.
	 * 
	 * @return the loader
	 */
	public Loader getLoader() {
		return loader;
	}

	/**
	 * Sets the native library loader.
	 * 
	 * @param loader
	 *            the loader
	 */
	public void setLoader(Loader loader) {
		if (loader == null) {
			throw new NullPointerException("loader must not be null");
		}
		this.loader = loader;
	}

	// -- Member types
	/**
	 * Loads the library.
	 */
	public interface Loader {
		public void load();
	}

	private class DefaultLoader implements Loader {
		@Override
		public void load() {
			String osName = System.getProperty("os.name");
	        String fileExtention = ".so"; // Default to the linux/unix library
	        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
	        	if (System.getProperty("os.arch").contains("86")) {
		            fileExtention = "32.dll";
	        	} else {
		            fileExtention = "64.dll";
	        	}
	        } else if (System.getProperty("os.name").toUpperCase().indexOf("LINUX") > -1) {
	            fileExtention = ".so";
	        } else if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1) {
	            fileExtention = ".dylib";
	        }

	        File libFolder = new File(ElectroCraft.electroCraftSided.getBaseDir(), "electrocraft" + File.separator + "natives");
	        if (!libFolder.exists())
	            libFolder.mkdirs();

	        File libraryFile = new File(libFolder, "libElectroCraftCPU" + fileExtention);
	        try {
                Utils.copyResource("info/cerios/electrocraft/core/natives/libElectroCraftCPU" + fileExtention, libraryFile);
            } catch (IOException e) {
            	ElectroCraft.instance.getLogger().severe("Error copying computer library! Computers will not work!");
            }

	        try {
	            System.load(libFolder.getAbsolutePath() + File.separator + "libElectroCraftCPU" + fileExtention);
	        } catch (Exception e) {
	        	ElectroCraft.instance.getLogger().severe("Error loading computer library! Computers will not work!");
	        }
		}
	}
}