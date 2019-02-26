/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.Date;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Scalar;

/** The class {@link TaxiTrip} is used to transform taxi trips from databases into scenarios
 * for AMoDeus. It contains the relevant recordings of a typical taxi trip recording. */
public class TaxiTrip implements Comparable<TaxiTrip> {
    public final Integer id;
    public final Integer taxiId;
    public final Coord pickupLoc;
    public final Coord dropoffLoc;
    public final Scalar distance;
    public final Scalar waitTime;
    // ---
    public Date pickupDate;
    public Date dropoffDate;
    public final Scalar duration;

    public TaxiTrip(Integer id, Integer taxiId, Date pickupDate, Date dropoffDate, //
            Coord pickupLoc, Coord dropoffLoc, //
            Scalar duration, Scalar distance, Scalar waitTime) {
        this.id = id;
        this.taxiId = taxiId;
        this.pickupDate = pickupDate;
        this.dropoffDate = dropoffDate;
        this.pickupLoc = pickupLoc;
        this.dropoffLoc = dropoffLoc;
        this.duration = duration;
        this.distance = distance;
        this.waitTime = waitTime;
    }

    @Override
    public int compareTo(TaxiTrip trip) {
        return this.pickupDate.compareTo(trip.pickupDate);
    }

    @Override
    public String toString() {
        return pickupDate + "\t:\tTrip " + id + "\t(Taxi " + taxiId + ")";
    }
}