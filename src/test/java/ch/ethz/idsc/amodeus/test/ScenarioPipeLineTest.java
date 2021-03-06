/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.gnu.glpk.GLPK;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.gbl.MatsimRandom;

import ch.ethz.idsc.amodeus.analysis.AnalysisConstants;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.testutils.TestPreparer;
import ch.ethz.idsc.amodeus.testutils.TestServer;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;

public class ScenarioPipeLineTest {

    private static TestPreparer testPreparer;
    private static TestServer testServer;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        // TODO TEST This reset call should eventually be removed. Right now we need this to reset the random number generator for MATSim.
        // In general, this is not necessary, because all MATSim components use MatsimRandom.getLocalInstance(). However,
        // the PopulationDensity strategy in the av package uses MatsimRandom.getRandom(), which is NOT reset between
        // simulations and iterations. Once the av package makes proper use of MatsimRandom generator, this can be removed
        // here (should happen once av:0.1.5 is used here). /shoerl mar18
        MatsimRandom.reset();

        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        // copy scenario data into main directory
        File scenarioDirectory = new File(LocateUtils.getSuperFolder("amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());

        // run scenario preparer
        testPreparer = TestPreparer.run(workingDirectory);

        // run scenario server
        testServer = TestServer.run().on(workingDirectory);
    }

    @Test
    public void testPreparer() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        System.out.print("Preparer Test:\t");

        // creation of files
        File preparedPopulationFile = new File("preparedPopulation.xml");
        assertTrue(preparedPopulationFile.isFile());

        File preparedNetworkFile = new File("preparedNetwork.xml");
        assertTrue(preparedNetworkFile.isFile());

        File config = new File("config.xml");
        assertTrue(config.isFile());

        // consistency of network (here no cutting)
        Network originalNetwork = NetworkLoader.fromConfigFile(testServer.getConfigFile());
        Network preparedNetwork = testPreparer.getPreparedNetwork();
        GlobalAssert.that(Objects.nonNull(originalNetwork));
        GlobalAssert.that(Objects.nonNull(preparedNetwork));
        assertEquals(preparedNetwork.getNodes().size(), originalNetwork.getNodes().size());
        assertEquals(preparedNetwork.getLinks().size(), originalNetwork.getLinks().size());

        // consistency of population
        Population population = testPreparer.getPreparedPopulation();
        assertEquals(2000, population.getPersons().size());

    }

    @Test
    public void testServer() throws Exception {
        System.out.print("GLPK version is: ");
        System.out.println(GLPK.glp_version());

        System.out.print("Server Test:\t");

        // scenario options
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        assertEquals(workingDirectory.getAbsolutePath() + "/config.xml", scenarioOptions.getSimulationConfigName());
        assertEquals(workingDirectory.getAbsolutePath() + "/preparedNetwork", scenarioOptions.getPreparedNetworkName());
        assertEquals(workingDirectory.getAbsolutePath() + "/preparedPopulation", scenarioOptions.getPreparedPopulationName());

        // simulation objects should exist after simulation (simulation data)
        File simobj = new File("output/001/simobj/it.00");
        assertTrue(simobj.exists());
        assertEquals(109, simobj.listFiles().length);
        assertTrue(new File(simobj, "0000000/0000010.bin").exists());
        assertTrue(new File(simobj, "0107000/0107940.bin").exists());
    }

    @Test
    public void testAnalysis() throws Exception {
        System.out.print("Analysis Test:\t");

        AnalysisTestExport ate = testServer.getAnalysisTestExport();

        /** number of processed requests */
        assertEquals(2000, ate.getSimulationInformationElement().reqsize());

        /** fleet size */
        assertEquals(200, ate.getSimulationInformationElement().vehicleSize());

        /** status distribution, every row must equal the total of vehicles */
        Tensor distributionSum = Total.of(Transpose.of(ate.getStatusDistribution().statusTensor));
        distributionSum.flatten(-1).forEach(e -> //
        assertTrue(e.equals(RealScalar.of(ate.getSimulationInformationElement().vehicleSize()))));

        ScalarAssert scalarAssert = new ScalarAssert();

        /** distance and occupancy ratios */
        Scalar occupancyRatio = Mean.of(ate.getDistancElement().ratios).Get(0);
        Scalar distanceRatio = Mean.of(ate.getDistancElement().ratios).Get(1);

        //
        scalarAssert.add(RealScalar.of(0.08269953703703704), occupancyRatio);
        scalarAssert.add(RealScalar.of(0.6796628382756873), distanceRatio);

        /** fleet distances */
        assertTrue(ate.getDistancElement().totalDistance >= 0.0);
        assertTrue(ate.getDistancElement().totalDistanceWtCst >= 0.0);
        assertTrue(ate.getDistancElement().totalDistancePicku > 0.0);
        assertTrue(ate.getDistancElement().totalDistanceRebal >= 0.0);
        assertTrue(ate.getDistancElement().totalDistanceRatio >= 0.0);
        ate.getDistancElement().totalDistancesPerVehicle.flatten(-1).forEach(s -> //
        assertTrue(Scalars.lessEquals(RealScalar.ZERO, (Scalar) s)));
        assertTrue(((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle)).number().doubleValue() //
        == ate.getDistancElement().totalDistance);
        assertTrue(((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle)).number().doubleValue() //
        == ate.getDistancElement().totalDistance);

        scalarAssert.add(RealScalar.of(34429.271548560515), RealScalar.of(ate.getDistancElement().totalDistance));
        scalarAssert.add(RealScalar.of(28980.70185154435), RealScalar.of(ate.getDistancElement().totalDistanceWtCst));
        scalarAssert.add(RealScalar.of(5448.5696970161725), RealScalar.of(ate.getDistancElement().totalDistancePicku));
        scalarAssert.add(RealScalar.of(0.0), RealScalar.of(ate.getDistancElement().totalDistanceRebal));

        scalarAssert.add(RealScalar.of(0.0), RealScalar.of(ate.getDistancElement().totalDistanceRebal));
        scalarAssert.add(RealScalar.of(0.8417460070471933), RealScalar.of(ate.getDistancElement().totalDistanceRatio));

        scalarAssert.add((Scalar) Total.of(ate.getDistancElement().totalDistancesPerVehicle), RealScalar.of(ate.getDistancElement().totalDistance));

        /** wait times, drive times */
        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2)));
        // FIXME the code below only checks units but not sign
        ate.getTravelTimeAnalysis().getWaitTimes().flatten(-1).forEach(t -> {
            Scalars.lessEquals(Quantity.of(0, SI.SECOND), (Scalar) t);
            Scalars.lessEquals((Scalar) t, ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2));

        });

        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(0)));
        assertTrue(Scalars.lessEquals(ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(0), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(1)));
        assertTrue(Scalars.lessEquals(ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(1), ate.getTravelTimeAnalysis().getWaitAggrgte().get(0).Get(2)));
        assertTrue(Scalars.lessEquals(Quantity.of(0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(1)));

        scalarAssert.add(Quantity.of(287.18, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(1));
        scalarAssert.add(Quantity.of(3261.0, SI.SECOND), ate.getTravelTimeAnalysis().getWaitAggrgte().Get(2));
        scalarAssert.add(Quantity.of(893.155, SI.SECOND), ate.getTravelTimeAnalysis().getDrveAggrgte().Get(1));
        scalarAssert.add(Quantity.of(3670.0, SI.SECOND), ate.getTravelTimeAnalysis().getDrveAggrgte().Get(2));

        /* TODO: Have a look at {AmodeusModule::install}. At some point the travel time calculation in DVRP has been improved.
         * Unfortunately, this improvement breaks these tests. The reference numbers here should be adjusted at some point so that the fallback in
         * {AmodeusModule::install} can be removed again.
         * (Nevertheless, we're talking about a different in routed time of +/-1 second). /sebhoerl */
        scalarAssert.consolidate();

        /** presence of plot files */
        assertTrue(new File("output/001/data/binnedWaitingTimes.png").isFile());
        assertTrue(new File("output/001/data/distanceDistribution.png").isFile());
        assertTrue(new File("output/001/data/occAndDistRatios.png").isFile());
        assertTrue(new File("output/001/data/stackedDistance.png").isFile());
        assertTrue(new File("output/001/data/statusDistribution.png").isFile());

        assertTrue(new File("output/001/data", AnalysisConstants.ParametersExportFilename).exists());

        assertTrue(new File("output/001/data/WaitingTimes").isDirectory());
        assertTrue(new File("output/001/data/WaitingTimes/WaitingTimes.mathematica").isFile());

        assertTrue(new File("output/001/data/StatusDistribution").isDirectory());
        assertTrue(new File("output/001/data/StatusDistribution/StatusDistribution.mathematica").isFile());

        assertTrue(new File("output/001/data/DistancesOverDay").isDirectory());
        assertTrue(new File("output/001/data/DistancesOverDay/DistancesOverDay.mathematica").isFile());

        assertTrue(new File("output/001/data/DistanceRatios").isDirectory());
        assertTrue(new File("output/001/data/DistanceRatios/DistanceRatios.mathematica").isFile());

        assertTrue(new File("output/001/report/report.html").isFile());
        assertTrue(new File("output/001/report/av.xml").isFile());
        assertTrue(new File("output/001/report/config.xml").isFile());
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
