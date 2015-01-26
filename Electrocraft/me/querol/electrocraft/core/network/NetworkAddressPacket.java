package me.querol.electrocraft.core.network;

import me.querol.electrocraft.core.ElectroCraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import me.querol.electrocraft.api.computer.NetworkBlock;
import me.querol.electrocraft.core.ElectroCraft;
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

    public void setLocation(int world, BlockPos pos) {
        this.world = world;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
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

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public IMessage onMessage(NetworkAddressPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ElectroCraft.electroCraftSided.openNetworkGui(message);
        } else {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(message.getWorldId());
            TileEntity tileEntity = world.getTileEntity(message.getBlockPos());
            if (tileEntity instanceof NetworkBlock) {
                ((NetworkBlock) tileEntity).setControlAddress(message.getControlAddress());
                ((NetworkBlock) tileEntity).setDataAddress(message.getDataAddress());
            }
        }
        return null;
    }
}
