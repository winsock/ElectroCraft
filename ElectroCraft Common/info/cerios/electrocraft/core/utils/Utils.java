package info.cerios.electrocraft.core.utils;

import cpw.mods.fml.common.FMLCommonHandler;

import info.cerios.electrocraft.core.ElectroCraft;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.minecraft.src.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

public class Utils {
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        Set<T> keys = new HashSet<T>();
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static byte[] compressBytes(byte[] data) throws UnsupportedEncodingException, IOException {
        Deflater df = new Deflater();
        df.setLevel(Deflater.BEST_COMPRESSION);
        df.setInput(data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        df.finish();
        byte[] buff = new byte[1024];
        while (!df.finished()) {
            int count = df.deflate(buff);
            bos.write(buff, 0, count);
        }
        bos.close();
        byte[] output = bos.toByteArray();
        return output;
    }

    public static byte[] extractBytes(byte[] input) throws UnsupportedEncodingException, IOException, DataFormatException {
        Inflater ifl = new Inflater();
        ifl.setInput(input);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buff = new byte[1024];
        while (!ifl.finished()) {
            int count = ifl.inflate(buff);
            baos.write(buff, 0, count);
        }
        baos.close();
        byte[] output = baos.toByteArray();
        return output;
    }

    public static void copyResource(String sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        ReadableByteChannel source = null;
        FileChannel destination = null;
        FileOutputStream fileOutput = null;
        InputStream resource = null;

        try {
            resource = Utils.class.getClassLoader().getResourceAsStream(sourceFile);
            source = Channels.newChannel(resource);
            fileOutput = new FileOutputStream(destFile);
            destination = fileOutput.getChannel();
            destination.transferFrom(source, 0, resource.available());
        } finally {
            if (source != null)
                source.close();
            if (destination != null)
                destination.close();
            if (fileOutput != null)
                fileOutput.close();
            if (resource != null)
                resource.close();
        }
    }

    public static String loadUncompiledAssembly(String file) {
        File javaFile = new File(file);
        if (!javaFile.exists()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        try {
            FileReader reader = new FileReader(javaFile);
            while (reader.ready()) {
                buffer.append((char)reader.read());
            }
            reader.close();
        } catch (IOException e) {
            ElectroCraft.instance.getLogger().severe("Error! Unable to open specified ASM file: " + file);
        }
        return  buffer.toString();
    }
    
    public static ForgeDirection isBlockOnOpaqueBlock(IBlockAccess access, int x, int y, int z) {
    	if (access.isBlockOpaqueCube(x + 1, y, z)) {
    		return ForgeDirection.EAST;
    	} else if (access.isBlockOpaqueCube(x - 1, y, z)) {
    		return ForgeDirection.WEST;
    	} else if (access.isBlockOpaqueCube(x, y + 1, z)) {
    		return ForgeDirection.UP;
    	} else if (access.isBlockOpaqueCube(x, y - 1, z)) {
    		return ForgeDirection.DOWN;
    	} else if (access.isBlockOpaqueCube(x, y, z + 1)) {
    		return ForgeDirection.SOUTH;
    	} else if (access.isBlockOpaqueCube(x, y, z - 1)) {
    		return ForgeDirection.NORTH;
    	} else {
    		return ForgeDirection.UNKNOWN;
    	}
    }
        
    /**
     * Checks, whether the child directory is a subdirectory of the base directory.
     * Or a file inside the base directory
     *
     * @param base the base directory.
     * @param child the suspected child directory or file.
     * @return true, if the child is a subdirectory of the base directory, or a file in the base directory
     * @throws IOException if an IOError occurred during the test.
     */
    public static boolean baseDirectoryContains(File base, File child) throws IOException {
        base = base.getCanonicalFile();
        child = child.getCanonicalFile();

        File parentFile = child;
        while (parentFile != null) {
            if (base.equals(parentFile)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }
    

	/**
	 * Compares array1 to array two and gets the ChangedBytes
	 * @param beginOffset
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static ChangedBytes getNextBlock(int beginOffset, byte[] array1, byte[] array2) {
		if (beginOffset >= array1.length || beginOffset >= array2.length) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.length = 0;
			return changedBytes;
		} else if (array1.length <= beginOffset) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.b = Arrays.copyOfRange(array2, beginOffset, array2.length);
			changedBytes.offset = beginOffset;
			changedBytes.length = array2.length - beginOffset;
			return changedBytes;
		} else if (array2.length <= beginOffset) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.length = 0;
			return changedBytes;
		} else if (array2 == null || array2.length <= 0) {
			ChangedBytes changedBytes = new ChangedBytes();
			changedBytes.b = array1;
			changedBytes.offset = beginOffset;
			changedBytes.length = array1.length;
			return changedBytes;
		} else {
			ChangedBytes changedBytes = new ChangedBytes();
			int offset = beginOffset;
			while (array1[offset] == array2[offset]) {
				offset++;
				if (offset >= array1.length || offset >= array2.length) {
					changedBytes.length = 0;
					return changedBytes;
				}
			}
			changedBytes.offset = offset;
			ByteBuffer buffer = ByteBuffer.allocate(array1.length - beginOffset);
			while (array1[offset] != array2[offset]) {
				buffer.put(array1[offset]);
				offset++;
			}
			changedBytes.length = offset - changedBytes.offset;
			changedBytes.b = new byte[changedBytes.length];
			buffer.rewind();
			buffer.get(changedBytes.b, 0, changedBytes.length);
			return changedBytes;
		}
	}
	
	public static class ChangedBytes {
		public byte[] b;
		public int offset;
		public int length;
	}
}
