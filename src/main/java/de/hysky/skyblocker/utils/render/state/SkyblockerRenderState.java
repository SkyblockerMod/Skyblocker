package de.hysky.skyblocker.utils.render.state;

/**
 * Render states define the necessary properties for a subject to be rendered.
 *
 * Each field inside of a render state must be:
 *   - Immutable (until the subject is rendered)
 *   - Thread safe
 *   - Fast to create (ideally just settings and properties, no code or anything too complex)
 *
 * Render states may be reused and updated after the subject has completed rendering provided the conditions
 * listed above always remain true.
 */
public class SkyblockerRenderState {
	public boolean enabled;
}
