package info.cerios.electrocraft.core;

import net.minecraft.src.EntityPlayer;
import info.cerios.electrocraft.core.computer.Computer;

public interface IComputer {
	public Computer getComputer();
    public void removeActivePlayer(EntityPlayer player);
}
