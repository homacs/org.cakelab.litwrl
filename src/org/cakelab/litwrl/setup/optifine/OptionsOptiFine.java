package org.cakelab.litwrl.setup.optifine;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.utils.OptionsFile;

public class OptionsOptiFine extends OptionsFile {
	public static final String FILENAME = "optionsof.txt";
	
	static final String PROPERTY_CLOUDS = "ofClouds";
	public static final int OPTION_CLOUDS_DEFAULT = 0;
	public static final int OPTION_CLOUDS_FAST = 1;
	public static final int OPTION_CLOUDS_FANCY = 2;
	public static final int OPTION_CLOUDS_OFF = 3;

	private static final String PROPERTY_RENDER_DISTANCE_CHUNKS = "ofRenderDistanceChunks";

	private static final String PROPERTY_CHUNK_LOADING = "ofChunkLoading";
	public static final int OPTION_CHUNK_LOADING_DEFAULT = 0;
	public static final int OPTION_CHUNK_LOADING_MULTICORE = 2;

	private static final String PROPERTY_FAST_MATH = "ofFastMath";

	private static final String PROPERTY_CHUNK_UPDATES_DYNAMIC = "ofChunkUpdatesDynamic";

	
	
	public static OptionsOptiFine loadFromGamedir(File gamedir) throws IOException {
		File f = new File(gamedir, FILENAME);
		OptionsOptiFine result = new OptionsOptiFine();
		if (f.exists())result.loadFile(f);
		else result.setFile(f);
		return result;
	}

	public void setClouds(int option_clouds) {
		setProperty(PROPERTY_CLOUDS, option_clouds);
	}

	public int getRenderDistanceChunks() {
		return super.getIntProperty(PROPERTY_RENDER_DISTANCE_CHUNKS, -1);
	}

	public void setRenderDistanceChunks(int chunks) {
		super.setProperty(PROPERTY_RENDER_DISTANCE_CHUNKS, chunks);
	}

	public void setChunkLoading(int chunk_loading_option) {
		super.setProperty(PROPERTY_CHUNK_LOADING, chunk_loading_option);
	}

	public void setFastMath(boolean enabled) {
		super.setProperty(PROPERTY_FAST_MATH, enabled);
	}

	public void setChunkUpdateDynamic(boolean enabled) {
		super.setProperty(PROPERTY_CHUNK_UPDATES_DYNAMIC, enabled);
	}
}
