package info.cerios.electrocraft.core.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRedstoneAdapter;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRedstoneAdapter extends BlockNetwork {


    public BlockRedstoneAdapter() {
        super(Material.rock);
        this.setBlockBounds(0f, 0f, 0f, 1.0f, 0.55f, 1.0f);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        return isProvidingStrongPower(worldIn, pos, state, side);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityRedstoneAdapter) {
            TileEntityRedstoneAdapter adapter = (TileEntityRedstoneAdapter) worldIn.getTileEntity(pos);
            return adapter.getRedstonePower() ? 1 :0;
        }
        return 0;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock)  {
        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
        if (worldIn.getTileEntity(pos) instanceof TileEntityRedstoneAdapter) {
            TileEntityRedstoneAdapter adapter = (TileEntityRedstoneAdapter) worldIn.getTileEntity(pos);
            if ((worldIn.isBlockIndirectlyGettingPowered(pos) != 0) != adapter.getState()) {
                adapter.setExternalState(worldIn.isBlockIndirectlyGettingPowered(pos) != 0);
            }
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRedstoneAdapter();
    }
}
