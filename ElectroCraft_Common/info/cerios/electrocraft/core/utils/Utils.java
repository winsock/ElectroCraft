package info.cerios.electrocraft.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
	
	public static byte[] compressBytes(byte[] data) throws UnsupportedEncodingException, IOException
    {
        Deflater df = new Deflater();
        df.setLevel(Deflater.BEST_COMPRESSION);
        df.setInput(data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        df.finish();
        byte[] buff = new byte[1024];
        while(!df.finished())
        {
            int count = df.deflate(buff);
            bos.write(buff, 0, count);
        }
        bos.close();
        byte[] output = bos.toByteArray();
        return output;
    }
    
    public static byte[] extractBytes(byte[] input) throws UnsupportedEncodingException, IOException, DataFormatException
    {
        Inflater ifl = new Inflater();
        ifl.setInput(input);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buff = new byte[1024];
        while(!ifl.finished())
        {
            int count = ifl.inflate(buff);
            baos.write(buff, 0, count);
        }
        baos.close();
        byte[] output = baos.toByteArray();
        return output;
    }
	
	public static void copyResource(String sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
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
	    }
	    finally {
	        if(source != null)
	            source.close();
	        if(destination != null)
	            destination.close();
	        if (fileOutput != null)
	        	fileOutput.close();
	        if (resource != null)
	        	resource.close();
	    }
	}
}
