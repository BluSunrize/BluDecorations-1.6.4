package bludecorations.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import bludecorations.common.TileEntityWineRack;

public class TileRenderWineRack extends TileEntitySpecialRenderer
{
	IModelCustom bottleModel;

	public TileRenderWineRack()
	{
		bottleModel = AdvancedModelLoader.loadModel("/assets/bludecorations/models/BluDecorations.obj");
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		try{
			TileEntityWineRack tile = (TileEntityWineRack) tileEntity;
			ItemStack outer = new ItemStack(tile.rackOuterID,1,tile.rackOuterMeta);
			ItemStack inner = new ItemStack(tile.rackInnerID,1,tile.rackInnerMeta);
			double rackRot = tile.orientation == 0 ? 90: tile.orientation==1 ? 0: tile.orientation==2 ? 270: 180;
			double offsetX = tile.orientation == 0 ? 0.5: tile.orientation == 1 ? 0: tile.orientation == 2 ? 0.5: 1;
			double offsetZ = tile.orientation == 0 ? 1: tile.orientation == 1 ? 0.5: tile.orientation == 2 ? 0: 0.5;
			String texRawO =  Block.blocksList[tile.rackOuterID].getIcon(0, tile.rackOuterMeta).getIconName();
			//System.out.println(texRawO);
			String texFiniteO = "";
			if(outer.getItem().getClass().getName().startsWith("net.minecraft"))
				texFiniteO = "textures/blocks/" + texRawO + ".png";
			else
			{
				String[] texArrayO = texRawO.split(":", 2);
				texFiniteO = texArrayO[0] + ":textures/blocks/" + texArrayO[1] + ".png";
			}
			render(new double[]{offsetX,0,offsetZ},new double[]{0,rackRot,0},"WineRack_08",texFiniteO);

			String texRawI = Block.blocksList[tile.rackInnerID].getIcon(0, tile.rackInnerMeta).getIconName();
			String texFiniteI = "";
			if(inner.getItem().getClass().getName().startsWith("net.minecraft"))
				texFiniteI = "textures/blocks/" + texRawI + ".png";
			else
			{
				String[] texArrayI = texRawI.split(":", 2);
				texFiniteI = texArrayI[0] + ":textures/blocks/" + texArrayI[1] + ".png";
			}
			render(new double[]{offsetX,0,offsetZ},new double[]{0,rackRot,0},"WineRack_Internal_08_2",texFiniteI);

			for(int b=0;b<tile.getSizeInventory();b++)
			{
								ItemStack bottle = tile.getStackInSlot(b);
								if(bottle != null)
								{
									int row = b<4?0: b<7?1: b<11?2: b<14?3: b<18?4: b<21?5: 6;
									double botX = (row==1||row==3||row==5?.125 : 0) + .25 * (b - (row==0?0: row==1?4: row==2?7: row==3?11: row==4?14: row==5?18: 21));
									double botY = row==0?0: row==1?0.125: row==2?0.25: row==3?0.375: row==4?0.5: row==5?0.625: 0.75;
									//if(b==0)
//									ClientProxy.bdModel.renderPart("WineRack_Bottle_Simple_08_1_1");
									//bottleModel.renderPart("WineRack_Bottle_VerySimple_08_1_2");
									render(new double[]{offsetX+(tile.orientation==0?-botX:tile.orientation==2?botX:0),-botY,offsetZ+(tile.orientation==1?-botX:tile.orientation==3?botX:0)},new double[]{0,rackRot,0},"WineRack_Bottle_VerySimple_08_1_2","bludecorations:textures/models/winebottle.png");
//									render(new double[]{offsetX+(tile.orientation==0?-botX:tile.orientation==2?botX:0),-botY,offsetZ+(tile.orientation==1?-botX:tile.orientation==3?botX:0)},new double[]{0,rackRot,0},"WineRack_Bottle_Simple_08_1_1","bludecorations:textures/models/winebottle.png");
								}
			}

		}catch(Exception e){
			//e.printStackTrace();
		}			

		GL11.glPopMatrix();
	}

	private void render(double[] offset, double[] rot, String partName, String texture)
	{
		if(offset!=null && offset.length>2)
		{
			GL11.glTranslated(offset[0], 0, 0);
			GL11.glTranslated(0, offset[1], 0);
			GL11.glTranslated(0, 0, offset[2]);
		}
		if(rot!=null && rot.length>2)
		{
			GL11.glRotated(rot[0], 1,0,0);
			GL11.glRotated(rot[1], 0,1,0);
			GL11.glRotated(rot[2], 0,0,1);
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(texture));
		//ClientProxy.bdModel.renderPart(partName);
		if(rot!=null && rot.length>2)
		{
			GL11.glRotated(-rot[2], 0,0,1);
			GL11.glRotated(-rot[1], 0,1,0);
			GL11.glRotated(-rot[0], 1,0,0);
		}
		if(offset!=null && offset.length>2)
		{
			GL11.glTranslated(0, 0, -offset[2]);
			GL11.glTranslated(0, -offset[1], 0);
			GL11.glTranslated(-offset[0], 0, 0);
		}

	}

}
