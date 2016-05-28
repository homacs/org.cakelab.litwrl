package org.cakelab.litwrl.setup.litwr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecConfiguration;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.omcl.config.GameTypes;

public class LitWRLConfig {
	private static JSONCodecConfiguration jsonConfig = new JSONCodecConfiguration(Charset.defaultCharset(), true, true);

	public static final String CONFIG_FILE = "litwr-launcher.cfg";

	
	private transient File file = null;
	private transient boolean modified = false;
	
	private String type;
	private String variant;
	private String version;
	private boolean keepVersion;
	
	private LitWRLConfig() {}
	
	public LitWRLConfig(String version, GameTypes type, Variants variant, boolean keepVersion) {
		this.version  = version;
		this.type = type.toString();
		this.variant = variant.toString();
		this.keepVersion = keepVersion;
		this.modified = true;
	}

	public static LitWRLConfig load(File configFile) throws IOException, JSONCodecException {
		JSONCodec codec = new JSONCodec(jsonConfig);
		if (configFile.exists()) {
			LitWRLConfig config = new LitWRLConfig();
			FileInputStream in = new FileInputStream(configFile);
			codec.decodeObject(in, config);
			in.close();
			config.file = configFile;
			config.modified = false;
			return config;
		} else {
			return null;
		}
	}
	
	public void save(File configFile) throws IOException, JSONCodecException {
		JSONCodec codec = new JSONCodec(jsonConfig);
		
		FileOutputStream out = new FileOutputStream(configFile);
		codec.encodeObject(this, out);
		out.close();
		modified = false;
	}
	
	public void save() throws IOException, JSONCodecException {
		save(file);
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

	public boolean isKeepVersion() {
		return keepVersion;
	}

	public void setKeepVersion(boolean keepVersion) {
		if (this.keepVersion != keepVersion) {
			this.keepVersion = keepVersion;
			modified = true;
		}
	}

	
	public boolean isModified() {
		return modified;
	}
	
}
