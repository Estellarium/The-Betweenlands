package thebetweenlands.common.network.clientbound;

import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thebetweenlands.client.event.handler.DruidAltarSoundHandler;
import thebetweenlands.client.render.particle.BLParticles;
import thebetweenlands.client.render.particle.ParticleFactory.ParticleArgs;
import thebetweenlands.common.network.MessageBase;
import thebetweenlands.common.registries.BlockRegistry;
import thebetweenlands.common.tile.TileEntityDruidAltar;

public class MessageDruidAltarProgress extends MessageBase {
	private BlockPos pos;
	private int progress;

	public MessageDruidAltarProgress() {}

	public MessageDruidAltarProgress(TileEntityDruidAltar tile) {
		this.pos = tile.getPos();
		this.progress = tile.craftingProgress;
	}

	public MessageDruidAltarProgress(TileEntityDruidAltar tile, int progress) {
		this.pos = tile.getPos();
		this.progress = progress;
	}

	@Override
	public void serialize(PacketBuffer buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeInt(this.progress);
	}

	@Override
	public void deserialize(PacketBuffer buffer) {
		this.pos = buffer.readBlockPos();
		this.progress = buffer.readInt();
	}

	@Override
	public IMessage process(MessageContext ctx) {
		World world = FMLClientHandler.instance().getWorldClient();
		TileEntity te = world.getTileEntity(this.pos);
		if (te instanceof TileEntityDruidAltar) {
			TileEntityDruidAltar altar = (TileEntityDruidAltar) te;
			if (this.progress >= 0) {
				altar.craftingProgress = this.progress;
			} else if(this.progress == -1) {
				for (int x = -8; x <= 8; x++) {
					for (int y = -8; y <= 8; y++) {
						for (int z = -8; z <= 8; z++) {
							BlockPos pos = te.getPos().add(x, y, z);
							Block block = world.getBlockState(pos).getBlock();
							if (block == BlockRegistry.DRUID_STONE_1 || block == BlockRegistry.DRUID_STONE_2 || 
									block == BlockRegistry.DRUID_STONE_3 || block == BlockRegistry.DRUID_STONE_4 || 
									block == BlockRegistry.DRUID_STONE_5) {
								BLParticles.ALTAR_CRAFTING.spawn(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, ParticleArgs.get().withScale(0.25F + te.getWorld().rand.nextFloat() * 0.2F).withData(te));
							}
						}
					}
				}

				DruidAltarSoundHandler.playAltarSound(altar);
			} else if(this.progress == -2) {
				DruidAltarSoundHandler.stopAltarSound(altar);
			}
		}
		return null;
	}
}