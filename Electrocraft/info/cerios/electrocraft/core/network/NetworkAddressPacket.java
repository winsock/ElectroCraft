package info.cerios.electrocraft.core.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class NetworkAddressPacket extends ElectroPacket implements IMessageHandler<NetworkAddressPacket, IMessage> {

    private int dataAddress;
    private int controlAddress;
    private int world, x, y, z;

    public NetworkAddressPacket() {
        type = Type.ADDRESS;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(world);
        buf.writeInt(dataAddress);
        buf.writeInt(controlAddress);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        x = data.readInt();
        y = data.readInt();
        z = data.readInt();
        world = data.readInt();
        dataAddress = data.readInt();
        controlAddress = data.readInt();
    }

    public int getDataAddress() {
        return dataAddress;
    }

    public void setDataAddress(int dataAddress) {
        this.dataAddress = dataAddress;
    }

    public int getControlAddress() {
        return controlAddress;
    }

    public void setControlAddress(int controlAddress) {
        this.controlAddress = controlAddress;
    }

    public void setLocation(int world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getWorldId() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public IMessage onMessage(NetworkAddressPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ElectroCraft.electroCraftSided.openNetworkGui(message);
        } else {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.getWorldId());
            TileEntity tileEntity = world.getTileEntity(message.getX(), message.getY(), message.getZ());
            if (tileEntity instanceof NetworkBlock) {
                ((NetworkBlock) tileEntity).setControlAddress(message.getControlAddress());
                ((NetworkBlock) tileEntity).setDataAddress(message.getDataAddress());
            }
        }
        return null;
    }
}
