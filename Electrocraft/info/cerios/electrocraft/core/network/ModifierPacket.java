package info.cerios.electrocraft.core.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import info.cerios.electrocraft.ElectroCraftSidedServer;
import info.cerios.electrocraft.core.ElectroCraft;
import io.netty.buffer.ByteBuf;

public class ModifierPacket extends ElectroPacket implements IMessageHandler<ModifierPacket, IMessage> {

    private boolean isShiftDown = false, isCtrlDown = false;

    public ModifierPacket() {
        type = Type.MODIFIER;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(new byte[] { (byte) (isShiftDown ? 1 : 0), (byte) (isCtrlDown ? 1 : 0) });
    }

    @Override
    public void fromBytes(ByteBuf data) {
        isShiftDown = (data.readByte() == 0 ? false : true);
        isCtrlDown = (data.readByte() == 0 ? false : true);
    }

    public void setModifiers(boolean shift, boolean ctrl) {
        this.isCtrlDown = ctrl;
        this.isShiftDown = shift;
    }

    public boolean isShiftDown() {
        return this.isShiftDown;
    }

    public boolean isCtrlDown() {
        return this.isCtrlDown;
    }

    @Override
    public IMessage onMessage(ModifierPacket message, MessageContext ctx) {
        // Set the server shift state
        if (ElectroCraft.electroCraftSided instanceof ElectroCraftSidedServer) {
            ((ElectroCraftSidedServer) ElectroCraft.electroCraftSided).setShiftState(message.isShiftDown());
        }

        // Send the modifier packet to the computer if it is a valid computer
        if (ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity) != null && ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity).getComputer() != null) {
            ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity).getComputer().getKeyboard().proccessModifierPacket(message);
        }
        return null;
    }
}
