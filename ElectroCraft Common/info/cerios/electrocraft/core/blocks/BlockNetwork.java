package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

import java.io.IOException;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public abstract class BlockNetwork extends ElectroBlock {

	protected BlockNetwork(int id, int textureId, Material material) {
		super(id, textureId, material);
	}

	protected BlockNetwork(int id, Material material) {
		super(id, material);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
			((NetworkBlock) world.getBlockTileEntity(x, y, z))
					.update((NetworkBlock) world.getBlockTileEntity(x, y, z));
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
			NetworkBlock block = (NetworkBlock) world.getBlockTileEntity(x, y,
					z);
			if (block.getComputerNetwork() != null) {
				block.getComputerNetwork().removeDevice(block);
			}
			super.breakBlock(world, x, y, z, par5, par6);
			block.update(block);
		} else {
			super.breakBlock(world, x, y, z, par5, par6);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
			int nBlockId) {
		super.onNeighborBlockChange(world, x, y, z, nBlockId);
		if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
			((NetworkBlock) world.getBlockTileEntity(x, y, z))
					.update((NetworkBlock) world.getBlockTileEntity(x, y, z));
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (player instanceof EntityPlayerMP) {
			if (player.isSneaking()) {
				if (world.getBlockId(x, y, z) == this.blockID) {
					if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
						NetworkBlock block = (NetworkBlock) world
								.getBlockTileEntity(x, y, z);
						NetworkAddressPacket networkAddressPacket = new NetworkAddressPacket();
						networkAddressPacket.setControlAddress(block
								.getControlAddress());
						networkAddressPacket.setDataAddress(block
								.getDataAddress());
						networkAddressPacket.setLocation(
								world.provider.dimensionId, x, y, z);
						try {
							PacketDispatcher.sendPacketToPlayer(
									networkAddressPacket.getMCPacket(),
									(Player) player);
						} catch (IOException e) {
							ElectroCraft.instance
									.getLogger()
									.severe("Unable to send \"Open Address GUI Packet\"!");
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}
