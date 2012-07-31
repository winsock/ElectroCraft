package info.cerios.electrocraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.ModLoader;

/**
 * A packet that contains uniform data that can be serialized
 * @author andrewquerol
 *
 * @param <T extends Serializable> The type of data
 */
public class ElectroPacket<T extends Serializable> {
	
	public Map<String, T> dataMap = new HashMap<String, T>();
	
	public byte[] getData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);   
		for (String key : dataMap.keySet()) {
			out.writeChars(key);
			out.writeObject(dataMap.get(key));
		}
		out.close();
		bos.close();
		return bos.toByteArray();
	}
	
	public void readData(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInput in = new ObjectInputStream(bis);
		String currentKey;
		while((currentKey = in.readLine()) != null) {
			Object object = in.readObject();
			try {
				dataMap.put(currentKey, (T)object);
			} catch (ClassCastException e) {
				ModLoader.getLogger().severe("ElectroCraft: Unable to read data from packet!");
			}
		}
	}
	
	public T getType(String id, T defaultValue) {
		if (defaultValue.getClass().isAssignableFrom(dataMap.get(id).getClass())) {
			return (T)dataMap.get(id);
		}
		return defaultValue;
	}
	
	public void addData(String id, T object) {
		dataMap.put(id, object);
	}
}
