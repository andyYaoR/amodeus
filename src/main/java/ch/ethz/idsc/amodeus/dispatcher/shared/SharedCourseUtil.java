/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.matsim.av.passenger.AVRequest;

public enum SharedCourseUtil {
    ;

    /** @return deep copy of {@link List} of {@link SharedCourse}s @param courses */
    public static List<SharedCourse> copy(List<SharedCourse> courses) {
        return new ArrayList<>(courses);
    }

    /** @return {@link Set} of unique {@link AVRequest}s in the {@link List} of {@link SharedCourse}s
     *         provided in @param courses */
    public static Set<AVRequest> getUniqueAVRequests(List<? extends SharedCourse> courses) {
        return courses.stream()//
                .filter(sc -> !sc.getMealType().equals(SharedMealType.REDIRECT))//
                .map(sc -> sc.getAvRequest()).collect(Collectors.toSet());//
    }

}
