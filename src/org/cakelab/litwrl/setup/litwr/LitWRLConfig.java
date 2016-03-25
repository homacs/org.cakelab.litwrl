package org.cakelab.litwrl.setup.litwr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.omcl.config.GameTypes;

public class LitWRLConfig {

	public static final String CONFIG_FILE = "litwr-launcher.cfg";

	
	String type;
	String variant;
	String version;
	
	private LitWRLConfig() {}
	
	public LitWRLConfig(String version, GameTypes type, Variants variant) {
		this.version  = version;
		this.type = type.toString();
		this.variant = variant.toString();
	}

	public static LitWRLConfig load(File configFile) throws IOException, JSONCodecException {
		JSONCodec codec = new JSONCodec(true, true);
		if (configFile.exists()) {
			LitWRLConfig config = new LitWRLConfig();
			FileInputStream in = new FileInputStream(configFile);
			codec.decodeObject(in, config);
			in.close();
			return config;
		} else {
			return null;
		}
	}
	
	public void save(File configFile) throws IOException, JSONCodecException {
		JSONCodec codec = new JSONCodec(true, true);
		
		FileOutputStream out = new FileOutputStream(configFile);
		codec.encodeObject(this, out);
		out.close();
	}

	public static LitWRLConfig loadFromGameDir(File gameDir) throws IOException, JSONCodecException {
		return load(new File(gameDir, "config" + File.separator + LitWRLConfig.CONFIG_FILE));
	}

	public String getVersion() {
		return version;
	}

	public String getType() {
		return type;
	}

	public String getVariant() {
		return variant;
	}


	
}
