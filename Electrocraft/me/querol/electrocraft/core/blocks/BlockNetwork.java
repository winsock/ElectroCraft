package me.querol.electrocraft.core.blocks;

import me.querol.electrocraft.api.computer.NetworkBlock;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.computer.luaapi.Network;
import me.querol.electrocraft.core.network.NetworkAddressPacket;

import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.network.NetworkAddressPacket;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class BlockNetwork extends ElectroBlock {

    protected BlockNetwork(Material material) {
        super(material);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (worldIn.getTileEntity(pos) instanceof NetworkBlock) {
            NetworkBlock networkBlock = (NetworkBlock) worldIn.getTileEntity(pos);
            networkBlock.update(networkBlock);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.getTileEntity(pos) instanceof NetworkBlock) {
            NetworkBlock block = (NetworkBlock) worldIn.getTileEntity(pos) ;
            if (block.getComputerNetwork() != null) {
                block.getComputerNetwork().removeDevice(block);
            }
            super.breakBlock(worldIn, pos, state);
            block.update(block);
        } else {
            super.breakBlock(worldIn, pos, state);
        }
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
        if (worldIn.getTileEntity(pos) instanceof NetworkBlock) {
            NetworkBlock networkBlock = (NetworkBlock) worldIn.getTileEntity(pos);
            networkBlock.update(networkBlock);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (playerIn instanceof EntityPlayerMP) {
            if (playerIn.isSneaking()) {
                if (Block.getIdFromBlock(state.getBlock()) == Block.getIdFromBlock(this)) {
                    if (worldIn.getTileEntity(pos) instanceof NetworkBlock) {
                        NetworkBlock block = (NetworkBlock) worldIn.getTileEntity(pos);
                        NetworkAddressPacket networkAddressPacket = new NetworkAddressPacket();
                        networkAddressPacket.setControlAddress(block.getControlAddress());
                        networkAddressPacket.setDataAddress(block.getDataAddress());
                        networkAddressPacket.setLocation(worldIn.provider.getDimensionId(), pos);
                        ElectroCraft.instance.getNetworkWrapper().sendTo(networkAddressPacket, (EntityPlayerMP) playerIn);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
