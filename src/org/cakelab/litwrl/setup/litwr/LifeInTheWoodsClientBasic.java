package org.cakelab.litwrl.setup.litwr;

import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.omcl.repository.PackageDescriptor;

public class LifeInTheWoodsClientBasic extends LifeInTheWoodsClient {


	public LifeInTheWoodsClientBasic(LitWRSetupParams params, PackageDescriptor pd, LitWRLRepository repository) {
		super(params, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		super.init();
	}

}
