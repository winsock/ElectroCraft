package info.cerios.electrocraft.core.blocks.tileentities;

import net.minecraftforge.common.util.ForgeDirection;

public interface IDirectionalBlock {
    public void setDirection(ForgeDirection direction);

    public ForgeDirection getDirection();
}
