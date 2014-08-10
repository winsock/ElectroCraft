package info.cerios.electrocraft.core.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import info.cerios.electrocraft.core.ElectroCraft;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class GuiPacket extends ElectroPacket implements IMessageHandler<GuiPacket, IMessage> {

    public enum Gui {
        COMPUTER_SCREEN, ADDRESS_SCREEN, DRONE_INVENTORY
    };
    private Gui windowId;
    private boolean closeWindow = false; // When set it closes the current window

    public GuiPacket() {
        type = Type.GUI;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(new byte[] { (byte) windowId.ordinal(), (byte) (closeWindow ? 1 : 0) });
    }

    @Override
    public void fromBytes(ByteBuf data) {
        windowId = Gui.values()[data.readByte()];
        closeWindow = (data.readByte() == 0 ? false : true);
    }

    public void setGui(Gui gui) {
        this.windowId = gui;
    }

    public Gui getGui() {
        return windowId;
    }

    public boolean closeWindow() {
        return closeWindow;
    }

    public void setCloseWindow(boolean close) {
        this.closeWindow = close;
    }

    @Override
    public IMessage onMessage(GuiPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            if (message.closeWindow()) {
                ElectroCraft.electroCraftSided.closeGui();
            } else if (message.getGui() == Gui.COMPUTER_SCREEN) {
                ElectroCraft.electroCraftSided.openComputerGui();
            }
        } else {
            if (message.getGui() == Gui.COMPUTER_SCREEN) {
                if (ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity) != null) {
                    ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity).removeActivePlayer(ctx.getServerHandler().playerEntity);
                }
            }
        }
        return null;
    }
}
