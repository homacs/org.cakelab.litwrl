package org.cakelab.litwrl.setup.shadersmod;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.utils.PropertiesFile;

public class OptionsShaders extends PropertiesFile {

	private static final long serialVersionUID = 1L;

	public static final String FILENAME = "optionsshaders.txt";

	private static final String defaults = 
			"TexMinFilB=0\n" +
			"shadowResMul=1.0\n" +
			"tweakBlockDamage=false\n" +
			"TexMagFilB=0\n" +
			"handDepthMul=0.125\n" +
			"oldLighting=false\n" +
			"shaderPack=(none)\n" +
			"TexMinFilS=0\n" +
			"renderResMul=1.0\n" +
			"TexMagFilS=0\n" +
			"TexMinFilN=0\n" +
			"shadowClipFrustrum=true\n" +
			"TexMagFilN=0\n" +
			"cloudShadow=true\n" +
			"normalMapEnabled=true\n" +
			"specularMapEnabled=true\n";

	private static final String PROPERTY_SHADERPACK = "shaderPack";
	private static final String PROPERTY_CLOUD_SHADOW = "cloudShadow";
	private static final String PROPERTY_TWEAK_BLOCK_DAMAGE = "tweakBlockDamage";
	private static final String PROPERTY_OLD_LIGHTING = "oldLighting";

	public static OptionsShaders createDefault() {
		OptionsShaders result = new OptionsShaders();
		result.loadFromString(defaults);
		return result;
	}

	public static OptionsShaders loadFromGamedir(File gamedir) throws IOException {
		// TODO: make interfaces of OptionsFiles consistent in regards to loadFromGamedir()
		File f = new File(gamedir, FILENAME);
		if (!f.exists()) return null;
		OptionsShaders result = new OptionsShaders();
		result.loadFile(f);
		return result;
	}

	public static boolean existsIn(File gamedir) throws IOException {
		File f = new File(gamedir, FILENAME);
		return f.exists();
	}
	
	public String getShaderPack() {
		return getProperty(PROPERTY_SHADERPACK);
	}
	
	public void setShaderPack(String shaderPackArchive) {
		setProperty(PROPERTY_SHADERPACK, shaderPackArchive);
	}

	public boolean getCloudShadow() {
		return getBooleanProperty(PROPERTY_CLOUD_SHADOW, false);
	}
	
	public void setCloudShadow(boolean enabled) {
		setProperty(PROPERTY_CLOUD_SHADOW, enabled);
	}

	public void setTweakBlockDamage(boolean enabled) {
		setProperty(PROPERTY_TWEAK_BLOCK_DAMAGE, enabled);
	}

	public void setOldLighting(boolean enabled) {
		setProperty(PROPERTY_OLD_LIGHTING, enabled);
	}
	

	
	
	
}
