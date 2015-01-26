package me.querol.electrocraft.core.blocks.tileentities;

import me.querol.electrocraft.api.computer.NetworkBlock;
import me.querol.electrocraft.core.computer.Computer;

public class TileEntitySerialCable extends NetworkBlock {

    public TileEntitySerialCable() {
    }

    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }

    @Override
    public void tick(Computer computer) {
    }
}
