package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.ElectroCraftSidedServer;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.utils.Utils;
import info.cerios.electrocraft.core.utils.Utils.ChangedBytes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class UniversialPacketHandler implements IPacketHandler {
	private byte[] lastVGAData;

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		if (!packet.channel.equalsIgnoreCase("electrocraft"))
			return;
		if (player instanceof EntityPlayerMP) {
			// Must be a server sided packet
			try {
				ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
				if (ecPacket.getType() == Type.MODIFIER) {
					ModifierPacket modifierPacket = (ModifierPacket)ecPacket;
					
					// Set the server shift state
					if (ElectroCraft.electroCraftSided instanceof ElectroCraftSidedServer)
						((ElectroCraftSidedServer)ElectroCraft.electroCraftSided).setShiftState(modifierPacket.isShiftDown());
					
					// Send the modifier packet to the computer if it is a valid computer
					if (ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player) != null && ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer() != null)
						ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getKeyboard().proccessModifierPacket(modifierPacket);
				} else if (ecPacket.getType() == Type.ADDRESS) {
					NetworkAddressPacket addressPacket = (NetworkAddressPacket)ecPacket;
					World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(addressPacket.getWorldId());
					TileEntity tileEntity = world.getBlockTileEntity(addressPacket.getX(), addressPacket.getY(), addressPacket.getZ());
					if(tileEntity instanceof NetworkBlock) {
						((NetworkBlock)tileEntity).setControlAddress(addressPacket.getControlAddress());
						((NetworkBlock)tileEntity).setDataAddress(addressPacket.getDataAddress());
					}
				} else if (ecPacket.getType() == Type.INPUT) {
					ComputerInputPacket inputPacket = (ComputerInputPacket)ecPacket;
					if (inputPacket.wasKeyDown()) {
						ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getKeyboard().onKeyPress(inputPacket);
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
						dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().getWidth());
						dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().getHeight());

						byte[] vgadata = ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getVideoCard().getData();
						
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
	            	} else if (customPacket.id == 2) {
	            		ByteArrayInputStream bis = new ByteArrayInputStream(customPacket.data);
	                    DataInputStream dis = new DataInputStream(bis);
	            		int row = dis.readInt();
	            		ByteArrayOutputStream out = new ByteArrayOutputStream();
	                    DataOutputStream dos = new DataOutputStream(out);
						dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getTerminal().getColumns());
						dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getTerminal().getRows());
						dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getTerminal().getCurrentColumn());
						dos.writeInt(ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getTerminal().getCurrentRow());
						out.write(0); // Terminal packet type 0
						dos.writeInt(row); // Resend the row number
						String rowData = ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().getTerminal().getLine(row);
						if (!rowData.isEmpty()) {
							dos.writeBoolean(true);
							dos.writeUTF(rowData);
						} else {
							dos.writeBoolean(false);
						}
						CustomPacket returnPacket = new CustomPacket();
						returnPacket.id = customPacket.id;
						returnPacket.data = out.toByteArray();
						manager.addToSendQueue(returnPacket.getMCPacket());
	            	} else if (customPacket.id == 3) {
	            		if (customPacket.data[0] == 0) {
	            			if (ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player) != null && ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer() != null) {
	            				ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().postEvent("kill");
	            				ElectroCraft.instance.getComputerForPlayer((EntityPlayer) player).getComputer().setRunning(false);
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
	            } else if (ecPacket.getType() == Type.PORT) {
	                ServerPortPacket portPacket = (ServerPortPacket) ecPacket;
                    ElectroCraft.electroCraftSided.startComputerClient(portPacket.getPort(), manager.getSocketAddress());
	            } else if (ecPacket.getType() == Type.CUSTOM) {
	            	CustomPacket customPacket = (CustomPacket)ecPacket;
	            	ElectroCraft.electroCraftSided.handleClientCustomPacket(customPacket);
	            }
	        } catch (Exception e) {
	            ElectroCraft.instance.getLogger().severe("Unable to read packet sent on our channel!");
	        }
		}
	}
}
