package org.cakelab.litwrl.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecConfiguration;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.Launcher;
import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.utils.log.Log;

public class Config {
	private static JSONCodecConfiguration jsonConfig = new JSONCodecConfiguration(Charset.defaultCharset(), true, true);

	/** Path of config file */
	private transient File file;
	private static final String CONFIG_FILE = "litwrl.cfg";
	
	private transient boolean modified = true;
	private transient boolean canSave  = true;
	
	private boolean offline           = false;
	private String downloadFolder     = null;
	private String workdir            = null;
	private String selectedVariant    = Variants.BASIC.toString();
	private String selectedType       = GameTypes.CLIENT.toString();
	private String updateURL          = "http://lifeinthewoods.ca/litwr/repository";
	private String secondaryUpdateUrl = "http://homac.cakelab.org/projects/litwrl/repository";
	private String[] serverPool       = new String[] {
			"http://www.lifeinthewoods.eu/litwr/repository",
			"http://lifeinthewoods.phedran.com/litwr/repository",
			"http://lifeinthewoods.getitfromhere.co.uk/litwr/repository"
		};
	private GameConfig[] configs      = new GameConfig[0];
	private String lastVersion        = "0.0.0";
	private boolean showGameLog       = false;

	private boolean dontShowLaunchHint = false;
	private boolean dontShowShaderHint = false;
	private boolean dontShowAdWarning = false;
	private boolean dontShowUpgradeWarning = false;
	
	public Config(File dir) {
		this.file = new File(dir, CONFIG_FILE);
	}

	public boolean validateSelectedVariant(String selectedVariant) {
		try {
			Variants.get(selectedVariant);
			return true;
		} catch (IllegalArgumentException e) {
			Log.warn("Invalid last variant in configuration. Reset to " + Variants.BASIC.toString(), e);
			return false;
		}
	}
	
	public void setSelectedVariant(Variants selectedVariant) {
		if (!selectedVariant.toString().equals(this.selectedVariant)) {
			this.selectedVariant = selectedVariant.toString();
			modified = true;
		}
	}

	
	public String getUpdateURL() {
		return updateURL;
	}
	
	public String getSecondaryUpdateURL() {
		return secondaryUpdateUrl;
	}


	public Variants getSelectedVariant() {
		return Variants.get(selectedVariant);
	}

	public boolean isModified() {
		return modified;
	}

	public void validate() {
		if (!validateSelectedVariant(selectedVariant)) {
			setSelectedVariant(Variants.BASIC);
		}
	}
	
	public static Config load(File dir) {
		Config config = new Config(dir);
		try {
			JSONCodec codec = new JSONCodec(jsonConfig);
			InputStream in = new FileInputStream(config.file);
			config = (Config) codec.decodeObject(in, config);
			in.close();
		} catch (IOException | JSONCodecException e) {
			Log.warn("Failed to load config '" + config.file + "'. Resetting to default config.");
			config = new Config(dir);
		}
		
		config.validate();
		
		return config;
	}

	public void save() {
		if (canSave) {
			try {
				
				JSONCodec codec = new JSONCodec(jsonConfig);
				FileOutputStream out = new FileOutputStream(this.file);
				codec.encodeObject(this, out);
				out.close();
				modified = false;
			} catch (IOException | JSONCodecException e) {
				Log.error("Failed to save configuration to '" + this.file + "'. Saving disabled.");
				canSave = false;
			}
		}
	}

	public GameConfig getGameConfig(GameTypes selectedGameType, Variants variant) {
		if (configs != null) {
			for (GameConfig v : configs) {
				if (v.getName().equals(toConfigName(selectedGameType, variant))) {
					return v;
				}
			}
		}
		return null;
	}

	public GameConfig addGameConfig(GameTypes type, Variants variant) {
		String configName = toConfigName(type, variant);
		if (configs == null) {
			configs = new GameConfig[1];
		} else {
			GameConfig[] tmp = new GameConfig[configs.length+1];
			System.arraycopy(configs, 0, tmp, 0, configs.length);
			configs = tmp;
		}
		
		GameConfig gameConfig = new GameConfig(configName, toProfileName(variant));
		configs[configs.length-1] = gameConfig;
		modified = true;
		return gameConfig;
	}

	private String toProfileName(Variants variant) {
		return Launcher.INSTANCE.createStandardProfileName(variant.toString());
	}

	private String toConfigName(GameTypes selectedGameType, Variants variant) {
		return Launcher.INSTANCE.createStandardProfileName(selectedGameType.toString() + "." + variant.toString());
	}

	public String getWorkDir() {
		return workdir;
	}

	public GameTypes getSelectedType() {
		return GameTypes.get(this.selectedType);
	}

	public void setWorkDir(String workdir) {
		if (!workdir.equals(this.workdir)) {
			this.workdir = workdir;
			modified = true;
		}
	}

	public boolean isOffline() {
		return offline;
	}

	public String getDownloadFolder() {
		return downloadFolder;
	}

	public void setDownloadFolder(String downloadFolder) {
		if (downloadFolder != this.downloadFolder && downloadFolder != null && !downloadFolder.equals(this.downloadFolder)) {
			this.downloadFolder = downloadFolder;
			modified = true;
		}
	}

	public boolean isDontShowLaunchHint() {
		return dontShowLaunchHint;
	}

	public void setDontShowLaunchHint(boolean dontShowLaunchHint) {
		if (this.dontShowLaunchHint != dontShowLaunchHint) {
			this.dontShowLaunchHint = dontShowLaunchHint;
			modified = true;
		}
	}

	public boolean isDontShowShaderHint() {
		return dontShowShaderHint;
	}

	public void setDontShowShaderHint(boolean dontShowShaderHint) {
		if (this.dontShowShaderHint != dontShowShaderHint) {
			this.dontShowShaderHint = dontShowShaderHint;
			modified = true;
		}
	}

	public boolean isDontShowAdWarning() {
		return dontShowAdWarning;
	}

	public void setDontShowAdWarning(boolean dontShowAdWarning) {
		if (this.dontShowAdWarning != dontShowAdWarning) {
			this.dontShowAdWarning = dontShowAdWarning;
			modified = true;
		}
	}

	public boolean isShowGameLog() {
		return showGameLog;
	}

	public void setShowGameLog(boolean showGameLog) {
		this.showGameLog = showGameLog;
	}

	public String getLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(String launcherVersion) {
		lastVersion = launcherVersion;
	}

	public String[] getServerPool() {
		return serverPool;
	}

	public boolean setDownShowUpgradeWarning(boolean selected) {
		return dontShowUpgradeWarning;
	}

	public boolean isDontShowUpgradeWarning() {
		return dontShowUpgradeWarning;
	}

}
