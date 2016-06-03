package org.cakelab.litwrl.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.update.ServerLockedException;
import org.cakelab.omcl.update.TransportException;
import org.cakelab.omcl.update.URLPath;
import org.cakelab.omcl.update.UpdateServer;
import org.cakelab.omcl.utils.json.JsonSaveTask;
import org.cakelab.omcl.utils.log.Log;





/**
 * All methods with the prefix "fetch" force to contact the
 * update server and fetch a fresh file.
 * 
 * All methods with prefix "getLocal" refer to local data only.
 * 
 * 
 * @author homac
 *
 */
public class LitWRLRepository extends Repository {

	
	public static final String VERSIONS_FILE = "versions.json";

	public LitWRLRepository(UpdateServer updateServer, File root) {
		super(updateServer, root);
	}

	
	public void init() throws IOException, JSONCodecException, ServerLockedException, TransportException {
		// nothing litwrl specific
		super.init(true);
	}

	

	@Override
	protected void finishUpdate(ArrayList<JsonSaveTask> saveables) throws TransportException, ServerLockedException {
		// 
		// Now add all new versions files that might have been added on server side.
		//
		for (GameTypes type : GameTypes.values()) {
			for (Variants variant : Variants.values()) {
				String baseLocation = getBaseLocationForLitWRParams(type, variant);
				File f = getLocalVersionsFile(baseLocation);
				// if it exists locally then we have already fetched the new version above.
				// if not, then fetch it now.
				if (!f.exists()) {
					URLPath baseLocationPath = new URLPath(baseLocation);
					if (updateServer.exists(baseLocationPath.append(Versions.FILENAME))) {
						Versions versions = updateServer.getVersions(baseLocationPath);
						saveables.add(new JsonSaveTask(versions, f));
					} else {
						Log.info("type: " + type.name() + " variant: " + variant.name() + " not yet supported");
					}
				}
			}
		}
	}


	public String getLocationForLitWRParams(GameTypes type,
			Variants variant, String version) {
		return getBaseLocationForLitWRParams(type, variant) 
				+ File.separator + version;
	}
	
	public String getBaseLocationForLitWRParams(GameTypes type,
			Variants variant) {
		return type.toString().toLowerCase() 
				+ File.separator + variant.toString().toLowerCase();
	}
	
	
	public Versions getLocalLitWRVersions(GameTypes type, Variants variant) {
		String baseLocation = getBaseLocationForLitWRParams(type, variant);
		try {
			return Versions.load(getLocalVersionsFile(baseLocation));
		} catch (JSONCodecException | IOException e) {
			return null;
		}
	}

	
	/**
	 * This method fetches latest meta-infos related to dependencies of
	 * a particular LitWR type and variant.
	 * 
	 * If necessary it also triggers an update of the repository
	 * to provide a consistent result.
	 * 
	 * @param type
	 * @param variant
	 * @param version
	 * @return
	 * @throws ServerLockedException 
	 * @throws TransportException 
	 * @throws JSONCodecException 
	 * @throws IOException 
	 * @throws Exception 
	 */
	public PackageDescriptor fetchLitWRDependencies(GameTypes type,
			Variants variant, String version) throws TransportException, ServerLockedException, IOException, JSONCodecException {
		
		String packageLocation = getLocationForLitWRParams(type, variant, version);
		
		if (!updateServer.isOffline()) {
			/* 
			 * first try to update the repository for a new version
			 */
			int remoteRevision;
			try {
				ArrayList<JsonSaveTask> saveables = new ArrayList<JsonSaveTask>();
				do {
					remoteRevision = tx.start();
					saveables.clear();
					resolveDependencies(packageLocation, saveables);
				} while (!tx.commit());

				//
				// once we have successfully received new files, we can save it.
				//
				for (JsonSaveTask s : saveables) {
					s.save();
				}

				//
				// now it is possible, that the remote repository is already at another revision
				// so, we have to update our local mirror.
				//
				if (remoteRevision > revision) {
					updateRepository();
				}
			} catch (Throwable t) {
				tx.abortAndThrow(t);
			}
		}
		// TODO: move this check for missing dependencies in a server tool
		PackageDescriptor descriptor = getLocalPackageDescriptorFromLocation(packageLocation);
		if (descriptor != null) {
			Set<String> missingDependencies = new HashSet<String> ();
			if (checkDependencies(descriptor, missingDependencies)) {
				StringBuffer msg = new StringBuffer("Missing dependencies.");
				for (String dependency : missingDependencies) {
					msg.append(" ").append(dependency);
				}
				throw new IOException(msg.toString());
			}
		}
		return descriptor;
	}

	public PackageDescriptor getLocalLitWRPackageDescriptor(GameTypes type,
			Variants variant, String version) throws Exception {
		String packageLocation = getLocationForLitWRParams(type, variant, version);
		return getLocalPackageDescriptorFromLocation(packageLocation);
	}







}
