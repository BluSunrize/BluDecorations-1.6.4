package bludecorations.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import bludecorations.api.ParticleElement;
import bludecorations.api.RenderElement;

public class TileEntityCustomizeableDecoration extends TileEntity implements IInventory
{
	ItemStack[] inv = new ItemStack[27];
	boolean hasInv = false;

	double yRotation;
	double scale = 1;
	float xMin = 0.5f;
	float xMax = 0.5f;
	float zMin = 0.5f;
	float zMax = 0.5f;
	float yMin = 0.5f;
	float yMax = 0.5f;
	int lightValue = 0;
	RenderElement[] renderElements = {};
	ParticleElement[] particleElements = {};
//	= {
//			//new RenderElement("ionicTorch").setModel("/assets/bludecorations/models/BluDecorations.obj").setTexture("bludecorations:textures/models/ZeldaTorch.png").setPart("Torch_07").setTranslation(new double[]{0.5,0,0.5}).update(),
//			new RenderElement("ionicTorch").setModel("/assets/bludecorations/models/BluDecorations.obj").setTexture("bludecorations:textures/models/ZeldaTorch.png").setPart("Torch_07").setTranslation(new double[]{0.5,0,0.5}).update()
//	};


	public RenderElement[] getRenderElements()
	{
		return this.renderElements;
	}
	public void setRenderElements(RenderElement[] r)
	{
		this.renderElements = r;
	}

	public ParticleElement[] getParticleElements()
	{
		return this.particleElements;
	}
	public void setParticleElements(ParticleElement[] p)
	{
		this.particleElements = p;
	}
	
	public double getOrientation()
	{
		return this.yRotation;
	}
	public void setOrientation(double r)
	{
		this.yRotation = r;
	}

	public double getScale()
	{
		return this.scale;
	}
	public void setScale(double s)
	{
		this.scale = s;
	}
	
	public float[] getAABBLimits()
	{
		return new float[]{xMin,xMax,yMin,yMax,zMin,zMax};
	}
	public void setAABBLimits(float[] aabb)
	{
		this.xMin = aabb[0];
		this.xMax = aabb[1];
		this.yMin = aabb[2];
		this.yMax = aabb[3];
		this.zMin = aabb[4];
		this.zMax = aabb[5];
	}

	public int getLightValue()
	{
		return this.lightValue;
	}
	public void setLightValue(int light)
	{
		this.lightValue = light;
		this.worldObj.updateAllLightTypes(this.xCoord, this.yCoord, this.zCoord);
	}



	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		readCustomNBT(tags);
	}
	public void readCustomNBT(NBTTagCompound tags)
	{
		this.hasInv = tags.getBoolean("hasInv");
		if(this.hasInv)
		{
			NBTTagList tagList = tags.getTagList("Inventory");
			this.inv = new ItemStack[getSizeInventory()];
			for(int i = 0; i < tagList.tagCount(); i++)
			{
				NBTTagCompound tagPart = (NBTTagCompound)tagList.tagAt(i);
				int slot = tagPart.getByte("Slot") & 0xFF;
				if ((slot >= 0) && (slot < this.inv.length)) {
					this.inv[slot] = ItemStack.loadItemStackFromNBT(tagPart);
				}
			}
		}
		this.lightValue = tags.getInteger("lightValue");

		this.scale = tags.getDouble("scale");
		this.xMin = tags.getFloat("xMin");
		this.xMax = tags.getFloat("xMax");
		this.yMin = tags.getFloat("yMin");
		this.yMax = tags.getFloat("yMax");
		this.zMin = tags.getFloat("zMin");
		this.zMax = tags.getFloat("zMax");

		this.yRotation = tags.getDouble("yRotation");
		NBTTagList renderList = tags.getTagList("renderElements");
		this.renderElements = new RenderElement[renderList.tagCount()];
		for(int i = 0; i < renderList.tagCount(); i++)
		{
			NBTTagCompound elementTag = (NBTTagCompound)renderList.tagAt(i);
			this.renderElements[i] = RenderElement.readFromNBT(elementTag);
		}
		NBTTagList particleList = tags.getTagList("particleElements");
		this.particleElements = new ParticleElement[particleList.tagCount()];
		for(int i = 0; i < particleList.tagCount(); i++)
		{
			NBTTagCompound elementTag = (NBTTagCompound)particleList.tagAt(i);
			this.particleElements[i] = ParticleElement.readFromNBT(elementTag);
		}
		
//		if(worldObj != null)
//			System.out.println( (worldObj.isRemote?"Client":"Server") + " World, custom Meta loaded");
//		else
//			System.out.println( "How can the world be null O_o");
	}

	@Override
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		writeCustomNBT(tags);
	}
	public void writeCustomNBT(NBTTagCompound tags)
	{
		tags.setBoolean("hasInv", this.hasInv);
		if(this.hasInv)
		{
			NBTTagList tagList = new NBTTagList();
			for (int i = 0; i < this.inv.length; i++) {
				if (this.inv[i] != null)
				{
					NBTTagCompound tagPart = new NBTTagCompound();
					tagPart.setByte("Slot", (byte)i);
					this.inv[i].writeToNBT(tagPart);
					tagList.appendTag(tagPart);
				}
			}
			tags.setTag("Inventory", tagList);
		}

		tags.setDouble("yRotation", this.yRotation);

		tags.setDouble("scale",this.scale);
		tags.setFloat("xMin",this.xMin);
		tags.setFloat("xMax",this.xMax);
		tags.setFloat("yMin",this.yMin);
		tags.setFloat("yMax",this.yMax);
		tags.setFloat("zMin",this.zMin);
		tags.setFloat("zMax",this.zMax);
		
		NBTTagList renderList = new NBTTagList();
		for (int i = 0; i < this.renderElements.length; i++) {
			if (this.renderElements[i] != null)
			{
				NBTTagCompound elementTag = this.renderElements[i].writeToNBT();
				elementTag.setName("RE: "+i);
				renderList.appendTag(elementTag);
			}
		}
		tags.setTag("renderElements", renderList);
		
		NBTTagList particleList = new NBTTagList();
		for (int i = 0; i < this.particleElements.length; i++) {
			if (this.particleElements[i] != null)
			{
				NBTTagCompound elementTag = this.particleElements[i].writeToNBT();
				elementTag.setName("PE: "+i);
				particleList.appendTag(elementTag);
			}
		}
		tags.setTag("particleElements", particleList);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		writeCustomNBT(tag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readCustomNBT(packet.data);
		this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
	}






	@Override
	public int getSizeInventory() {
		return hasInv?inv.length:0;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return hasInv?inv[slot]:null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if(hasInv)
		{
			ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				if (stack.stackSize <= amount) {
					setInventorySlotContents(slot, null);
				} else {
					stack = stack.splitStack(amount);
					if (stack.stackSize == 0) {
						setInventorySlotContents(slot, null);
					}
				}
			}
			return stack;
		}
		else
			return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if(hasInv)
		{
			ItemStack stack = getStackInSlot(slot);
			if (stack != null) {
				setInventorySlotContents(slot, null);
			}
			return stack;
		}
		else
			return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(hasInv)
		{
			inv[slot] = stack;
			if (stack != null && stack.stackSize > getInventoryStackLimit()) {
				stack.stackSize = getInventoryStackLimit();
			}   
		}
	}
	@Override
	public String getInvName()
	{
		return "CustomizeableDecorationInventory";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return hasInv?64:0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return true;
	}

}