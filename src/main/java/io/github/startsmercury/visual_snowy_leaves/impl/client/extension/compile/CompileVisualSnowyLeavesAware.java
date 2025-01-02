package io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile;

import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;

/**
 * Loom interface injectable version of {@code VisualSnowyLeavesAware}.
 *
 * @see VisualSnowyLeavesAware
 * @deprecated Prefer using {@code VisualSnowyLeavesAware} outside interface
 *     injection purposes.
 */
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public interface CompileVisualSnowyLeavesAware extends VisualSnowyLeavesAware {
    @Override
    default VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return CompileAwareHelper.unimplemented();
    }
}
