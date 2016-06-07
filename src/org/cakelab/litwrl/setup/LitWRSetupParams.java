package org.cakelab.litwrl.setup;

import java.io.File;
import java.util.Arrays;

import org.cakelab.litwrl.config.Variants;
import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.setup.SetupParameters;

public class LitWRSetupParams extends SetupParameters {

	public Variants variant;

	public LitWRSetupParams(GameConfig gameConfig, File workdir, File gamedir,
			String version, boolean keepVersion, GameTypes type, Variants variant, String javaArgs, String shader, String[] optionals) {
		super(gameConfig, workdir, gamedir, version, keepVersion, type, javaArgs, shader, optionals);
		this.variant = variant;
	}

	@Override
	public String toString() {
		return "LitWRSetupParams [variant=" + variant + ", gameConfig="
				+ gameConfig + ", workdir=" + workdir + ", gamedir=" + gamedir
				+ ", type=" + type + ", version=" + version + ", keepVersion="
				+ keepVersion + ", javaArgs=" + javaArgs + ", shader=" + shader
				+ ", optionals=" + Arrays.toString(optionals) + "]";
	}


}
