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

public class TileEntityWineRack extends TileEntity implements IInventory
{
	public ItemStack[] inv = new ItemStack[25];
	public int rackOuterID;
	public int rackOuterMeta;
	public int rackInnerID;
	public int rackInnerMeta;
	public int orientation;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tags)
	{
		super.readFromNBT(tags);
		readCustomNBT(tags);
	}

	public void readCustomNBT(NBTTagCompound tags)
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

		this.rackOuterID = tags.getInteger("rackOuterID");
		this.rackOuterMeta = tags.getInteger("rackOuterMeta");
		this.rackInnerID = tags.getInteger("rackInnerID");
		this.rackInnerMeta = tags.getInteger("rackInnerMeta");
		this.orientation = tags.getInteger("orientation");
	}
	@Override
	public void writeToNBT(NBTTagCompound tags)
	{
		super.writeToNBT(tags);
		writeCustomNBT(tags);
	}

	public void writeCustomNBT(NBTTagCompound tags)
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

		tags.setInteger("rackOuterID", this.rackOuterID);
		tags.setInteger("rackOuterMeta", this.rackOuterMeta);
		tags.setInteger("rackInnerID", this.rackInnerID);
		tags.setInteger("rackInnerMeta", this.rackInnerMeta);
		tags.setInteger("orientation", this.orientation);
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
	public int getSizeInventory()
	{
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inv[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			setInventorySlotContents(slot, null);
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		inv[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}              
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public String getInvName()
	{
		return "Winerack";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{}

	@Override
	public void closeChest()
	{}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		return true;
	}
}
