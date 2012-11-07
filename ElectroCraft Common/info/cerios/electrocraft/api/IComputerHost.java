package info.cerios.electrocraft.api;

import info.cerios.electrocraft.core.computer.Computer;
import net.minecraft.src.EntityPlayer;

public interface IComputerHost {
	public Computer getComputer();

	public void removeActivePlayer(EntityPlayer player);
}
