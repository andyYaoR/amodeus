package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.shared.OnMenuRequests;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** This consistency check is very expensive, for this reason, it is only executed when
 * the total number of matched requests or dropped off requests has changed in the time
 * step. */
/* package */ class OnboardPassengerCheck {
    ;

    private Integer totalMatched = null;
    private Integer totalDropoff = null;

    public OnboardPassengerCheck(int total_matchedRequests, int total_dropedOffRequests) {
        this.totalMatched = total_matchedRequests;
        this.totalDropoff = total_dropedOffRequests;
    }

    public void now(int total_matchedRequests, int total_dropedOffRequests, //
            List<RoboTaxi> allTaxis) {

        if (totalMatched != total_matchedRequests && totalDropoff != total_dropedOffRequests) {
            /** requests are matched once a pickup process is in execution */
            int travelling = total_matchedRequests - total_dropedOffRequests;
            /** onboard requests which are currently in a shared {@link RoboTaxi} */
            int menuOnboard = allTaxis.stream().mapToInt(rt -> (int) rt.getMenuOnBoardCustomers()).sum();
            GlobalAssert.that(travelling == menuOnboard);
        }

        this.totalMatched = total_matchedRequests;
        this.totalDropoff = total_dropedOffRequests;
    }
}
