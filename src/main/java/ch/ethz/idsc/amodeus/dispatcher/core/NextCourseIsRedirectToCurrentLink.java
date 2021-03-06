/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum NextCourseIsRedirectToCurrentLink {
    ;

    public static boolean check(RoboTaxi roboTaxi) {
        Optional<SharedCourse> redirectCourseCheck = SharedCourseAccess.getStarter(roboTaxi);
        if (!redirectCourseCheck.isPresent())
            return false;
        if (!redirectCourseCheck.get().getMealType().equals(SharedMealType.REDIRECT))
            return false;
        return redirectCourseCheck.get().getLink().equals(roboTaxi.getDivertableLocation());
    }

}
