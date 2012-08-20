package info.cerios.electrocraft.core.blocks;

import java.io.IOException;
import java.util.ArrayList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockComputer extends BlockNetwork {

    public BlockComputer(int id) {
        super(id, 40, Material.wood);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[side];
    }
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving) {
    	super.onBlockPlacedBy(world, x, y, z, entityliving);
    	
    	if (world.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
    		TileEntityComputer computerTileEntity = (TileEntityComputer) world.getBlockTileEntity(x, y, z);
    		int direction = MathHelper.floor_double((double)((entityliving.rotationYaw * 4F) / 360F) + 0.5D) & 3;
    		computerTileEntity.setDirection(ForgeDirection.values()[direction]);
    	}
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9)) {
            return true;
        }

        if (player instanceof EntityPlayerMP) {
        	if (world.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
        		TileEntityComputer computerTileEntity = (TileEntityComputer) world.getBlockTileEntity(x, y, z);
        		computerTileEntity.setActivePlayer(player);
        		if (computerTileEntity != null) {
        			computerTileEntity.setActivePlayer(player);
        			if (computerTileEntity.getComputer() == null)
        				computerTileEntity.createComputer();
        			if (!computerTileEntity.getComputer().isRunning())
        				computerTileEntity.startComputer();
        			ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).setComputer(computerTileEntity);
        	        ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).sendTerminalSize();
        			GuiPacket guiPacket = new GuiPacket();
        			guiPacket.setCloseWindow(false);
        			guiPacket.setGui(Gui.COMPUTER_SCREEN);
        			try {
        				PacketDispatcher.sendPacketToPlayer(guiPacket.getMCPacket(), (Player) player);
        			} catch (IOException e) {
        				FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to send \"Open Computer GUI Packet\"!");
        			}
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
