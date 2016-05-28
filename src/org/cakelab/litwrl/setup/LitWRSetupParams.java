package org.cakelab.litwrl.setup;

import java.io.File;

import org.cakelab.litwrl.config.Variants;
import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.setup.SetupParameters;

public class LitWRSetupParams extends SetupParameters {

	public Variants variant;

	public LitWRSetupParams(GameConfig gameConfig, File workdir, File gamedir,
			String version, boolean keepVersion, GameTypes type, Variants variant, String javaArgs, String shader) {
		super(gameConfig, workdir, gamedir, version, keepVersion, type, javaArgs, shader);
		this.variant = variant;
	}

}
