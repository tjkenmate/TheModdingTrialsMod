package com.trials.modsquad.block.tile;

import com.trials.modsquad.ModSquad;
import com.trials.net.TileDataSync;
import com.trials.net.Updatable;
import net.darkhax.tesla.api.implementation.BaseTeslaContainer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.darkhax.tesla.lib.TeslaUtils;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

import static net.darkhax.tesla.capability.TeslaCapabilities.CAPABILITY_CONSUMER;
import static net.darkhax.tesla.capability.TeslaCapabilities.CAPABILITY_HOLDER;
import static net.darkhax.tesla.capability.TeslaCapabilities.CAPABILITY_PRODUCER;

public class TileSolarPanel extends TileEntity implements net.minecraft.util.ITickable, Updatable {

    private BaseTeslaContainer solarContainer;

    public TileSolarPanel(){
        solarContainer = new BaseTeslaContainer(0, 10000, 20, 20);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound t = new NBTTagCompound();
        t = writeToNBT(t);
        return new SPacketUpdateTileEntity(pos, 0, t);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Container", solarContainer.serializeNBT());
        compound = super.writeToNBT(compound);
        if(pos!=null){
            int dim = 0;
            for(int i : DimensionManager.getIDs())
                if(DimensionManager.getWorld(i).equals(worldObj)) {
                    dim = i;
                    break;
                }
            ModSquad.channel.sendToAll(new TileDataSync(pos, compound.toString(), dim));
        }
        return compound;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
        super.onDataPacket(net, pkt);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("Container")) solarContainer.deserializeNBT((NBTTagCompound) compound.getTag("Container"));
    }
    private int firstfewTicks = 500;

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onEntityJoinEvent(EntityJoinWorldEvent event){
        firstfewTicks = 0;
    }

    @Override
    public void update() {
        if(firstfewTicks>=10 && firstfewTicks!=500 && !worldObj.isRemote){
            if(pos!=null){
                int dim = 0;
                for(int i : DimensionManager.getIDs())
                    if(DimensionManager.getWorld(i).equals(worldObj)) {
                        dim = i;
                        break;
                    }
                ModSquad.channel.sendToAll(new TileDataSync(pos, serializeNBT().toString(), dim));
            }
            firstfewTicks=500;
        }else if(firstfewTicks!=500) ++firstfewTicks;
        if(solarContainer.getStoredPower()>0) {
            int i = TeslaUtils.getConnectedCapabilities(CAPABILITY_CONSUMER, worldObj, pos).size();
            if(i!=0)
                solarContainer.takePower(TeslaUtils.distributePowerToAllFaces(worldObj, pos, Math.min(solarContainer.getStoredPower() / i, solarContainer.getOutputRate()), false), false);
        }
        // Increase internal power supply
        if (worldObj.getTopSolidOrLiquidBlock(pos).getY() > pos.getY() && solarContainer.getStoredPower() < solarContainer.getCapacity() && worldObj.isDaytime()
                && !worldObj.isRaining()) solarContainer.givePower(Math.min(solarContainer.getCapacity()-solarContainer.getStoredPower(), 5), false); // Fills as much as possible
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) { return capability == TeslaCapabilities.CAPABILITY_PRODUCER || capability == CAPABILITY_HOLDER; }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability==CAPABILITY_HOLDER || capability==CAPABILITY_PRODUCER) //noinspection unchecked
            return (T) solarContainer;
        return super.getCapability(capability, facing);
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.hasKey("Container")) solarContainer.deserializeNBT((NBTTagCompound) compound.getTag("Container"));
    }

    @Override
    public void update(String s) {
        try {
            deserializeNBT(JsonToNBT.getTagFromJson(s));
        } catch (NBTException e) {
            e.printStackTrace();
        }
    }

}
