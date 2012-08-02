package info.cerios.electrocraft.core;

import net.minecraft.src.TileEntity;

public interface IMinecraftMethods {
	public TileEntity getBlockTileEntity(int x, int y, int z);
	public void closeGui(Object... optionalPlayers);
}
