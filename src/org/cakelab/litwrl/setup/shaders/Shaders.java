package org.cakelab.litwrl.setup.shaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.cakelab.litwrl.setup.optifine.OptionsOptiFine;
import org.cakelab.litwrl.setup.shadersmod.OptionsShaders;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.minecraft.Options;
import org.cakelab.omcl.utils.log.Log;


public class Shaders<I> {
	public static final String SHADER_NONE = "(none)";
	public static final String SHADER_INTERNAL = "(internal)";
	
	private ArrayList<PackageDescriptor> available;
	private I identifier;
	
	public Shaders(I identifier, PackageDescriptor shadersMetaPackageDescriptor, Repository repository) throws Exception {
		this.identifier = identifier;
		available = new ArrayList<PackageDescriptor>(shadersMetaPackageDescriptor.optional.length);

		for (String option : shadersMetaPackageDescriptor.optional) {
			if (option.startsWith("thirdparty/shaders")) {
				available.add(repository.getLocalPackageDescriptorFromLocation(option));
			}
		}
	}

	public I getIdentifier() {
		return identifier;
	}

	public synchronized String[] getAvailableShaders() {
		String[] result = new String[2 + available.size()];
		int i = 0;
		result[i++] = SHADER_NONE;
		result[i++] = SHADER_INTERNAL;
		for (int j = 0; j < available.size(); j++, i++) {
			PackageDescriptor shader = available.get(j);
			result[i] = shader.name;
		}
		return result;
	}



	public synchronized String getNameOf(String shaderPackFileName) {

		if (shaderPackFileName.equals(SHADER_NONE) || shaderPackFileName.equals(SHADER_INTERNAL)) {
			return shaderPackFileName;
		}
		for (int i = 0; i < available.size(); i++) {
			PackageDescriptor shader = available.get(i);
			if (shader.filename.equals(shaderPackFileName)) {
				return shader.name;
			}
		}
		throw new IllegalArgumentException("Unknown shader '" + shaderPackFileName + "'");
	}

	public synchronized String getFilenameOf(String shaderName) {
		if (shaderName.equals(SHADER_NONE) || shaderName.equals(SHADER_INTERNAL)) {
			return shaderName;
		}
		for (int i = 0; i < available.size(); i++) {
			PackageDescriptor shader = available.get(i);
			if (shader.name.equals(shaderName)) {
				return shader.filename;
			}
		}
		
		throw new IllegalArgumentException("Unknown shader '" + shaderName + "'");
	}

	public static String getUnknownShaderName(String shaderPackFileName) {
		return shaderPackFileName.replaceAll("\\.zip$", "");
	}

	public static String getUnknownShaderFileName(String shaderName) {
		return shaderName + ".zip";
	}



	public synchronized PackageDescriptor getPackageDescriptor(String shaderName) {
		PackageDescriptor shader = null;
		for (int i = 0; i < available.size(); i++) {
			PackageDescriptor s = available.get(i);
			if (s.name.equals(shaderName)) {
				shader = s;
				break;
			}
		}
		
		return shader;
	}


	public String getNameOfEquivalent(String shaderPackFileName) {
		if (shaderPackFileName == null) throw new IllegalArgumentException("requested shader pack file is null");
		if (shaderPackFileName.equals(SHADER_NONE) || shaderPackFileName.equals(SHADER_INTERNAL)) {
			return shaderPackFileName;
		}
		int matchLen = 0;
		String bestMatch = null;
		int minMatchLen = ((shaderPackFileName.length() -4) - "Vx.x.x".length());
		for (int i = 0; i < available.size(); i++) {
			PackageDescriptor shader = available.get(i);
			int n = stringMatchLength(shader.filename.replace(".zip", ""), shaderPackFileName.replace(".zip", ""));
			if (n > matchLen && n > minMatchLen) {
				bestMatch = shader.name;
				matchLen = n;
				if (n == shaderPackFileName.length()) {
					// still the same shader
					return bestMatch;
				}
			}
		}
		if (bestMatch == null) throw new IllegalArgumentException("no match found for shader pack file name '" + shaderPackFileName + "'");
		return bestMatch;
	}

	private int stringMatchLength(String s1, String s2) {
		int i = 0, j = 0;
		int len = 0;
		while (i < s1.length() && j < s2.length()
				&& s1.charAt(i) == s2.charAt(j)) 
		{
			len++;
			j++;
			i++;
		}
		i = s1.length()-1;
		j = s2.length()-1;
		while (i >= 0 && j >= 0
			&& s1.charAt(i) == s2.charAt(j)) 
		{
			len++;
			j--;
			i--;
		}
		return len;
	}



	public PackageDescriptor migrateUnknownShader(File gamedir, String shaderName) {
		String filename = getUnknownShaderFileName(shaderName);
		if (!isInstalled(filename, gamedir)) return getPackageDescriptor(Shaders.SHADER_NONE);
		
		PackageDescriptor shader = new PackageDescriptor(shaderName, "0", filename, getUnknownShaderLocation(shaderName), "");
		available.add(shader);
		return shader;
	}



	public String getUnknownShaderLocation(String shaderName) {
		return "thirdparty/shaders/user/" + shaderName;
	}



	public ShaderSetup getSetupService(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		if (pd.hasPatch) {
			return new PatchedShaderSetup(setupParams, pd, repository);
		} else {
			return new ShaderSetup(setupParams, pd, repository);
		}
	}



	public static boolean isNonStandardShader(String shader) {
		return !(shader == null || shader.equals(SHADER_NONE) || shader.equals(SHADER_INTERNAL));
	}



	public static void setShaderOptions(String shaderFilename, File gamedir) throws IOException {
		OptionsShaders optionsshaders = OptionsShaders.loadFromGamedir(gamedir);
		
		String previous = optionsshaders.getShaderPack();
		if (previous.equals(shaderFilename)) return;
		
		optionsshaders.setShaderPack(shaderFilename);

		if (Shaders.isNonStandardShader(shaderFilename)) {
			//
			// Options for not every downloaded shader.
			//
			optionsshaders.setOldLighting(false);
			optionsshaders.setCloudShadow(false);
			optionsshaders.setTweakBlockDamage(true);
			optionsshaders.save();
			
			Options mcoptions = Options.loadFromGamedir(gamedir);
			mcoptions.setClouds(true); // that seems weird but optifine needs it like that
			mcoptions.save();
			
			OptionsOptiFine optionsof = OptionsOptiFine.loadFromGamedir(gamedir);
			optionsof.setClouds(OptionsOptiFine.OPTION_CLOUDS_OFF);
			// optifine options too system specific to find generic values for all systems
			optionsof.save();
		} else {
			//
			// Options for minecraft (none) or (internal) shader
			//
			optionsshaders.setCloudShadow(true);
			optionsshaders.setTweakBlockDamage(false);
			optionsshaders.save();
			
			Options mcoptions = Options.loadFromGamedir(gamedir);
			mcoptions.setClouds(true);
			mcoptions.save();
			
			
			OptionsOptiFine optionsof = OptionsOptiFine.loadFromGamedir(gamedir);
			optionsof.setClouds(OptionsOptiFine.OPTION_CLOUDS_DEFAULT);
			optionsof.save();
		}
	}

	public static void setCapRenderDistance(File gamedir, int chunks) {
		try {
			Options mcoptions = Options.loadFromGamedir(gamedir);
			if (mcoptions.getRenderDistance() > 16) {
				mcoptions.setRenderDistance(16);
				mcoptions.save();
			}
		} catch (IOException e) {
			Log.warn("failed to set max render distance to 16", e);
		}

		try {
			OptionsOptiFine optifine = OptionsOptiFine.loadFromGamedir(gamedir);
			if (optifine.getRenderDistanceChunks() > 16) {
				optifine.setRenderDistanceChunks(16);
				optifine.save();
			}
		} catch (IOException e) {
			// not installed: ignore
		}
	}



	public static boolean isInstalled(String filename, File gamedir) {
		if (filename == null) return false;
		return new File(new File(gamedir, "shaderpacks"), filename).exists();
	}





}
