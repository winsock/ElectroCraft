package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.ElectroCraftSidedServer;
import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.api.utils.Utils.ChangedBytes;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class UniversialPacketHandler implements IPacketHandler {
	private byte[] lastVGAData;

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		if (!packet.channel.equalsIgnoreCase("electrocraft"))
			return;
		if (player instanceof EntityPlayerMP) {
			// Must be a server sided packet
			try {
				ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
				if (ecPacket.getType() == Type.ADDRESS) {
					NetworkAddressPacket addressPacket = (NetworkAddressPacket)ecPacket;
					World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(addressPacket.getWorldId());
					TileEntity tileEntity = world.getBlockTileEntity(addressPacket.getX(), addressPacket.getY(), addressPacket.getZ());
					if(tileEntity instanceof NetworkBlock) {
						((NetworkBlock)tileEntity).setControlAddress(addressPacket.getControlAddress());
						((NetworkBlock)tileEntity).setDataAddress(addressPacket.getDataAddress());
					}
				} else if (ecPacket.getType() == Type.INPUT) {
					ComputerInputPacket inputPacket = (ComputerInputPacket)ecPacket;
					if (ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player) != null)
						if (inputPacket.wasKeyDown()) {
							ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getKeyboard().keyPressed((byte) inputPacket.getEventKey());
						} else {
							ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getKeyboard().keyReleased((byte) inputPacket.getEventKey());
						}
				} else if (ecPacket.getType() == Type.GUI) {
					GuiPacket guiPacket = (GuiPacket)ecPacket;
					if (guiPacket.getGui() == Gui.COMPUTER_SCREEN) {
						if (ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player) != null)
							ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).removeActivePlayer((EntityPlayer) player);
					}
				} else if (ecPacket.getType() == Type.CUSTOM) {
					CustomPacket customPacket = (CustomPacket)ecPacket;
					if (customPacket.id == 1) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(out);
						dos.writeInt((int) ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().getDisplaySize().getWidth());
						dos.writeInt((int) ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().getDisplaySize().getHeight());

						ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().prepareUpdate();
						ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().updateDisplay();
						byte[] vgadata = ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().getBytes();

						if (lastVGAData == null) {
							lastVGAData = vgadata;
							out.write(0);
							dos.writeInt(vgadata.length);
							byte[] compressedData = Utils.compressBytes(vgadata);
							dos.writeInt(compressedData.length);
							out.write(compressedData);
						} else {						
							ChangedBytes current = null;
							List<ChangedBytes> changedBytes = new ArrayList<ChangedBytes>();

							int lastOffset = 0;
							int totalLength = 0;
							while (current == null ? true : current.length > 0) {
								if (current == null) {
									current = Utils.getNextBlock(0, vgadata, lastVGAData);
								} else {
									current = Utils.getNextBlock(lastOffset + current.length, vgadata, lastVGAData);
								}
								lastOffset = current.offset;
								totalLength += current.length;
								changedBytes.add(current);
							}

							out.write(1);
							dos.writeInt(totalLength);

							for (ChangedBytes changedByte : changedBytes) {
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
						returnPacket.id = customPacket.id;
						returnPacket.data = out.toByteArray();
						manager.addToSendQueue(returnPacket.getMCPacket());
					} else if (customPacket.id == 3) {
						if (customPacket.data[0] == 0) {
							if (ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player) != null && ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer() != null) {
								ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().shutdown();
							}
						}
					} else if (customPacket.id == 4) {
						ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
						DataInputStream dis = new DataInputStream(bis);
						int entity = dis.readInt();
						Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer)player).worldObj);
						if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
							EntityDrone drone = (EntityDrone)possibleEntity;
							CustomPacket response = new CustomPacket();
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(bos);
							NBTTagCompound inventory = new NBTTagCompound();
							drone.getInventory().writeToNBT(inventory);
							try {
								dos.writeInt(drone.entityId);
								NBTBase.writeNamedTag(inventory, dos);
								response.id = 4;
								response.data = bos.toByteArray();
								manager.addToSendQueue(response.getMCPacket());
							} catch (IOException e) {
								ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
							}
						}
					} else if (customPacket.id == 7) {
						ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
						DataInputStream dis = new DataInputStream(bis);
						int entity = dis.readInt();
						Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer)player).worldObj);
						if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
							EntityDrone drone = (EntityDrone)possibleEntity;
							CustomPacket response = new CustomPacket();
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(bos);
							NBTTagCompound inventory = new NBTTagCompound();
							drone.getInventory().writeToNBT(inventory);
							try {
								dos.writeInt(drone.entityId);
								dos.writeBoolean(((drone.getDrone() == null) ? false : drone.getDrone().getFlying()));
								NBTBase.writeNamedTag(inventory, dos);
								response.id = 7;
								response.data = bos.toByteArray();
								manager.addToSendQueue(response.getMCPacket());
							} catch (IOException e) {
								ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
							}
						}
					}
				}
			} catch (Exception e) {
				ElectroCraft.instance.getLogger().severe("Unable to parse packet sent on our channel!");
				e.printStackTrace();
			}
		} else {
			// Must be a client sided packet
			try {
				ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
				if (ecPacket.getType() == Type.GUI) {
					GuiPacket guiPacket = (GuiPacket) ecPacket;
					if (guiPacket.closeWindow())
						ElectroCraft.instance.electroCraftSided.closeGui();
					else if (guiPacket.getGui() == Gui.COMPUTER_SCREEN) {
						ElectroCraft.electroCraftSided.openComputerGui();
					}
				} else if (ecPacket.getType() == Type.ADDRESS) {
					NetworkAddressPacket networkPacket = (NetworkAddressPacket) ecPacket;
					ElectroCraft.electroCraftSided.openNetworkGui(networkPacket);
				} else if (ecPacket.getType() == Type.CUSTOM) {
					CustomPacket customPacket = (CustomPacket)ecPacket;
					if (customPacket.id == 4) {
						ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
						DataInputStream dis = new DataInputStream(bis);
						int entity = dis.readInt();
						NBTTagCompound inventory = (NBTTagCompound) NBTBase.readNamedTag(dis);
						Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer)player).worldObj);
						if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
							((EntityDrone) possibleEntity).getInventory().readFromNBT(inventory);
						}
					} else if (customPacket.id == 5) {
						ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
						DataInputStream dis = new DataInputStream(bis);
						int entity = dis.readInt();
						int rotationTicks = dis.readInt();
						Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer)player).worldObj);
						if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
							((EntityDrone) possibleEntity).setRotationTicks(rotationTicks);
						}
					} else if (customPacket.id == 6) {
						ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
						DataInputStream dis = new DataInputStream(bis);
						int entity = dis.readInt();
						boolean flying = dis.readBoolean();
						Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer)player).worldObj);
						if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
							((EntityDrone) possibleEntity).setClientFlying(flying);
						}
					} else if (customPacket.id == 7) {
						ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
						DataInputStream dis = new DataInputStream(bis);
						int entity = dis.readInt();
						boolean flying = dis.readBoolean();
						NBTTagCompound inventory = (NBTTagCompound) NBTBase.readNamedTag(dis);
						Entity possibleEntity = ElectroCraft.instance.getEntityByID(entity, ((EntityPlayer)player).worldObj);
						if (possibleEntity != null && possibleEntity instanceof EntityDrone) {
							((EntityDrone) possibleEntity).getInventory().readFromNBT(inventory);
							((EntityDrone) possibleEntity).setClientFlying(flying);
						}
					} else {
						ElectroCraft.electroCraftSided.handleClientCustomPacket(customPacket);
					}
				}
			} catch (Exception e) {
				ElectroCraft.instance.getLogger().severe("Unable to read packet sent on our channel!");
				e.printStackTrace();
			}
		}
	}
}