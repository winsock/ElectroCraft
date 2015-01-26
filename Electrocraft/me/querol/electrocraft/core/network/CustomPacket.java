package me.querol.electrocraft.core.network;

import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.entites.EntityDrone;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import me.querol.electrocraft.api.utils.Utils;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.entites.EntityDrone;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomPacket extends ElectroPacket implements IMessageHandler<CustomPacket, CustomPacket> {
    public int id = 0;
    public int length = 0;
    public byte[] data;
    private static byte[] lastVGAData = null;

    public CustomPacket() {
        type = Type.CUSTOM;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        id = data.readInt();
        length = data.readInt();
        this.data = new byte[length];
        int count = 0;
        while (count < length) {
            int readable = data.readableBytes();
            data.readBytes(this.data, count, readable);
            count += readable;
        }
    }

    @Override
    public CustomPacket onMessage(CustomPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            AbstractClientPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
            try {
                if (message.id == 4) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int entity = dis.readInt();
                    NBTTagCompound inventory = CompressedStreamTools.read(dis);
                    Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, (player).worldObj);
                    if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
                        ((EntityDrone) possibleEntity).getInventoryInterface().readFromNBT(inventory);
                    }
                } else if (message.id == 5) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int entity = dis.readInt();
                    int rotationTicks = dis.readInt();
                    Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, (player).worldObj);
                    if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
                        ((EntityDrone) possibleEntity).setRotationTicks(rotationTicks);
                    }
                } else if (message.id == 6) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int entity = dis.readInt();
                    boolean flying = dis.readBoolean();
                    Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, (player).worldObj);
                    if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
                        ((EntityDrone) possibleEntity).setClientFlying(flying);
                    }
                } else if (message.id == 7) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int entity = dis.readInt();
                    boolean flying = dis.readBoolean();
                    NBTTagCompound inventory = CompressedStreamTools.read(dis);
                    Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, (player).worldObj);
                    if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
                        ((EntityDrone) possibleEntity).getInventoryInterface().readFromNBT(inventory);
                        ((EntityDrone) possibleEntity).setClientFlying(flying);
                    }
                } else {
                    ElectroCraft.electroCraftSided.handleClientCustomPacket(message);
                }
            } catch (Exception ignored) {
            }
        } else {
            try {
                if (message.id == 1) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    dos.writeInt(ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity).getComputer().getVideoCard().getWidth());
                    dos.writeInt(ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity).getComputer().getVideoCard().getHeight());

                    byte[] vgadata = ElectroCraft.instance.getComputerForPlayer(ctx.getServerHandler().playerEntity).getComputer().getVideoCard().getData();

                    if (lastVGAData == null) {
                        lastVGAData = vgadata;
                        out.write(0);
                        byte[] compressedData = Utils.compressBytes(vgadata);
                        dos.writeInt(compressedData.length);
                        out.write(compressedData);
                    } else {
                        Utils.ChangedBytes current = null;
                        List<Utils.ChangedBytes> changedBytes = new ArrayList<Utils.ChangedBytes>();

                        int lastOffset = 0;
                        while (current == null ? true : current.length > 0) {
                            if (current == null) {
                                current = Utils.getNextBlock(0, vgadata, lastVGAData);
                            } else {
                                current = Utils.getNextBlock(lastOffset + current.length, vgadata, lastVGAData);
                            }
                            lastOffset = current.offset;
                            changedBytes.add(current);
                        }

                        out.write(1);
                        for (Utils.ChangedBytes changedByte : changedBytes) {
                            if (changedByte.length > 0) {
                                byte[] compressedData = Utils.compressBytes(changedByte.b);
                                dos.writeInt(compressedData.length);
                                dos.writeInt(changedByte.offset);
                                out.write(compressedData);
                            }
                        }
                    }
                    lastVGAData = vgadata;
                    CustomPacket returnPacket = new CustomPacket();
                    returnPacket.id = message.id;
                    returnPacket.data = out.toByteArray();
                    return returnPacket;
                }else if (message.id == 2) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int row = dis.readInt();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer().getTerminal().getColumns());
                    dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer().getTerminal().getRows());
                    dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer().getTerminal().getCurrentColumn());
                    dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer().getTerminal().getCurrentRow());
                    out.write(0); // Terminal packet type 0
                    dos.writeInt(row); // Resend the row number
                    String rowData = ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer().getTerminal().getLine(row);
                    if (!rowData.isEmpty()) {
                        dos.writeBoolean(true);
                        dos.writeUTF(rowData);
                    } else {
                        dos.writeBoolean(false);
                    }
                    CustomPacket returnPacket = new CustomPacket();
                    returnPacket.id = message.id;
                    returnPacket.data = out.toByteArray();
                    return returnPacket;
                } else if (message.id == 3) {
                    if (message.data[0] == 0) {
                        if (ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity) != null && ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer() != null) {
                            ElectroCraft.instance.getComputerForPlayer((EntityPlayer) ctx.getServerHandler().playerEntity).getComputer().shutdown();
                        }
                    }
                } else if (message.id == 4) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int entity = dis.readInt();
                    Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer) ctx.getServerHandler().playerEntity).worldObj);
                    if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
                        EntityDrone drone = (EntityDrone) possibleEntity;
                        CustomPacket response = new CustomPacket();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        NBTTagCompound inventory = new NBTTagCompound();
                        drone.getInventoryInterface().writeToNBT(inventory);
                        try {
                            dos.writeInt(drone.getEntityId());
                            CompressedStreamTools.write(inventory, dos);
                            response.id = 4;
                            response.data = bos.toByteArray();
                            return response;
                        } catch (IOException e) {
                            ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
                        }
                    }
                } else if (message.id == 7) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.data);
                    DataInputStream dis = new DataInputStream(bis);
                    int entity = dis.readInt();
                    Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer) ctx.getServerHandler().playerEntity).worldObj);
                    if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
                        EntityDrone drone = (EntityDrone) possibleEntity;
                        CustomPacket response = new CustomPacket();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(bos);
                        NBTTagCompound inventory = new NBTTagCompound();
                        drone.getInventoryInterface().writeToNBT(inventory);
                        try {
                            dos.writeInt(drone.getEntityId());
                            dos.writeBoolean(((drone.getDrone() == null) ? false : drone.getDrone().getFlying()));
                            CompressedStreamTools.write(inventory, dos);
                            response.id = 7;
                            response.data = bos.toByteArray();
                            return response;
                        } catch (IOException e) {
                            ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
                        }
                    }
                }
            }  catch (Exception ignored) {
            }
        }
        return null;
    }
}
