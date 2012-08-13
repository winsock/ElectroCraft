package info.cerios.electrocraft;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ShiftPacket;

import java.io.IOException;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements IScheduledTickHandler {
	
    private boolean lastShiftState = false;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
        ElectroCraft.instance.getComputerHandler().update();
		try {
            if (!FMLClientHandler.instance().getClient().isSingleplayer()) {
                // Shift Packet
                if (lastShiftState != (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
                    lastShiftState = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                    ShiftPacket shiftPacket = new ShiftPacket();
                    shiftPacket.setShiftState(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
                    FMLClientHandler.instance().getClient().getSendQueue().addToSendQueue(shiftPacket.getMCPacket());
                }
            }
        } catch (IOException e) {
            FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to send packet!");
        }
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
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
