/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.service;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/* package */ class RequestHistoriesExportFromEvents implements AnalysisExport {

    // TODO Sebastian/Lukas: take from controler or config if possible
    private static final String FILENAME_MANUAL = "/output_events.xml.gz";

    private static final String SERVICEFILENAME = "requestHistory.csv";
    private final Network network;
    private final String eventFile;

    public RequestHistoriesExportFromEvents(Network network, Config config) {
        this.network = network;
        eventFile = config.controler().getOutputDirectory() + FILENAME_MANUAL;
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        try {
            ConvertAVServicesFromEvents.write(network, relativeDirectory.getAbsolutePath() + "/" + SERVICEFILENAME, eventFile);
        } catch (Exception e) {
            System.err.println("It was not possible to create the " + SERVICEFILENAME);
        }
    }

}
