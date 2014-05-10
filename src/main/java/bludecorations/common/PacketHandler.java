package bludecorations.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import bludecorations.api.ParticleElement;
import bludecorations.api.RenderElement;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));

		byte packetId;
		try
		{
			packetId = data.readByte();
		} catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		//System.out.println("Packet received, handling");
		//System.out.println("ID: "+packetId);
		switch (packetId)
		{
		case 0:
			this.handleRenderElementPacket(data);
			break;
		case 1:
			this.handleTileRotationScalePacket(data);
			break;
		case 2:
			this.handleTileAABBLightPacket(data);
			break;
		case 3:
			this.handleParticleElementPacket(data);
			break;
		case 4:
			this.handleRenderWipePacket(data);
			break;
		case 5:
			this.handleParticleWipePacket(data);
			break;
		case 6:
			break;
		}
	}

	private static Packet250CustomPayload createPacket(int id, World world, Object... additionalData) throws IOException
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);
		Packet250CustomPayload packet = new Packet250CustomPayload();

		dataStream.writeByte(id); //ID
		dataStream.writeInt(world.provider.dimensionId); //WorldProvider ID
		if(additionalData != null)
			for(Object o : additionalData)
			{
				if(o == null)
					break;
				if(o instanceof Boolean)
					dataStream.writeBoolean((Boolean) o);
				else if(o instanceof Byte)
					dataStream.writeByte((Integer) o);
				else if(o instanceof Double)
					dataStream.writeDouble((Double) o);
				else if(o instanceof Float)
					dataStream.writeFloat((Float) o);
				else if(o instanceof Short)
					dataStream.writeShort((Short) o);
				else if(o instanceof Integer)
					dataStream.writeInt((Integer) o);
				else if(o instanceof String)
					dataStream.writeUTF((String) o);
				else if(o instanceof RenderElement)
					((RenderElement)o).writeToOutputStream(dataStream);
				else if(o instanceof ParticleElement)
					((ParticleElement)o).writeToOutputStream(dataStream);
			}
		dataStream.close();
		packet.channel = "BluDecorations";
		packet.data = byteStream.toByteArray();
		packet.length = byteStream.size();
		return packet;
	}

	public static void sendRenderElementPacket(World world, int x, int y, int z, int iterator, RenderElement renderElement)
	{
		try
		{
			Packet packet = createPacket(0, world, x, y, z, iterator, renderElement);
			PacketDispatcher.sendPacketToServer(packet);
			//System.out.println("Sent packet to server");

			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
			{
				TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
				int size = tile.getRenderElements().length;
				if(size <= iterator)
					size = iterator+1;

				RenderElement[] newElements = new RenderElement[size];
				for(int oldIt=0;oldIt<tile.getRenderElements().length;oldIt++)
					if(oldIt != iterator)
						newElements[oldIt]=tile.getRenderElements()[oldIt];

				newElements[iterator] = renderElement.update();
				//System.out.println("Packet Sent, Client Colour Info:"+renderElement.getColour()[0]+", "+renderElement.getColour()[1]+", "+renderElement.getColour()[2]);
				tile.setRenderElements(newElements);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void handleRenderElementPacket(DataInputStream data)
	{
		int worldID;
		int x;
		int y;
		int z;
		int iterator;
		RenderElement renderElement;
		try
		{
			worldID = data.readInt();
			x = data.readInt();
			y = data.readInt();
			z = data.readInt();
			iterator = data.readInt();
			renderElement = RenderElement.readFromInputStream(data);
		}catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		World world = DimensionManager.getWorld(worldID);
		if (world == null) return;
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
		{
			TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
			int size = tile.getRenderElements().length;
			if(size <= iterator)
				size = iterator+1;

			RenderElement[] newElements = new RenderElement[size];
			for(int oldIt=0;oldIt<tile.getRenderElements().length;oldIt++)
				if(oldIt != iterator)
					newElements[oldIt]=tile.getRenderElements()[oldIt];

			newElements[iterator] = renderElement.update();
			//System.out.println("Packet Arrived, Server Colour Info:"+renderElement.getColour()[0]+", "+renderElement.getColour()[1]+", "+renderElement.getColour()[2]);
			tile.setRenderElements(newElements);
			tile.worldObj.markBlockForUpdate(x, y, z);
		}
	}


	public static void sendTileRotationScalePacket(World world, int x, int y, int z, double rotation, double scale)
	{
		try
		{
			Packet packet = createPacket(1, world, x, y, z, rotation, scale);
			PacketDispatcher.sendPacketToServer(packet);
			//System.out.println("Sent packet to server");

			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
			{
				TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
				tile.setOrientation(rotation);
				tile.setScale(scale);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void handleTileRotationScalePacket(DataInputStream data)
	{
		int worldID;
		int x;
		int y;
		int z;
		double rotation;
		double scale;
		try
		{
			worldID = data.readInt();
			x = data.readInt();
			y = data.readInt();
			z = data.readInt();
			rotation = data.readDouble();
			scale = data.readDouble();
		}catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		World world = DimensionManager.getWorld(worldID);
		if (world == null) return;
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
		{
			TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
			tile.setOrientation(rotation);
			tile.setScale(scale);
		}
	}

	public static void sendTileAABBLightPacket(World world, int x, int y, int z, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, int light)
	{
		try
		{
			Packet packet = createPacket(2, world, x, y, z, xMin, xMax, yMin, yMax, zMin, zMax, light);
			PacketDispatcher.sendPacketToServer(packet);
			//System.out.println("Sent packet to server");

			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
			{
				TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
				tile.setAABBLimits(new float[]{xMin,xMax,yMin,yMax, zMin, zMax});
				tile.setLightValue(light);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void handleTileAABBLightPacket(DataInputStream data)
	{
		int worldID;
		int x;
		int y;
		int z;
		float xMin;
		float xMax;
		float yMin;
		float yMax;
		float zMin;
		float zMax;
		int light;
		try
		{
			worldID = data.readInt();
			x = data.readInt();
			y = data.readInt();
			z = data.readInt();
			xMin = data.readFloat();
			xMax = data.readFloat();
			yMin = data.readFloat();
			yMax = data.readFloat();
			zMin = data.readFloat();
			zMax = data.readFloat();
			light = data.readInt();
		}catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		World world = DimensionManager.getWorld(worldID);
		if (world == null) return;
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
		{
			TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
			tile.setAABBLimits(new float[]{xMin,xMax,yMin,yMax, zMin, zMax});
			tile.setLightValue(light);
		}
	}
	
	public static void sendParticleElementPacket(World world, int x, int y, int z, int iterator, ParticleElement particleElement)
	{
		try
		{
			Packet packet = createPacket(3, world, x, y, z, iterator, particleElement);
			PacketDispatcher.sendPacketToServer(packet);
			//System.out.println("Sent packet to server");

			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
			{
				TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
				int size = tile.getParticleElements().length;
				if(size <= iterator)
					size = iterator+1;

				ParticleElement[] newElements = new ParticleElement[size];
				for(int oldIt=0;oldIt<tile.getParticleElements().length;oldIt++)
					if(oldIt != iterator)
						newElements[oldIt]=tile.getParticleElements()[oldIt];

				newElements[iterator] = particleElement.update();
				tile.setParticleElements(newElements);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void handleParticleElementPacket(DataInputStream data)
	{
		int worldID;
		int x;
		int y;
		int z;
		int iterator;
		ParticleElement particleElement;
		try
		{
			worldID = data.readInt();
			x = data.readInt();
			y = data.readInt();
			z = data.readInt();
			iterator = data.readInt();
			particleElement = ParticleElement.readFromInputStream(data);
		}catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		World world = DimensionManager.getWorld(worldID);
		if (world == null) return;
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
		{
			TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
			int size = tile.getParticleElements().length;
			if(size <= iterator)
				size = iterator+1;

			ParticleElement[] newElements = new ParticleElement[size];
			for(int oldIt=0;oldIt<tile.getParticleElements().length;oldIt++)
				if(oldIt != iterator)
					newElements[oldIt]=tile.getParticleElements()[oldIt];

			newElements[iterator] = particleElement.update();
			tile.setParticleElements(newElements);
			tile.worldObj.markBlockForUpdate(x, y, z);
		}
	}
	
	
	public static void sendRenderWipePacket(World world, int x, int y, int z)
	{
		try
		{
			Packet packet = createPacket(4, world, x, y, z);
			PacketDispatcher.sendPacketToServer(packet);
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
			{
				TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
				tile.setRenderElements(new RenderElement[]{});
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void handleRenderWipePacket(DataInputStream data)
	{
		int worldID;
		int x;
		int y;
		int z;
		try
		{
			worldID = data.readInt();
			x = data.readInt();
			y = data.readInt();
			z = data.readInt();
		}catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		World world = DimensionManager.getWorld(worldID);
		if (world == null) return;
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
		{
			TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
			tile.setRenderElements(new RenderElement[]{});
		}
	}
	
	public static void sendParticleWipePacket(World world, int x, int y, int z)
	{
		try
		{
			Packet packet = createPacket(5, world, x, y, z);
			PacketDispatcher.sendPacketToServer(packet);
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
			{
				TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
				tile.setParticleElements(new ParticleElement[]{});
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void handleParticleWipePacket(DataInputStream data)
	{
		int worldID;
		int x;
		int y;
		int z;
		try
		{
			worldID = data.readInt();
			x = data.readInt();
			y = data.readInt();
			z = data.readInt();
		}catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		World world = DimensionManager.getWorld(worldID);
		if (world == null) return;
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity!= null && tileEntity instanceof TileEntityCustomizeableDecoration)
		{
			TileEntityCustomizeableDecoration tile = (TileEntityCustomizeableDecoration) tileEntity;
			tile.setParticleElements(new ParticleElement[]{});
		}
	}
}