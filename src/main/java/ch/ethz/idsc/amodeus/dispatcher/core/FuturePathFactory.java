/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** factory that emits {@link FuturePathContainer} */
/* package */ class FuturePathFactory {
    private final ParallelLeastCostPathCalculator parallelLeastCostPathCalculator;
    private final TravelTime travelTime;

    public FuturePathFactory( //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            TravelTime travelTime) {
        this.parallelLeastCostPathCalculator = parallelLeastCostPathCalculator;
        this.travelTime = travelTime;
    }

    public FuturePathContainer createFuturePathContainer(Link startLink, Link destLink, double startTime) {
        Future<Path> leastCostPathFuture = parallelLeastCostPathCalculator.calcLeastCostPath( // <- non-blocking call
                startLink.getToNode(), destLink.getFromNode(), startTime, null, null);
        return new FuturePathContainer(startLink, destLink, startTime, leastCostPathFuture, travelTime);
    }
}
