package org.fxmisc.flowless;

import javafx.geometry.Orientation;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

import java.util.Optional;

public interface Virtualized {
    Val<Double> totalWidthEstimateProperty();
    Val<Double> totalHeightEstimateProperty();
    Var<Double> estimatedScrollXProperty();
    Var<Double> estimatedScrollYProperty();
}
