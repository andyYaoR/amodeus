package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.Calendar;
import java.util.Date;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.idsc.amodeus.dispatcher.util.EasyMinDistPathCalculator;
import ch.ethz.idsc.amodeus.util.netdist.CashedDistanceCalculator;

/* package */ enum StaticHelper {
    ;

    public static double getEuclideanTripDistance(Trip trip) {
        return CoordUtils.calcEuclideanDistance(trip.PickupLoc, trip.DropoffLoc);
    }

    public static double getMinNetworkTripDistance(Trip trip, Network network) {
        // LeastCostPathCalculator lcpc = EasyDijkstra.prepDijkstra(network);

        CashedDistanceCalculator lcpc = CashedDistanceCalculator//
                .of(EasyMinDistPathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory()), 180000.0);

        // find links
        Link linkStart = NetworkUtils.getNearestLink(network, trip.PickupLoc);
        Link linkEnd = NetworkUtils.getNearestLink(network, trip.DropoffLoc);

        // shortest path
        return lcpc.distFromTo(linkStart, linkEnd).number().doubleValue();

    }

    public static boolean sameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

}