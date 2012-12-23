package info.cerios.electrocraft.core.drone.tools;

import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.core.entites.EntityDrone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SwordTool implements IDroneTool {

    @Override
    public boolean isRightTool(ItemStack item) {
        return item != null && item.getItem() instanceof ItemSword;
    }

    @Override
    public boolean appliesToBlock(ItemStack item, Block block, int metadata) {
        return true;
    }

    @Override
    public List<ItemStack> preformAction(ItemStack item, EntityDrone drone, World world, int x, int y, int z) {
        List<ItemStack> itemsDropped = new ArrayList<ItemStack>();

        Vec3 look = drone.getLookVec();
        // field_82592_a = getVecFromPool
        Vec3 pos = Vec3.vec3dPool.getVecFromPool(drone.posX, drone.posY, drone.posZ);
        Vec3 vector = pos.addVector(look.xCoord * 4, look.yCoord * 4, look.zCoord * 4);

        Entity entity = null;
        List entites = world.getEntitiesWithinAABBExcludingEntity(drone, drone.boundingBox.addCoord(look.xCoord, look.yCoord, look.zCoord).expand(1.0D, 1.0D, 1.0D));
        double distance = 0.0D;
        Iterator entityIterator = entites.iterator();
        while (entityIterator.hasNext()) {
            Entity e = (Entity) entityIterator.next();
            float expandOffset = 0.3F;
            AxisAlignedBB boudingBoxToSearch = e.boundingBox.expand(expandOffset, expandOffset, expandOffset);
            MovingObjectPosition var12 = boudingBoxToSearch.calculateIntercept(pos, vector);
            if (var12 != null) {
                double distanceTo = pos.distanceTo(var12.hitVec);
                if (distanceTo < distance || distance == 0.0D) {
                    entity = e;
                    distance = distanceTo;
                }
            }
        }
        if (entity != null) {
            MovingObjectPosition rayTrace = new MovingObjectPosition(entity);
            if (rayTrace != null && rayTrace.typeOfHit == EnumMovingObjectType.ENTITY && rayTrace.entityHit != null) {
                rayTrace.entityHit.captureDrops = true;
                rayTrace.entityHit.capturedDrops.clear();
                rayTrace.entityHit.attackEntityFrom(DamageSource.causeMobDamage(drone), item.getDamageVsEntity(rayTrace.entityHit));
                item.damageItem(1, drone);
                if (rayTrace.entityHit.capturedDrops.size() > 0) {
                    for (EntityItem i : rayTrace.entityHit.capturedDrops) {
                        itemsDropped.add(i.func_92014_d()); // Get item stack
                        i.setDead();
                    }
                }
                rayTrace.entityHit.captureDrops = false;
            }
        }
        return itemsDropped;
    }

    @Override
    public void damageItem(EntityDrone drone, ItemStack item, Block block, int metadata) {
    }
}
