package info.cerios.electrocraft.core.drone;

import java.util.List;

import net.minecraft.src.EntityPlayer;
import info.cerios.electrocraft.core.computer.Computer;

public class Drone extends Computer {

	
	
	public Drone(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		super(clients, script, baseDirectory, isInternal, width, height, rows, columns);
	}

	
}
