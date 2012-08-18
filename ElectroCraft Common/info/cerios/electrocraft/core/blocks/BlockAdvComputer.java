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
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockAdvComputer extends BlockNetwork {

    public BlockAdvComputer(int id) {
        super(id, 40, Material.iron);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return ElectroBlocks.ADV_COMPUTER.getDefaultTextureIndices()[side];
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
