package bludecorations.common;

import java.lang.reflect.Constructor;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import bludecorations.api.ParticleElement;
import bludecorations.api.RenderElement;

public class BlockCustomizeableDecoration extends BlockContainer
{

	public BlockCustomizeableDecoration(int id)
	{
		super(id, Material.cloth);
		this.setTickRandomly(true);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) world.getBlockTileEntity(x, y, z);
		if(tile != null)
			return tile.getLightValue();
		return 0;
	}

	@Override
	public int getLightOpacity(World world, int x, int y, int z)
	{
		return 0;
	}
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face)
	{
		return 0;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) world.getBlockTileEntity(x, y, z);
		if(tile != null)
		{
			float[] aabb = tile.getAABBLimits();
			this.setBlockBounds(0.5f-aabb[0],0.5f-aabb[2],0.5f-aabb[4],0.5f+aabb[1],0.5f+aabb[3],0.5f+aabb[5]);
		}
		else
			this.setBlockBounds(0f,0f,0f,1f,1f,1f);
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
		TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) world.getBlockTileEntity(x, y, z);
		try
		{
			if(tile != null)
				for(ParticleElement pe: tile.getParticleElements())
				{
					Class<EntityFX> c = (Class<EntityFX>) Class.forName(pe.getParticleClass());
					Constructor<EntityFX> constructor = c.getConstructor(World.class,double.class,double.class,double.class,double.class,double.class,double.class);
					double xPos = x + pe.getTranslation()[0];
					double yPos = y + pe.getTranslation()[1];
					double zPos = z + pe.getTranslation()[2];
					EntityFX fx = constructor.newInstance(world, xPos, yPos, zPos, 0,0,0);
					fx.setRBGColorF(pe.getColour()[0], pe.getColour()[1], pe.getColour()[2]);
					fx.setAlphaF(pe.getAlpha());
					fx.multipleParticleScaleBy(pe.getScale());
					Minecraft.getMinecraft().effectRenderer.addEffect(fx);
				}
		}
		catch(Exception e)
		{
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) world.getBlockTileEntity(x, y, z);
		if(!player.isSneaking())
		{
			if(player.getCurrentEquippedItem()!= null && player.getCurrentEquippedItem().itemID == Block.chest.blockID && !tile.hasInv)
			{
				tile.hasInv = true;
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				return true;
			}
			if(player.getCurrentEquippedItem()!= null && player.getCurrentEquippedItem().getItem() instanceof ItemTool && player.getCurrentEquippedItem().getItemDamage() == 0)
			{
				player.openGui(BluDecorations.instance, 0, world, x, y, z);
				return true;
			}
			if(player.getCurrentEquippedItem()!= null && player.getCurrentEquippedItem().getItem() instanceof ItemTool && player.getCurrentEquippedItem().getItemDamage() == 1)
			{
				player.openGui(BluDecorations.instance, 1, world, x, y, z);
				return true;
			}
			if(player.getCurrentEquippedItem()!= null && player.getCurrentEquippedItem().getItem() instanceof ItemTool && player.getCurrentEquippedItem().getItemDamage() == 2)
			{
				if(!player.getCurrentEquippedItem().hasTagCompound())
					player.getCurrentEquippedItem().setTagCompound(new NBTTagCompound());
				if(player.getCurrentEquippedItem().getTagCompound().hasKey("copiedDecoration"))
				{
					NBTTagCompound decoTag = player.getCurrentEquippedItem().getTagCompound().getCompoundTag("copiedDecoration");
					tile.setLightValue(decoTag.getInteger("lightValue"));
					tile.setScale(decoTag.getDouble("scale"));
					tile.setAABBLimits(new float[]{decoTag.getFloat("xMin"),decoTag.getFloat("xMax"), decoTag.getFloat("yMin"),decoTag.getFloat("yMax"), decoTag.getFloat("zMin"),decoTag.getFloat("zMax")});
					tile.setOrientation(decoTag.getDouble("yRotation"));

					NBTTagList renderList = decoTag.getTagList("renderElements");
					RenderElement[] renderElements = new RenderElement[renderList.tagCount()];
					for(int i = 0; i < renderList.tagCount(); i++)
					{
						NBTTagCompound elementTag = (NBTTagCompound)renderList.tagAt(i);
						renderElements[i] = RenderElement.readFromNBT(elementTag);
					}
					tile.setRenderElements(renderElements);

					NBTTagList particleList = decoTag.getTagList("particleElements");
					ParticleElement[] particleElements = new ParticleElement[particleList.tagCount()];
					for(int i = 0; i < particleList.tagCount(); i++)
					{
						NBTTagCompound elementTag = (NBTTagCompound)particleList.tagAt(i);
						particleElements[i] = ParticleElement.readFromNBT(elementTag);
					}
					tile.setParticleElements(particleElements);
				}
				else
				{
					NBTTagCompound decoTag = new NBTTagCompound();

					decoTag.setDouble("yRotation", tile.getOrientation());

					decoTag.setDouble("scale",tile.getScale());
					decoTag.setFloat("xMin",tile.getAABBLimits()[0]);
					decoTag.setFloat("xMax",tile.getAABBLimits()[1]);
					decoTag.setFloat("yMin",tile.getAABBLimits()[2]);
					decoTag.setFloat("yMax",tile.getAABBLimits()[3]);
					decoTag.setFloat("zMin",tile.getAABBLimits()[4]);
					decoTag.setFloat("zMax",tile.getAABBLimits()[5]);

					NBTTagList renderList = new NBTTagList();
					for (int i = 0; i < tile.getRenderElements().length; i++) {
						if (tile.getRenderElements()[i] != null)
						{
							NBTTagCompound elementTag = tile.getRenderElements()[i].writeToNBT();
							elementTag.setName("RE: "+i);
							renderList.appendTag(elementTag);
						}
					}
					decoTag.setTag("renderElements", renderList);

					NBTTagList particleList = new NBTTagList();
					for (int i = 0; i < tile.getParticleElements().length; i++) {
						if (tile.getParticleElements()[i] != null)
						{
							NBTTagCompound elementTag = tile.getParticleElements()[i].writeToNBT();
							elementTag.setName("PE: "+i);
							particleList.appendTag(elementTag);
						}
					}
					decoTag.setTag("particleElements", particleList);

					player.getCurrentEquippedItem().getTagCompound().setCompoundTag("copiedDecoration", decoTag);
				}
				return true;
			}
			if(player.getCurrentEquippedItem()!= null && player.getCurrentEquippedItem().getItem() instanceof ItemTool && player.getCurrentEquippedItem().getItemDamage() == 3)
			{
				tile.infiniteRenderSize = !tile.infiniteRenderSize;
				if(world.isRemote)
					player.addChatMessage(StatCollector.translateToLocal("gui.text.renderBounds"+(tile.infiniteRenderSize==false?"1":"0")));
			}
			if(tile.hasInv)
			{
				player.openGui(BluDecorations.instance, 2, world, x, y, z);
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public void registerIcons(IconRegister ir)
	{
		this.blockIcon = ir.registerIcon("bludecorations:customDecoration");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCustomizeableDecoration();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration)world.getBlockTileEntity(x,y,z);

		if(tile.hasInv)
		{
			float f3 = 0.05F;
			EntityItem entityitem;
			entityitem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Block.chest));
			entityitem.motionX = (float)world.rand.nextGaussian() * f3;
			entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float)world.rand.nextGaussian() * f3;
			world.spawnEntityInWorld(entityitem);
			for(int i=0;i<tile.getSizeInventory();i++)
			{
				ItemStack stack = tile.getStackInSlot(i);
				if (stack != null)
				{
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					for (float f2 = world.rand.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; world.spawnEntityInWorld(entityitem))
					{
						int k1 = world.rand.nextInt(21) + 10;
						if (k1 > stack.stackSize)
							k1 = stack.stackSize;
						stack.stackSize -= k1;
						entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(stack.itemID, k1, stack.getItemDamage()));
						entityitem.motionX = (float)world.rand.nextGaussian() * f3;
						entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float)world.rand.nextGaussian() * f3;

						if (stack.hasTagCompound())
						{
							entityitem.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
						}
					}
				}
			}
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}
}
