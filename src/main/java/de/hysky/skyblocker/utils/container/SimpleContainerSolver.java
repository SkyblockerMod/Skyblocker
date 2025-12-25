package de.hysky.skyblocker.utils.container;

import org.intellij.lang.annotations.Language;

import java.util.regex.Pattern;

/**
 * Simple implementation of a container solver. Extend this class to add a new gui solver,
 * like terminal solvers or experiment solvers and add it to {@link ContainerSolverManager#solvers}.
 */
public abstract class SimpleContainerSolver extends RegexContainerMatcher implements ContainerSolver {
	/**
	 * Utility constructor that will compile the given string into a pattern.
	 *
	 * @see #SimpleContainerSolver(Pattern)
	 */
	protected SimpleContainerSolver(@Language("RegExp") String titlePattern) {
		super(titlePattern);
	}

	/**
	 * Creates a ContainerSolver that will be applied to screens with titles that match the given pattern.
	 *
	 * @param titlePattern The pattern to match the screen title against.
	 */
	protected SimpleContainerSolver(Pattern titlePattern) {
		super(titlePattern);
	}

	// A container solver that applies to every screen doesn't make sense,
	// so we don't provide a constructor for that and force getTitlePattern to be @NotNull
	@Override
	public Pattern getTitlePattern() {
		assert super.getTitlePattern() != null;
		return super.getTitlePattern();
	}
}
