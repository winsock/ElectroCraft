package info.cerios.electrocraft.core.blocks;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import info.cerios.electrocraft.api.utils.Utils;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import java.io.FileNotFoundException;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

public class BlockComputer extends BlockNetwork {

    public static final PropertyDirection PROPERTYFACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockComputer() {
        super(Material.iron);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        super.onBlockPlaced(worldIn, pos, blockFaceClickedOn, hitX, hitY, hitZ, meta, placer);

        if (worldIn.getTileEntity(pos) instanceof TileEntityComputer) {
            TileEntityComputer computerTileEntity = (TileEntityComputer) worldIn.getTileEntity(pos);
            // Find the quadrant the player is facing, add 0.5 for half a quadrant to get proper round to east/west/north/south, then
            int playerFacingDirection = (placer == null) ? 0 : MathHelper.floor_double((placer.rotationYaw / 90.0F) + 0.5D) & 3;
            EnumFacing enumfacing = EnumFacing.getHorizontal(playerFacingDirection);
            computerTileEntity.setDirection(enumfacing);
            return this.getDefaultState().withProperty(PROPERTYFACING, enumfacing);
        }
        return this.getDefaultState();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote)
            return;
        if (worldIn.getTileEntity(pos) instanceof TileEntityComputer) {
            TileEntityComputer computerTileEntity = (TileEntityComputer) worldIn.getTileEntity(pos);
            if (computerTileEntity != null) {
                if (computerTileEntity.getComputer() != null) {
                    if (computerTileEntity.getComputer().isRunning()) {
                        computerTileEntity.stopComputer();
                    }
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
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ))
            return true;

        if (playerIn instanceof EntityPlayerMP) {
            if (worldIn.getTileEntity(pos) instanceof TileEntityComputer) {
                TileEntityComputer computerTileEntity = (TileEntityComputer) worldIn.getTileEntity(pos);
                if (computerTileEntity != null) {
                    if (computerTileEntity.getComputer() == null) {
                        computerTileEntity.createComputer();
                    }
                    if (!computerTileEntity.getComputer().isRunning()) {
                        computerTileEntity.startComputer();
                    }
                    ElectroCraft.instance.setComputerForPlayer(playerIn, computerTileEntity);
                    GuiPacket guiPacket = new GuiPacket();
                    guiPacket.setCloseWindow(false);
                    guiPacket.setGui(Gui.COMPUTER_SCREEN);
                    ElectroCraft.instance.getNetworkWrapper().sendTo(guiPacket, (EntityPlayerMP) playerIn);
                    computerTileEntity.addActivePlayer(playerIn);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityComputer();
    }
}
