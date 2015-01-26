package me.querol.electrocraft.api;

import me.querol.electrocraft.core.computer.Computer;
import net.minecraft.entity.player.EntityPlayer;

public interface IComputerHost {
    public Computer getComputer();

    public void removeActivePlayer(EntityPlayer player);
}
