package mbTest.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ScriptHelper {
    private final static Logger LOG = LogManager.getLogger();

	public static void runScriptLocally(String command) {
		OutputStream out1=null;
		try {

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(CommonHelper.user, CommonHelper.host, CommonHelper.port);
			session.setPassword(CommonHelper.password);
			session.setConfig(config);
			session.connect();
			System.out.println("Connected");

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setPty(true);
			((ChannelExec) channel).setCommand(command);

			//channel.getInputStream();
			out1 = channel.getOutputStream();
			((ChannelExec) channel).setErrStream(System.err);
			channel.connect();
			out1.write((CommonHelper.password + "\n").getBytes());
			out1.flush();
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			//channel.getOutputStream();
			channel.setOutputStream(System.out, true);
			channel.setExtOutputStream(System.err, true);

			InputStream in = channel.getInputStream();
			channel.connect();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					System.out.println("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					CommonHelper.quietSleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
			System.out.println("DONE");
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (null != out1)
				   out1.close();
			} catch (IOException e) {
				LOG.error("exception caught", e);
				throw new RuntimeException(e);
			}
		}
         
	}

	public static void runScriptRemotely(String user, String host, int port, String password, String command) {

		OutputStream out1=null;
		InputStream in = null;
		try {

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			System.out.println("Connected");

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setPty(true);
			((ChannelExec) channel).setCommand(command);

			//channel.getInputStream();
			out1 = channel.getOutputStream();
			((ChannelExec) channel).setErrStream(System.err);
			channel.connect();
			out1.write((password + "\n").getBytes());
			out1.flush();
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			//channel.getOutputStream();

			channel.setOutputStream(System.out, true);
			channel.setExtOutputStream(System.err, true);

			in = channel.getInputStream();
			channel.connect();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					System.out.println("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					CommonHelper.quietSleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
			System.out.println("DONE");
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.info("connection reset");
		} finally {
			try {
				if (null != out1) {
					out1.close();
				}
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				LOG.error("exception caught", e);
				throw new RuntimeException(e);
			}
		}

	}

	public static void runScriptRemotelyOnKobold(String user, String password, String command) {
		try {
			runScriptRemotely(user, PowinProperty.CLOUDHOST.toString(), CommonHelper.port, password, command);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
    
	/**
	 * For use by UI apps - The configuration won't be accessible so those values need to be passed in.
	 * @param command
	 * @param isLocal
	 * @param host
	 * @param user
	 * @param password
	 * RAF
	 */
	public static void runScriptOnClient(String command, boolean isLocal, String host, String user, String password ) {
		if (isLocal) {
			String cmd = " echo " + password + " | sudo -S  " + command;
			//ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", cmd);
			runScriptLocally(command);
			
		} else {
			//ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", "sshpass -p powin ssh powin@"+host + " '"+command+"'");
			runScriptRemotely(user, host, 22, password, command);
		}
	}
	
	public static void runScriptRemotelyOnTurtle(String command) {
//		if (Constants.LOCAL_HOST.equals(Constants.TURTLE_HOST)) {
//			String cmd = " echo " + Constants.PW + " | sudo -S  " + command;
//			ScriptHelper.executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", cmd);
//		} else {
			try {
				runScriptRemotely(CommonHelper.user, CommonHelper.host, CommonHelper.port, CommonHelper.password,
						command);
			} catch (Exception e) {
				e.printStackTrace();
			}
//		}
	}
	public static List<String> executeProcess(boolean logresponse, boolean wait, boolean captureOutput,
			String... executableFile) {
		List<String> ret = new ArrayList<>();
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(executableFile);
			CommonHelper.LOG.info(Arrays.asList(executableFile).toString());
			ProcessBuilder pb = new ProcessBuilder(executableFile);
			Process process = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			if (captureOutput) {
				String line;
				while ((line = reader.readLine()) != null) {
					ret.add(line);
					if (logresponse) {
						CommonHelper.LOG.info(line);
					}
				}
			}
			if (wait) {
				process.waitFor();
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e.getMessage());
		}
		// LOG.info(Arrays.asList(ret).toString());
		return ret;
	}

	/**
	 * TODO create a System class for these system functions Executes a program or
	 * shell script. example: executeProcess("sh", "-c", "echo " + Constants.USER +
	 * " | sudo -S service tomcat restart");
	 *
	 * @param executableFile
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void executeProcess(String... executableFile) {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(executableFile);
		CommonHelper.LOG.info(Arrays.asList(executableFile).toString());
		pb = new ProcessBuilder(executableFile);
		try {
			Process p = pb.start();
			p.waitFor();
		} catch (InterruptedException | IOException e) {
			CommonHelper.LOG.error("can't execute process", e);
		}
	}

	/**
	 * Execute the command on the specified remote host. If the remote host is
	 * localhost don't bother with ssh
	 *
	 * @param host
	 * @param user
	 * @param pw
	 * @param command
	 * @return
	 */
	public static List<String> executeRemoteSSHCommand(String host, String user, String pw, String command) {
		if (Constants.LOCAL_HOST.equals(host)) {
			return executeProcess(Constants.LOG, Constants.WAIT, Constants.CAPTURE, "sh", "-c", command.trim());
		} else {
			return executeProcess(Constants.NOLOG, Constants.WAIT, Constants.CAPTURE, "sshpass", "-p", pw, "ssh",
					user + "@" + host, command);
		}
	}

	/**
	 * Move a shell script from resources to the home directory and execute it.
	 *
	 * @param scriptFilename
	 * @throws IOException
	 */
	public static void executeScriptFromResources(String scriptFilename) throws IOException {
		String localScriptFile = FileHelper.copyScriptFileToLocalHome(scriptFilename);
		executeProcess(localScriptFile);
		CommonHelper.waitForFile(localScriptFile, 10);
	}

	/**
	 * Execute Remote bash command Tested using touch, rm, mv
	 *
	 * @param host
	 * @param username
	 * @param params
	 * @param newParam TODO
	 */
	public static List<String> executeSimpleRemote(String host, String username, String pw, String cmd,
			String... params) {
		String command = " " + cmd + " " + String.join(" ", params);
		CommonHelper.LOG.info("{}", command);
		return executeRemoteSSHCommand(host, username, pw, command);
	}

	public static void callScriptSudo(String password, String shellScriptPath) {
		String[] cmd = { "sh", "-c", "echo " + password + "| sudo -S " + shellScriptPath };
		try {
			Process pb = Runtime.getRuntime().exec(cmd);
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
		} catch (IOException e) {
			CommonHelper.LOG.error(e);
			throw new RuntimeException(e.getMessage());
		}
	}

}
