/**
 * Package containing models for the JSON responses used by the profile viewer.
 * {@code class}es are intentionally used instead of records, since specifying defaults for records is more cumbersome.
 * This package is {@link org.jetbrains.annotations.NotNullByDefault not null by default}, meaning warnings are emitted
 * if a field is not explicitly marked as nullable or has a default value provided. This should be a sufficient safeguard
 * against most API deviations (such as missing fields).
 */
@NotNullByDefault
package de.hysky.skyblocker.skyblock.profileviewer.model;

import org.jetbrains.annotations.NotNullByDefault;
