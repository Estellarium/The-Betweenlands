package thebetweenlands.common.entity.mobs;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.util.PlayerUtil;

public class EntityRockSnotTendril extends Entity implements IEntityAdditionalSpawnData {
    public EntityRockSnot parent;

	public EntityRockSnotTendril(World world) {
		super(world);
		setSize(0.25F, 0.25F);
		this.parent = null;
	}

	public EntityRockSnotTendril(EntityRockSnot parent) {
        super(parent.getWorld());
        setSize(0.25F, 0.25F);
        this.parent = parent;
        ignoreFrustumCheck = true;
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public void onUpdate() {
		if (!this.world.isRemote)
			if (getParentEntity() == null || getParentEntity().isDead)
				setDead();

		checkCollision();

		if (parent != null && !parent.getExtending()) {
			if (getEntityBoundingBox().intersects(parent.getEntityBoundingBox())) {
				if (posX != parent.posX || posZ != parent.posZ)
					setPosition(parent.posX, parent.posY, parent.posZ);
				motionX = 0D;
				motionY = 0D;
				motionZ = 0D;

				if (!getEntityWorld().isRemote) {
					if (isBeingRidden()) {
						Entity entity = getPassengers().get(0);
						entity.startRiding(parent, true);
					}
					setDead();
					parent.setCanShootTendril(true);
				}
			}
		}

		if (parent != null && (!isBeingRidden() && ticksExisted > 120) || parent != null && collidedVertically)
			if (!getEntityWorld().isRemote)
				parent.setAttackTarget(null);

		if (parent != null && parent.getAttackTarget() == null && !parent.getExtending())
			if (posX != parent.posX || posY != parent.posY || posZ != parent.posZ)
				returnToParent();

		move(MoverType.SELF, motionX, motionY, motionZ);
		motionX *= 1D;
		motionY *= 1D;
		motionZ *= 1D;
		super.onUpdate();
	}

	public void moveToTarget(double targetX, double targetY, double targetZ, float velocity) {
		float distSq = MathHelper.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
		targetX = targetX / (double) distSq;
		targetY = targetY / (double) distSq;
		targetZ = targetZ / (double) distSq;
		targetX = targetX * (double) velocity;
		targetY = targetY * (double) velocity;
		targetZ = targetZ * (double) velocity;
		motionX = targetX;
		motionY = targetY;
		motionZ = targetZ;
		float angle = MathHelper.sqrt(targetX * targetX + targetZ * targetZ);
		rotationYaw = (float) (MathHelper.atan2(targetX, targetZ) * (180D / Math.PI));
		rotationPitch = (float) (MathHelper.atan2(targetY, (double) angle) * (180D / Math.PI));
		prevRotationYaw = rotationYaw;
		prevRotationPitch = rotationPitch;
	}

	protected Entity checkCollision() {
		if (parent != null && parent.getAttackTarget() != null) {
			List<EntityLivingBase> list = getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox());
			for (int i = 0; i < list.size(); i++) {
				Entity entity = list.get(i);
				if (entity != null && entity == parent.getAttackTarget()) {
					if (entity instanceof EntityLivingBase && !(entity instanceof EntityRockSnot) && !(entity instanceof EntityRockSnotTendril)) {
						if (!isBeingRidden()) {
							entity.startRiding(this, true);
							if (!getEntityWorld().isRemote)  {
								if (parent.getExtending())
									parent.setExtending(false);
								returnToParent();
							}
						}
					}
				}
			}
		}
		return null;
	}

	public void returnToParent() {
		double targetX = parent.posX - posX;
		double targetY = parent.posY - posY;
		double targetZ = parent.posZ - posZ;
		moveToTarget(targetX, targetY, targetZ, 0.25F);
	}

	@Override
	public void updatePassenger(Entity entity) {
		PlayerUtil.resetFloating(entity);
		if (entity instanceof EntityLivingBase) {
			double a = Math.toRadians(rotationYaw);
			double offSetX = Math.sin(a) *  -0.125D;
			double offSetZ = -Math.cos(a) * -0.125D;
			entity.setPosition(posX, getEntityBoundingBox().minY - entity.height - height, posZ);
			if (entity.isSneaking())
				entity.setSneaking(false);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
	}

	public EntityRockSnot getParentEntity() {
		return parent;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		if (getParentEntity() != null)
			buffer.writeInt(getParentEntity().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		if (buffer.isReadable()) {
			int parentEntityID = buffer.readInt();
			EntityRockSnot parentEntityIn = (EntityRockSnot) world.getEntityByID(parentEntityID);
			this.parent = parentEntityIn;
		}
	}

	@Override	
    public boolean canBeCollidedWith() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getEntityBoundingBox().grow(10D);
    }

}