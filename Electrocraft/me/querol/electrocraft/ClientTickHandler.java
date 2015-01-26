package me.querol.electrocraft;

import me.querol.electrocraft.core.network.ModifierPacket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.network.ModifierPacket;

import java.io.IOException;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraftforge.fml.client.FMLClientHandler;

public class ClientTickHandler {

    private boolean lastShiftState = false;
    private boolean lastCtrlState = false;

    @SubscribeEvent
    public void onTick(TickEvent tick) {
        if (tick.phase == TickEvent.Phase.START && tick.type == TickEvent.Type.PLAYER) {
            // Shift Packet
            if ((lastShiftState != (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) || (lastCtrlState != (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)))) {
                lastShiftState = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                lastCtrlState = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                ModifierPacket modifierPacket = new ModifierPacket();
                modifierPacket.setModifiers(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
                ElectroCraft.instance.getNetworkWrapper().sendToServer(modifierPacket);
            }
        }
    }
}
