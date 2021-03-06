/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ enum Length {
    ;

    public static Scalar of(RoboTaxi roboTaxi, List<SharedCourse> menu, //
            CachedNetworkTimeDistance distance, Double now) {
        if (menu.isEmpty())
            return Quantity.of(0.0, SI.METER);
        Scalar length = Quantity.of(0.0, SI.METER);
        Link link = roboTaxi.getDivertableLocation();
        for (SharedCourse course : menu) {
            length = length.add(distance.distance(link, course.getLink(), now));
            link = course.getLink();
        }
        return length;
    }
}
