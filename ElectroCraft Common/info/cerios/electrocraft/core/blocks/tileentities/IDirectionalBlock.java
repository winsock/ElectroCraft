package info.cerios.electrocraft.core.blocks.tileentities;

import net.minecraftforge.common.ForgeDirection;

public interface IDirectionalBlock {
	public void setDirection(ForgeDirection direction);

	public ForgeDirection getDirection();
}
