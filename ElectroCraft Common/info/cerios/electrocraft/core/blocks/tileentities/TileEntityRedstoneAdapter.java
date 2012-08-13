package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.core.computer.NetworkBlock;
import net.minecraft.src.NBTTagCompound;

public class TileEntityRedstoneAdapter extends NetworkBlock {

    private boolean redstonePower = false;
    private boolean receiveMode = false;

    public TileEntityRedstoneAdapter() {
        this.dataAddress = 225;
        this.controlAddress = 226;
    }

    public boolean getRedstonePower() {
        return redstonePower;
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("redstonePower", redstonePower);
        nbttagcompound.setBoolean("receiveMode", receiveMode);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.redstonePower = nbttagcompound.getBoolean("redstonePower");
        this.receiveMode = nbttagcompound.getBoolean("receiveMode");
    }

    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }
}
