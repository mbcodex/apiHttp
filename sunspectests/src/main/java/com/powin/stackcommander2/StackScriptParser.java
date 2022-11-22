//package com.powin.stackcommander2;
//
//import java.io.IOException;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.json.simple.parser.ParseException;
//
//import com.powin.modbus.ModbusException;
//import com.powin.modbusfiles.apps.PowerApps;
//
//public class StackScriptParser {
//	private static final int STEP_IDX = 8;
//	private final static Logger LOG = LogManager.getLogger();
//	private static IStackCommandExecutor scd;
//
//	public StackScriptParser() {
//	}
//
//	private String normalizeInput(final String line) {
//		final String s = line.replace("Activate cell balancing", "Balancing ON").replace("Deactivate cell balancing",
//				"Balancing OFF");
//		return s.trim();
//	}
//
//	/**
//	 * Parse the line of script into an Executor. Some normalization is done to
//	 * account for variations in input syntax, for example "Deactivate cell
//	 * balancing" is converted to "Balancing OFF" just to minimize the cases to
//	 * handle.
//	 *
//	 * @param script
//	 * @return
//	 */
//	public List<Executor> parse(final List<String> script) {
//		boolean isCSV = false;
//		final List<Executor> executors = new ArrayList<>();
//		if (script.get(0).contains("Action")) {
//			script.remove(0);
//			isCSV = true;
//		}
//		for (String line : script) {
//			line = normalizeInput(line);
//			final Executor exec = new Executor();
//			Pattern pattern = Pattern.compile("(" + Command.toRegEx(isCSV) + ")", Pattern.CASE_INSENSITIVE);
//			Matcher matcher = pattern.matcher(line);
//			if (matcher.find()) {
//				final Command c = Command.fromString(matcher.group(1));
//				exec.setCommand(c);
//			}
//
//			if (line.contains("Repeat")) {
//				final Directive d = Directive.step;
//				if (line.contains("step")) {
//					d.setRepeatStep(Integer.parseInt(line.split("step")[1].trim().split(" ")[0]));
//				} else {
//					d.setRepeatStep(Integer.parseInt(line.split(",")[STEP_IDX]));
//				}
//				exec.setDirective(d);
//			} else if (exec.getCommand() == Command.Title) {
//				final Directive d = Directive.Undefined;
//				d.setTitle(line.split("Title")[1].replace(",", ""));
//				exec.setDirective(d);
//			}  else {
//				pattern = Pattern.compile("(" + Directive.toRegEx() + ")", Pattern.CASE_INSENSITIVE);
//				matcher = pattern.matcher(line);
//				while (matcher.find()) {
//					final Directive d = Directive.fromString(matcher.group(1));
//					exec.setDirective(d);
//				}
//			}
//            /* Condition */
//			pattern = Pattern.compile("(" + Condition.toRegEx() + ")", Pattern.CASE_INSENSITIVE);
//			matcher = pattern.matcher(line);
//			if (matcher.find()) {
//				final Condition p = Condition.fromString(matcher.group(1));
//				exec.setCondition(p);
//			}
//            /* Power */
//			pattern = Pattern.compile("(" + Power.toRegEx() + ")", Pattern.CASE_INSENSITIVE);
//			matcher = pattern.matcher(line);
//			if (matcher.find()) {
//				final Power p = Power.fromString(matcher.group(1));
//				exec.setPower(p);
//			}
//            /* Duration */
//			pattern = Pattern.compile("(" + Duration.toRegEx() + ")", Pattern.CASE_INSENSITIVE);
//			matcher = pattern.matcher(line);
//			if (matcher.find()) {
//				final Duration d = Duration.fromString(matcher.group(1));
//				if (exec.getDirective().contains(Directive.OR) && line.contains("until")) {
//					exec.setDuration(Duration.fromString("2h"));
//				} else {
//					exec.setDuration(d);
//				}
//			}
//			exec.setStatus();
////			assert exec.getStatus().equals(Executor.Status.READY.toString())
////					: String.format("%s\n%s is not Ready", line, exec);
//			executors.add(exec);
//		}
//
//		return executors;
//	}
//
//
//}
