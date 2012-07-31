package info.cerios.electrocraft;

import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.src.TileEntity;
import info.cerios.electrocraft.core.IMinecraftMethods;

public class MinecraftMethods implements IMinecraftMethods {

	@Override
	public TileEntity getBlockTileEntity(int x, int y, int z) {
		// TODO XXX FIXME MULTI_WORLD SUPPORT
		return FMLServerHandler.instance().getServer().getWorldManager(0).getBlockTileEntity(x, y, z);
	}

}
