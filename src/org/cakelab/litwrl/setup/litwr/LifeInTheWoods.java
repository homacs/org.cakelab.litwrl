package org.cakelab.litwrl.setup.litwr;

import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.omcl.repository.PackageDescriptor;

public class LifeInTheWoods {


	public static LifeInTheWoodsClient getSetupService(PackageDescriptor pd, LitWRSetupParams params, LitWRLRepository repository) {

		
		switch(params.type) {
		case CLIENT:
			switch(params.variant) {
			case BASIC:
				return new LifeInTheWoodsClientBasic(params, pd, repository);
			case HUNGRY:
				return new LifeInTheWoodsClientHungry(params, pd, repository);
			}
			break;
		case SERVER:
			break;
		}
		
		return null;
	}

	public static LaunchService getLaunchService(PackageDescriptor pd, LitWRSetupParams setup, LitWRLRepository repository) {
		return getSetupService(pd, setup, repository);
		
	}
	

	

	



	
}
