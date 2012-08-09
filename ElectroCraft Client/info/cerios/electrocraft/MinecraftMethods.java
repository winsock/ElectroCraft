package info.cerios.electrocraft;

import cpw.mods.fml.client.FMLClientHandler;
import info.cerios.electrocraft.core.IMinecraftMethods;
import net.minecraft.src.TileEntity;

public class MinecraftMethods implements IMinecraftMethods {

    @Override
    public TileEntity getBlockTileEntity(int x, int y, int z) {
        return FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(x, y, z);
    }

    @Override
    public void closeGui(Object... optionalPlayers) {
        FMLClientHandler.instance().getClient().displayGuiScreen(null);
    }
}
