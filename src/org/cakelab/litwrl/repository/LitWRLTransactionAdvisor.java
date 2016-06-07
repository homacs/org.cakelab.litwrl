package org.cakelab.litwrl.repository;

import java.io.FileNotFoundException;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.update.TransactionFallThrough;
import org.cakelab.omcl.update.TransportException;
import org.cakelab.omcl.update.URLPath;
import org.cakelab.omcl.update.UpdateServer;

public class LitWRLTransactionAdvisor extends UpdateServer.DefaultTransactionAdvisor {

	private static final boolean treatCorruptedFilesAsNoLongerExisting = true;

	
	@Override
	public void checkRetry(URLPath location, int i, Throwable e) throws Throwable {
		if (e instanceof FileNotFoundException) throw new TransactionFallThrough(e);
		if (treatCorruptedFilesAsNoLongerExisting && e instanceof JSONCodecException) {
			throw new TransactionFallThrough(new FileNotFoundException(e.getMessage()));
		}
		super.checkRetry(location, i, e);
	}

	@Override
	public void validate(URLPath location, PackageDescriptor descriptor) throws Throwable {
		// We need this method to verify data received from a web server which sends
		// valid HTTP replies even if the requested document does not exist!
		// TODO: kick Tim in his butt to fix his web server!
		
		if (descriptor.location != null && !descriptor.location.equals(location.toString())) {
			String message = "received invalid descriptor:"
					+ "\n\trequested: " + location
					+ "\n\treceived:  " + descriptor.location;
			if (treatCorruptedFilesAsNoLongerExisting) {
				throw new FileNotFoundException(message);
			}
			else throw new TransportException(message);
		} else {
			String version = location.getLast();
			if (version == null || !version.equals(descriptor.version)) {
				String message = "received descriptor with invalid version field"
						+ "\n\trequested: " + version
						+ "\n\treceived:  " + descriptor.version;
				if (treatCorruptedFilesAsNoLongerExisting) {
					throw new FileNotFoundException(message);
				}
				else throw new TransportException(message);
			}
		}
		super.validate(location, descriptor);
	}

	@Override
	public void validate(URLPath location, Versions versions) throws Throwable {
		super.validate(location, versions);
	}

}
