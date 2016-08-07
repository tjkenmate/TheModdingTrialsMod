package com.trials.modsquad.block.machines;

import com.trials.modsquad.Ref;
import com.trials.modsquad.block.TileEntities.TileCapacitor;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCapacitor extends Block {

    public BlockCapacitor (String name, String reg) {
        super(Material.IRON);
        setUnlocalizedName(name);
        setRegistryName(reg);
        setCreativeTab(Ref.tabModSquad);
        setHarvestLevel("pickaxe", 1);
        setResistance(30F);
        setHardness(5F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(worldIn.getTileEntity(pos) == null || playerIn.isSneaking()) return false;
        TileEntity tileentity = worldIn.getTileEntity(pos);
        long power = tileentity.getCapability(TeslaCapabilities.CAPABILITY_HOLDER, EnumFacing.DOWN).getStoredPower();
        long cap = tileentity.getCapability(TeslaCapabilities.CAPABILITY_HOLDER, EnumFacing.DOWN).getCapacity();
        playerIn.addChatMessage(new TextComponentString("Power: " + power + "/" + cap));
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileCapacitor();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }
}
