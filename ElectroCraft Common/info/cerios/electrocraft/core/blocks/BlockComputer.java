package info.cerios.electrocraft.core.blocks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;

public class BlockComputer extends BlockNetwork {

	public BlockComputer(int id) {
		super(id, 33, Material.wood);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		if (metadata > 5)
			metadata = 5;
		if (side > 5)
			side = 5;

		if (side == 0) {
			return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[1];
		}

		switch(metadata) {
		case 4:
			switch (side) {
			case 2:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[0];
			case 3:
			case 4:
			case 5:
			default:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[1];
			}
		default:
		case 2:
			switch (side) {
			case 3:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[0];
			case 2:
			case 4:
			case 5:
			default:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[1];
			}
		case 3:
			switch (side) {
			case 4:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[0];
			case 2:
			case 3:
			case 5:
			default:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[1];
			}
		case 1:
			switch (side) {
			case 5:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[0];
			case 2:
			case 3:
			case 4:
			default:
				return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[1];
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving) {
		super.onBlockPlacedBy(world, x, y, z, entityliving);

		if (world.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
			TileEntityComputer computerTileEntity = (TileEntityComputer) world.getBlockTileEntity(x, y, z);
			int direction = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F + 0.5D)) & 0x3;
			if (direction == 0) {
				direction = 4;
			}
			world.setBlockMetadataWithNotify(x, y, z, direction);
			computerTileEntity.setDirection(ForgeDirection.values()[direction]);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		if (world.isRemote)
			return;
		if (world.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
			TileEntityComputer computerTileEntity = (TileEntityComputer) world.getBlockTileEntity(x, y, z);
			if (computerTileEntity != null) {
				if (computerTileEntity.getComputer() != null) {
					if (computerTileEntity.getComputer().isRunning())
						computerTileEntity.stopComputer();
					if (!computerTileEntity.getComputer().getBaseDirectory().delete() && ConfigHandler.getCurrentConfig().get(Configuration.CATEGORY_GENERAL, "deleteFiles", true).getBoolean(true)) {
						try {
							Utils.deleteRecursive(computerTileEntity.getComputer().getBaseDirectory());
						} catch (FileNotFoundException e) {
							ElectroCraft.instance.getLogger().severe("Unable to delete removed computers files! Path: " + computerTileEntity.getComputer().getBaseDirectory().getAbsolutePath());
						}
					}
				}
			}
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9)) {
			return true;
		}

		if (player instanceof EntityPlayerMP) {
			if (world.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
				TileEntityComputer computerTileEntity = (TileEntityComputer) world.getBlockTileEntity(x, y, z);
				if (computerTileEntity != null) {
					if (computerTileEntity.getComputer() == null)
						computerTileEntity.createComputer();
					if (!computerTileEntity.getComputer().isRunning())
						computerTileEntity.startComputer();
					ElectroCraft.instance.setComputerForPlayer(player, computerTileEntity);
					GuiPacket guiPacket = new GuiPacket();
					guiPacket.setCloseWindow(false);
					guiPacket.setGui(Gui.COMPUTER_SCREEN);
					try {
						PacketDispatcher.sendPacketToPlayer(guiPacket.getMCPacket(), (Player) player);
					} catch (IOException e) {
						ElectroCraft.instance.getLogger().severe("Unable to send \"Open Computer GUI Packet\"!");
					}
					computerTileEntity.addActivePlayer(player);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileEntityComputer();
	}

	@Override
	public void addCreativeItems(ArrayList itemList) { 
		itemList.add(this);
	}
}
