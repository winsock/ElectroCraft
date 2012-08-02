package info.cerios.electrocraft;

import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import info.cerios.electrocraft.core.IMinecraftMethods;
import info.cerios.electrocraft.core.network.GuiPacket;

public class MinecraftMethods implements IMinecraftMethods {

	@Override
	public TileEntity getBlockTileEntity(int x, int y, int z) {
		// TODO XXX FIXME MULTI_WORLD SUPPORT
		return FMLServerHandler.instance().getServer().getWorldManager(0).getBlockTileEntity(x, y, z);
	}

	@Override
	public void closeGui(Object... optionalPlayers) {
		for (Object o : optionalPlayers) {
			if (o instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) o;
				// Close out of the computer monitor screen
				GuiPacket packet = new GuiPacket();
				packet.setCloseWindow(true);
				try {
					player.playerNetServerHandler.sendPacket(packet.getMCPacket());
				} catch (IOException e) {
					FMLCommonHandler.instance().getFMLLogger().fine("ElectroCraft: Unable to send a close Gui packet!");
				}
			}
		}
	}
}
