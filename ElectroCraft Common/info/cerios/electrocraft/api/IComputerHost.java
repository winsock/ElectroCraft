package info.cerios.electrocraft.api;

import net.minecraft.src.EntityPlayer;
import info.cerios.electrocraft.core.computer.Computer;

public interface IComputerHost {
	public Computer getComputer();
    public void removeActivePlayer(EntityPlayer player);
}
