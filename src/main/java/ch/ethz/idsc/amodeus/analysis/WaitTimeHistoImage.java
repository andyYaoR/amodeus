/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum WaitTimeHistoImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "requestsPerWaitTime";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis travelTimeAnalysis = analysisSummary.getTravelTimeAnalysis();
        HistogramReportFigure.of( //
                PositiveSubVector.of(travelTimeAnalysis.getWaitTimes()), //
                travelTimeAnalysis.getWaitAggrgte().Get(2), //
                colorDataIndexed, relativeDirectory, "Number of Requests per Wait Time", "Wait Times [s]", FILENAME);
    }
}
