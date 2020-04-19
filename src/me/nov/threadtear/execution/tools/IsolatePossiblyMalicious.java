package me.nov.threadtear.execution.tools;

import java.util.ArrayList;

import me.nov.threadtear.asm.Clazz;
import me.nov.threadtear.asm.util.Instructions;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;

public class IsolatePossiblyMalicious extends Execution {

	private static final String POSSIBLY_MALICIOUS_REGEX = "(java/lang/runtime/Runtime|java/lang/reflect/|com/sun/jna|java/nio/file|java/io/File|sun/misc/Unsafe|java/io/.*Writer|java/io/.*Reader).*";
	private int changed;

	public IsolatePossiblyMalicious() {
		super(ExecutionCategory.TOOLS, "Isolate dangerous calls", "Isolate runtime, reflection and IO calls, so no damaging code can be executed.<br><b>This <i>DOESN'T</i> protect you fully against malicious code!</b>",
				ExecutionTag.POSSIBLE_DAMAGE);
	}

	@Override
	public boolean execute(ArrayList<Clazz> classes, boolean verbose) {
		this.changed = 0;
		logger.info("Isolating all " + classes.size() + " classes");
		classes.stream().map(c -> c.node).forEach(c -> {
			c.methods.forEach(m -> {
				int oldSize = m.instructions.size();
				Instructions.isolateCallsThatMatch(m, (s) -> s.matches(POSSIBLY_MALICIOUS_REGEX));
				if (oldSize != m.instructions.size()) {
					changed++;
					if (verbose) {
						logger.info("Removed calls in " + c.name + "." + m.name + m.desc);
					}
				}
			});
		});
		logger.info(changed + " methods containing calls were isolated");
		return changed > 0;
	}
}