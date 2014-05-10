package bludecorations.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import bludecorations.client.GraphicUtilities;

public class RenderElement
{
	private String modelPath = "";
	private String partName = "";
	private String texture = "";
	private double[] translation = {0,0,0};
	private double[] rotation = {0,0,0};
	private double[] colourModifier = {1,1,1};
	private double alpha = 1;

	/**
	 * perform this after modifying any values on a RenderElement to avoid any fields being null
	 * @return the RenderElement
	 */
	public RenderElement update()
	{
		if(modelPath==null)
			this.modelPath = "";
		if(partName==null)
			this.partName = "";
		if(texture==null)
			this.texture = "";
		if(translation==null)
			this.translation = new double[]{0,0,0};
		if(rotation==null)
			this.rotation = new double[]{0,0,0};
		if(colourModifier==null)
			this.colourModifier = new double[]{1,1,1};
		return this;
	}

	public RenderElement setModel(String s) {
		this.modelPath = s;
		return this;
	}
	public String getModel() {
		return this.modelPath;
	}
	public RenderElement setPart(String s) {
		this.partName = s;
		return this;
	}
	public String getPart() {
		return this.partName;
	}
	public RenderElement setTexture(String s) {
		this.texture = s;
		return this;
	}
	public String getTexture() {
		return this.texture;
	}
	public RenderElement setTranslation(double dX, double dY, double dZ) {
		this.translation = new double[]{dX,dY,dZ};
		return this;
	}
	public double[] getTranslation() {
		return this.translation;
	}
	public RenderElement setRotation(double dX, double dY, double dZ) {
		this.rotation = new double[]{dX,dY,dZ};
		return this;
	}
	public double[] getRotation() {
		return this.rotation;
	}
	public RenderElement setColour(double dX, double dY, double dZ) {
		this.colourModifier = new double[]{dX,dY,dZ};
		return this;
	}
	public double[] getColour() {
		return this.colourModifier;
	}
	public RenderElement setAlpha(double d) {
		this.alpha = d;
		return this;
	}
	public double getAlpha() {
		return this.alpha;
	}

	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound elementTag = new NBTTagCompound();
		elementTag.setString("modelPath", modelPath);
		elementTag.setString("partName", partName);
		elementTag.setString("texture", texture);
		if(translation != null)
		{
			elementTag.setDouble("translationX", translation[0]);
			elementTag.setDouble("translationY", translation[1]);
			elementTag.setDouble("translationZ", translation[2]);
		}
		if(rotation != null)
		{
			elementTag.setDouble("rotationX", rotation[0]);
			elementTag.setDouble("rotationY", rotation[1]);
			elementTag.setDouble("rotationZ", rotation[2]);
		}
		if(colourModifier != null)
		{
			elementTag.setDouble("colourModifierR", colourModifier[0]);
			elementTag.setDouble("colourModifierG", colourModifier[1]);
			elementTag.setDouble("colourModifierB", colourModifier[2]);
		}
		elementTag.setDouble("alpha", alpha);
		return elementTag;
	}

	public static RenderElement readFromNBT(NBTTagCompound tag)
	{
		RenderElement element = new RenderElement();
		element.setModel(tag.getString("modelPath"));
		element.setPart(tag.getString("partName"));
		element.setTexture(tag.getString("texture"));
		if(tag.hasKey("translationX"))
			element.setTranslation(tag.getDouble("translationX"),tag.getDouble("translationY"),tag.getDouble("translationZ"));
		if(tag.hasKey("rotationX"))
			element.setRotation(tag.getDouble("rotationX"),tag.getDouble("rotationY"),tag.getDouble("rotationZ"));
		if(tag.hasKey("colourModifierR"))
			element.setColour(tag.getDouble("colourModifierR"),tag.getDouble("colourModifierG"),tag.getDouble("colourModifierB"));
		element.setAlpha(tag.getDouble("alpha"));
		return element.update();
	}

	public void writeToOutputStream(DataOutputStream dataStream) throws IOException
	{
		dataStream.writeUTF(modelPath);
		dataStream.writeUTF(partName);
		dataStream.writeUTF(texture);
		dataStream.writeDouble(translation != null ? translation[0]: 0);
		dataStream.writeDouble(translation != null ? translation[1]: 0);
		dataStream.writeDouble(translation != null ? translation[2]: 0);
		dataStream.writeDouble(rotation != null ? rotation[0]: 0);
		dataStream.writeDouble(rotation != null ? rotation[1]: 0);
		dataStream.writeDouble(rotation != null ? rotation[2]: 0);
		dataStream.writeDouble(colourModifier != null ? colourModifier[0]: 0);
		dataStream.writeDouble(colourModifier != null ? colourModifier[1]: 0);
		dataStream.writeDouble(colourModifier != null ? colourModifier[2]: 0);
		dataStream.writeDouble(alpha);
	}

	public static RenderElement readFromInputStream(DataInputStream data) throws IOException
	{
		RenderElement element = new RenderElement();
		element.setModel(data.readUTF());
		element.setPart(data.readUTF());
		element.setTexture(data.readUTF());
		double trX = data.readDouble();
		double trY = data.readDouble();
		double trZ = data.readDouble();
		element.setTranslation(trX,trY,trZ);
		double rotX = data.readDouble();
		double rotY = data.readDouble();
		double rotZ = data.readDouble();
		element.setRotation(rotX,rotY,rotZ);
		double colR = data.readDouble();
		double colG = data.readDouble();
		double colB = data.readDouble();
		element.setColour(colR,colG,colB);
		element.setAlpha(data.readDouble());
		return element.update();
	}

	public String toTranslatedString()
	{
		String result = "";
		DecimalFormat df0 = new DecimalFormat("0.####");
		DecimalFormat df1 = new DecimalFormat("0.###");
		try
		{
			String mod = GraphicUtilities.isModelPathValid(modelPath)?modelPath.split("/")[modelPath.split("/").length-1] : "INVALID";
			String part = (GraphicUtilities.isModelPathValid(modelPath) && GraphicUtilities.isModelPartValid(GraphicUtilities.bindModel(modelPath),partName))?partName : "INVALID";
			String tex = GraphicUtilities.isTexturePathValid(texture)?texture.split("/")[texture.split("/").length-1] : "INVALID";
			String transl = null;
			String rot = null;
			String col = null;
			String alp = null;
			if(translation != null)
				transl = StatCollector.translateToLocalFormatted("gui.text.translation", df0.format(translation[0]),df0.format(translation[1]),df0.format(translation[2]));
			if(rotation != null && (colourModifier[0]!=0||colourModifier[1]!=0||colourModifier[2]!=0))
				rot= StatCollector.translateToLocalFormatted("gui.text.rotation", df0.format(rotation[0]),df0.format(rotation[1]),df0.format(rotation[2]));
			if(colourModifier != null && (colourModifier[0]!=1||colourModifier[1]!=1||colourModifier[2]!=1))
				col= StatCollector.translateToLocalFormatted("gui.text.colour", df1.format(colourModifier[0]*255),df1.format(colourModifier[1]*255),df1.format(colourModifier[2]*255));
			if(alpha != 1)
				alp = StatCollector.translateToLocalFormatted("gui.text.alpha", df0.format(alpha));
			result = StatCollector.translateToLocalFormatted("gui.text.renderElementText", mod,part,tex);
			if(transl!=null)
				result += "!LINE! "+transl;
			if(rot!=null)
				result += "!LINE! "+rot;
			if(col!=null)
				result += "!LINE! "+col;
			if(alp!=null)
				result += "!LINE! "+alp;
		}
		catch(Exception e)
		{
		}
		return result;
	}
}