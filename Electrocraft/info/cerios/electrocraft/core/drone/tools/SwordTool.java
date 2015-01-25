package info.cerios.electrocraft.core.drone.tools;

import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.core.entites.EntityDrone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SwordTool implements IDroneTool {

    @Override
    public boolean isRightTool(ItemStack item) {
        return item != null && item.getItem() instanceof ItemSword;
    }

    @Override
    public boolean appliesToBlock(IBlockAccess world, ItemStack item, BlockPos blockPos) {
        return true;
    }

    @Override
    public List<ItemStack> preformAction(ItemStack item, EntityDrone drone, World world, BlockPos blockPos) {
        List<ItemStack> itemsDropped = new ArrayList<ItemStack>();

        Vec3 look = drone.getLookVec();
        Vec3 pos = new Vec3(drone.posX, drone.posY, drone.posZ);
        Vec3 vector = pos.addVector(look.xCoord * 4, look.yCoord * 4, look.zCoord * 4);

        Entity entity = null;
        List nearEntities = world.getEntitiesWithinAABBExcludingEntity(drone, drone.getEntityBoundingBox().addCoord(look.xCoord, look.yCoord, look.zCoord).expand(1.0D, 1.0D, 1.0D));
        double distance = 0.0D;
        for (Object nearEntityObject : nearEntities) {
            Entity nearEntity = (Entity) nearEntityObject;
            float expandOffset = 0.3F;
            AxisAlignedBB boudingBoxToSearch = nearEntity.getEntityBoundingBox().expand(expandOffset, expandOffset, expandOffset);
            MovingObjectPosition var12 = boudingBoxToSearch.calculateIntercept(pos, vector);
            if (var12 != null) {
                double distanceTo = pos.distanceTo(var12.hitVec);
                if (distanceTo < distance || distance == 0.0D) {
                    entity = nearEntity;
                    distance = distanceTo;
                }
            }
        }
        if (entity != null) {
            MovingObjectPosition rayTrace = new MovingObjectPosition(entity);
            if (rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit != null) {
                rayTrace.entityHit.captureDrops = true;
                rayTrace.entityHit.capturedDrops.clear();
                float damage = 0;
                if (item.getItem() instanceof ItemTool) {
                    @SuppressWarnings("unchecked")
                    Collection attributes = ((ItemTool) item.getItem()).getItemAttributeModifiers().get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
                    for (Object m : attributes) {
                        if (m instanceof AttributeModifier) {
                            damage += ((AttributeModifier) m).getAmount();
                        }
                    }
                } else {
                    damage = 1f;
                }
                rayTrace.entityHit.attackEntityFrom(DamageSource.causeMobDamage(drone), damage);
                item.damageItem(1, drone);
                if (rayTrace.entityHit.capturedDrops.size() > 0) {
                    for (EntityItem i : rayTrace.entityHit.capturedDrops) {
                        itemsDropped.add(i.getEntityItem()); // Get item stack
                        i.setDead();
                    }
                }
                rayTrace.entityHit.captureDrops = false;
            }
        }
        return itemsDropped;
    }

    @Override
    public void damageItem(EntityDrone drone, ItemStack item, IBlockState blockState) {
    }
}
