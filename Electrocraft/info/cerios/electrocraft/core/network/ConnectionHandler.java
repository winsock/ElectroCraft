package info.cerios.electrocraft.core.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;

import net.minecraft.entity.player.EntityPlayerMP;

public class ConnectionHandler {
    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ConfigHandler.getCurrentConfig().get("general", "useMCServer", false).getBoolean(false)) {
            ServerPortPacket portPacket = new ServerPortPacket();
            portPacket.setPort(ElectroCraft.instance.getServer().getPort());
            ElectroCraft.instance.getNetworkWrapper().sendTo(portPacket, (EntityPlayerMP) event.player);
        }
    }
}
