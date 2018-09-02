/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.NumberQ;
import ch.ethz.idsc.tensor.io.ResourceData;
import ch.ethz.idsc.tensor.qty.Quantity;
import junit.framework.TestCase;

public class ServiceQualityScoreTest extends TestCase {
    public void testSimple() {
        Properties properties = ResourceData.properties("/aido/scoreparam.properties");
        ServiceQualityScore efficiencyScore = new ServiceQualityScore(properties);
        NumberQ.require(efficiencyScore.alpha.Get(0).multiply(Quantity.of(3, SI.SECOND)));
        NumberQ.require(efficiencyScore.alpha.Get(1).multiply(Quantity.of(3, SI.METER)));
        try {
            NumberQ.require(efficiencyScore.alpha.Get(1).multiply(Quantity.of(3, SI.SECOND)));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }
}
