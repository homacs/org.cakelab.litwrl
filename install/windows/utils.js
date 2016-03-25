/**
 * This is an utility script for the Launcher of Life in the 
 * Woods Renaissance.
 * 
 * Synopsis:
 *   cscript utils.js [mode]
 * 
 *   mode refers to one of two execution modes:
 * 
 *   --install: This tests whether a suitable Java 
 *      installation is available and creates a shortcut 
 *      on the Desktop, if the user acknowledges.
 *   --run: This determines a suitable Java installation
 *      and uses it to start the launcher of LitWR.
 *    
 * In the following configuration section you can set 
 * various parameters to be fixed. This is for example useful
 * if the script is not able to find a suitable java 
 * installation (see JAVAW).
 * 
 * -homac 2016
 */



/*****************************************************************/
/********************* CONFIGURATION SECTION *********************/
/*****************************************************************/

/**
 * JAVAW
 * =====
 * Path to the java executable used to start the launcher of LitWR.
 * If this parameter is not set, the script will search for a 
 * suitable Java installation on the system in typical installation
 * directories.
 * You can set a fixed value like this
 * var JAVAW = "D:\\MySpecialJavaFolder\\bin\\javaw.exe"
 */
var JAVAW = "";

/**
 * TARGET
 * ======
 * This parameter specifies the location of the launcher folder
 * which was downloaded and unzipped by the user and contains the
 * jar file litwrl.jar.
 * This script usually considers TARGET to be the parent directory
 * if its own location.
 * You can set a different path like this:
 * var TARGET = "C:\\My Documents\\LifeInTheWoodsRenaissanceLauncher"
 */
var TARGET = "";

/**
 * DESKTOP
 * =======
 * This is the location where the script will create a short cut.
 * If not set, the script will set it to point to the users desktop
 * directory.
 * You can set a different value like this:
 * var DESKTOP = "F:\\MyShortCutFolder"
 */
var DESKTOP = "";

/**
 * This parameter will contain the architecture type. The architecture 
 * type is considered during selection of a suitable Java installation
 * (i.e. to check whether a 64bit Java installation will be benefitial).
 * 
 * The following values are accepted:
 * "AMD64" : all 64bit x86 architectures (Intel, AMD, etc.)
 * "x86"   : all non-64bit x68 architectures
 * "IA64"  : Intel "Itanium" architecture (64bit).
 */
var ARCH; // AMD64/IA64/x86




/**
 * CSCRIPT
 * =======
 * This points to the cscript.exe in the system. 
 * If not set, the script will determine its location by itself.
 */
var CSCRIPT = "";

/**
 * Set this to 'true' to get debug output on stdout.
 */
var DEBUG = false;

/*****************************************************************/
/***************** END OF CONFIGURATION SECTION ******************/
/*****************************************************************/






var fso = new ActiveXObject("Scripting.FileSystemObject");
var shell = WScript.CreateObject("WScript.Shell");


var ARCH64; // Boolean
var JAVA_PROPERTIES = null;


function log(msg) {
	if (DEBUG) {
		WScript.Echo(msg);
	}
	shell.LogEvent(0, msg);
}


function showInfo(message) {
	shell.Popup(message, 0 /* wait */,
			"Life in the Woods Renaissance Launcher", 
			4096 /* topmost */ + 64 /*INFO icon*/);
}

function showAsk(message) {
	return shell.Popup(message, 0 /* wait */,
			"Life in the Woods Renaissance Launcher", 
			4096 /* topmost */ + 32 /*question mark*/ + 1 /* OK| Cancel*/ );
}

function showError(message) {
	shell.Popup(message, 0 /* wait */,
			"Life in the Woods Renaissance Launcher", 
			4096 /* topmost */ + 48 /*Exclamation mark*/);
}


function exec(cmd) {
	var process = shell.Exec(cmd);
	while (process.Status != 1) { WScript.Sleep(100); }
	return (process);
}


function isSymbolicLink (file) {
	var f = fso.GetFile(file);
	return f.Attributes & 1024;
}

function getLinkTarget (link) {
	var result = exec("cmd /C dir " + link);
	var target;
	if (result.ExitCode == 0) {
		var out = result.StdOut;
		while (!out.AtEndOfStream) {
			line = out.ReadLine();
			target = line.substring(0, line.lastIndexOf("]"));
			target = target.substring(line.lastIndexOf("[")+1);
			if (target.length > 0) break;
		}
	}
	if (isSymbolicLink(target)) {
		target = getLinkTarget(target);
	}
	return target;
}

function getFullExePath(cmd) {
	var where = exec("where " + cmd);
	if (where.ExitCode == 0) {
		var out = where.StdOut;
		var result;
		while (!out.AtEndOfStream) {
			filespec = out.ReadLine();
			if (fso.FileExists(filespec)) {
			   var extension = fso.GetExtensionName(filespec);
			   if (isSymbolicLink(filespec)) {
					if (extension.toUpperCase() == "LNK") {
						log(2, "shortcut --> ignored");
						continue;
					} else {
						filespec = getLinkTarget(filespec);
					}
			   }
			   return filespec;
			}
		}
	}
	return null;
}



function getJavaVersion(props, line) {
	props.Item("version.full") = line;
	props.Item("version.major") = 0;
	props.Item("version.minor") = 0;
	props.Item("version.build") = 0;
	props.Item("version.revision") = 0;



	var end = line.indexOf(".");
	if (end < 0) return;
	var s = line.substring(line.indexOf("\"")+1, end);
	props.Item("version.major") = new Number(s).valueOf();
	line = line.substr(end+1);
	
	log( "major: " + s);
	
	end = line.indexOf(".");
	if (end < 0) return;
	s = line.substr(0, end);
	props.Item("version.minor") = new Number(s).valueOf();
	line = line.substr(end+1);

	log( "minor: " + s);
	
	end = line.indexOf("_");
	if (end < 0) return;
	s = line.substr(0, end);
	props.Item("version.build") = new Number(s).valueOf();
	line = line.substr(end+1);

	log( "build: " + s);

	end = line.indexOf("\"");
	if (end < 0) return;
	s = line.substr(0, end);
	props.Item("version.revision") = new Number(s).valueOf();
	line = line.substr(end+1);
	
	log( "revision: " + s);
}

function isVersionGE(v1, v2) {
	var key = "version.major"
	if (v1.Item(key) > v2.Item(key)) {
		return true;
	} else if (v1.Item(key) == v2.Item(key)) {
		key = "version.minor"
		if (v1.Item(key) > v2.Item(key)) {
			return true;
		} else if (v1.Item(key) == v2.Item(key)) {
			key = "version.build"
			if (v1.Item(key) > v2.Item(key)) {
				return true;
			} else if (v1.Item(key) == v2.Item(key)) {
				key = "version.revision"
				if (v1.Item(key) >= v2.Item(key)) {
					return true;
				}
			} else {
			}
		}
	}
	log( v1.Item("version.full") + " < " + v2.Item("version.full"));
	return false;
}

function isArchSupported(props) {
	if (ARCH64) {
		return props.Item("is64bit");
	}
	return true;
}


function getJVMProperties (javaexe) {
	var props = new ActiveXObject("Scripting.Dictionary");
	props.add("exe", javaexe);
	var process = exec(javaexe + " -version");
	if (process.ExitCode == 0) {
		var out = process.StdOut;
		if (out.AtEndOfStream) out = process.StdErr;
		while (!out.AtEndOfStream) {
			var line = out.ReadLine();
			if (line.indexOf("java version") != -1) {
				getJavaVersion(props, line);
			} else if (line.toLowerCase().indexOf("64-bit") != -1) {
				props.add("is64bit", true);
			}
		}
	} else {
		WScript.Echo("failed exec of: " + javaexe + " -version");
	}
	return props;
}




function getJavaExecutables() {
	var list = new Array();
	

	var where = exec("where java");
	if (where.ExitCode == 0) {
		var out = where.StdOut;
		var result;
		while (!out.AtEndOfStream) {
			filespec = out.ReadLine();
			if (fso.FileExists(filespec)) {
			   var extension = fso.GetExtensionName(filespec);
			   if (isSymbolicLink(filespec)) {
					if (extension.toUpperCase() == "LNK") {
						log(2, "shortcut --> ignored");
						continue;
					} else {
						filespec = getLinkTarget(filespec);
					}
			   }
			}
			list.push(filespec);
		}
	}
	
	var dir = shell.ExpandEnvironmentStrings("%ProgramFiles%\\Java");
	if (fso.FolderExists(dir)) {
		var folder = fso.GetFolder(dir);
		var it = new Enumerator(folder.SubFolders);
		for (; !it.atEnd() ; it.moveNext()) {
			folder = it.item();
			if (fso.FileExists(folder.Path + "\\jre\\bin\\java.exe")) {
				list.push(folder.Path + "\\jre\\bin\\java.exe");
			} else if (fso.FileExists(folder.Path + "\\bin\\java.exe")) {
				list.push(folder.Path + "\\bin\\java.exe");
			}
		}
	}
	if (ARCH64) {
		var dir = shell.ExpandEnvironmentStrings("%ProgramFiles(x86)%\\Java");
		if (fso.FolderExists(dir)) {
			var folder = fso.GetFolder(dir);
			var it = new Enumerator(folder.SubFolders);
			for (; !it.atEnd() ; it.moveNext()) {
				folder = it.item();
				if (fso.FileExists(folder.Path + "\\jre\\bin\\java.exe")) {
					list.push(folder.Path + "\\jre\\bin\\java.exe");
				} else if (fso.FileExists(folder.Path + "\\bin\\java.exe")) {
					list.push(folder.Path + "\\bin\\java.exe");
				}
			}
		}
	}
	return list;
}


function getJava() {
	var minimum = new ActiveXObject("Scripting.Dictionary");
	getJavaVersion(minimum, "\"1.8.0\"");
	
	var selected = minimum;
	
	var list = getJavaExecutables();
	if (list.length > 0) {
		while (list.length > 0) {
			filespec = list.pop();
			log("checking: " + filespec);
			if (fso.FileExists(filespec)) {
				var props = getJVMProperties(filespec);
				if (isArchSupported(props) && isVersionGE(props, selected)) 
				{
					selected = props;
				}
			}
		}
	}

	if (selected == minimum) {
		var javaSpec = "Oracle Java SE 1.8";
		if (ARCH64) {
			javaSpec = javaSpec + " 64bit";
		}
		showError("\nYou need to have at least\n\n" + javaSpec + "\n\ninstalled.\n");
		return null;
	}
	
	//
	// Try to get javaw.exe instead of java.exe
	//
	var exe = selected.Item("exe");
	JAVA_PROPERTIES = selected;
	var javawexe = fso.GetFile(exe).ParentFolder.Path + "\\javaw.exe";
	if (fso.FileExists(javawexe)) {
		return javawexe;
	} else {
		return exe;
	}
	
}	

function init() {
	if (ARCH == null || ARCH == "") {
		ARCH = shell.ExpandEnvironmentStrings("%PROCESSOR_ARCHITECTURE%");
	}

	if (ARCH == "AMD64" || ARCH == "IA64") {
		ARCH64 = true;
	} else {
		ARCH64 = false;
	}
	
	if (JAVAW == null || JAVAW == "") {
		JAVAW = getJava();
	}
	if (TARGET == null || TARGET == "") {
		TARGET = fso.GetParentFolderName(WScript.ScriptFullName);
		TARGET = fso.GetParentFolderName(TARGET);
	}
	
	
	log("JAVAW: " + JAVAW);
	log("TARGET: " + TARGET);
	log("ARCH64: " + ARCH64);
}

function install () {
	
	init();
	if (DESKTOP == null || DESKTOP == "") {
		DESKTOP = shell.ExpandEnvironmentStrings("%HOMEDRIVE%%HOMEPATH%\\Desktop")
	}
	if (CSCRIPT == null || CSCRIPT == "") {
		CSCRIPT = getFullExePath("cscript");
	}
	log( "DESKTOP: " + DESKTOP);
	log( "CSCRIPT: " + CSCRIPT);
	
	
	
	if (JAVAW == null) return;
	else {
		var reason = null;
		if (!isArchSupported(JAVA_PROPERTIES)) {
			reason = "You have a 64bit system but the installed JVM\n" +
						"is made for 32bit systems."
		}
		
		if (JAVA_PROPERTIES.Item("version.minor") < 8) {
			if (reason == null) {
				reason = "Your Java installation is outdated.";
			} else {
				reason += "\nAlso, your Java installation is outdated.";
			}
		}
		
		if (reason != null) {
			var javaSpec = "Oracle Java SE 1.8";
			if (ARCH64) {
				javaSpec = javaSpec + " 64bit";
			}
			showInfo("\nThe script has found a suitable Java installation. However, " +
					"it is not optimal. Reason: \n\n" +
					reason + "\n\nConsider upgrading to " 
					+ javaSpec + ".");
		}
	}
	
	var sLinkFile = DESKTOP + "\\Life in the Woods Renaissance.LNK";
	var sArgs = "//B \"" + TARGET + "\\windows\\utils.js\" --run";
	var answer = showAsk("The installation script will now create a shortcut on your Desktop.\n\nDo you want to proceed?");
	
	if (answer == 1 /* OK */) {
		var oLink = shell.CreateShortcut(sLinkFile);
		oLink.TargetPath = "\"" + CSCRIPT + "\"";
		oLink.Arguments = sArgs;
		oLink.Description = "Starts the launcher of Life in the Woods Renaissance";
		oLink.IconLocation = TARGET + "\\utils\\appicon.ico, 0";
		oLink.WindowStyle = "0";
		oLink.WorkingDirectory = TARGET;
		oLink.Save();
		
		showInfo("Successfully created shortcut: \n\n'" + sLinkFile + "'.\n\n"
				+ "You can now move the link to any location you like.");
	}
}


function unset(name) {
	var env = shell.Environment("PROCESS");
	var value = env(name);
	if (value != null && value != "") {
		env.Remove(name);
	}
	env = shell.Environment("USER");
	value = env(name);
	if (value != null && value != "") {
		log( "USER: " + value);
		env.Remove(name);
	}
	env = shell.Environment("SYSTEM");
	value = env(name);
	if (value != null && value != "") {
		env.Remove(name);
	}
	env = shell.Environment("VOLATILE");
	value = env(name);
	if (value != null && value != "") {
		env.Remove(name);
	}
}

function run () {
	init();
	
	
	if (JAVAW == null) return;
	
	var sLinkFile = TARGET + "\\Life in the Woods Renaissance.LNK";
	var sJarfile = TARGET + "\\litwrl.jar";
	var sJvmArgs = "";
	var sArgs = sJvmArgs + " -jar \"" + sJarfile + "\"";

	// prevent us from having an incorrect JAVA_HOME
	unset("JAVA_HOME");
	
	shell.Run("\"" + JAVAW + "\" " + sArgs);
	
}


var args = WScript.Arguments;
if (args.length == 0 || args(0) == "--install") {
	install();
} else if (args(0) == "--run") {
	run();
}