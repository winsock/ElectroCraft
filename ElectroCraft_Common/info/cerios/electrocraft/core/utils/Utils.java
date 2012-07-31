package info.cerios.electrocraft.core.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
}
