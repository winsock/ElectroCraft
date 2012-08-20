package info.cerios.electrocraft.core.utils;

import cpw.mods.fml.common.FMLCommonHandler;

import info.cerios.electrocraft.core.ElectroCraft;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
}
