/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Objects;
import java.util.Properties;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class ServiceQualityScore extends LinComScore {
    protected static Tensor setAlpha(Properties scrprm) {
        Scalar alpha1 = Quantity.of(Double.parseDouble(scrprm.getProperty("alpha1")), SI.SECOND.negate());
        Objects.requireNonNull(alpha1);
        Scalar alpha2 = Quantity.of(Double.parseDouble(scrprm.getProperty("alpha2")), SI.METER.negate());
        Objects.requireNonNull(alpha2);
        return Tensors.of(alpha1, alpha2);
    }

    public ServiceQualityScore(Properties scrprm) {
        super(setAlpha(scrprm));
    }

}