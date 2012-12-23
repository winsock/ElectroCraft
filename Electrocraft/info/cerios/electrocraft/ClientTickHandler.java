package info.cerios.electrocraft;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ModifierPacket;

import java.io.IOException;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements IScheduledTickHandler {

    private boolean lastShiftState = false;
    private boolean lastCtrlState = false;

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        try {
            // Shift Packet
            if ((lastShiftState != (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) || (lastCtrlState != (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)))) {
                lastShiftState = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                lastCtrlState = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
                ModifierPacket modifierPacket = new ModifierPacket();
                modifierPacket.setModifiers(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT), Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
                FMLClientHandler.instance().getClient().getSendQueue().addToSendQueue(modifierPacket.getMCPacket());
            }
        } catch (IOException e) {
            ElectroCraft.instance.getLogger().severe("Unable to send modifier packet!");
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public String getLabel() {
        return "ElectroCraft Client Tick Handler";
    }

    @Override
    public int nextTickSpacing() {
        return 10;
    }
}
