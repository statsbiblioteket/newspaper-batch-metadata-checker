package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.MultiThreadedEventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertTrue;

/**
 */
public class MetadataCheckerComponentIT {

    private final static String TEST_BATCH_ID = "400022028241";

    /** Tests that the BatchStructureChecker can parse a production like batch. */
    @Test(groups = "testDataTest")
    public void testMetadataCheck() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties properties = new Properties();


        properties.load(new FileInputStream(pathToProperties));

        TreeIterator iterator = getIterator();

        ResultCollector resultCollector = new ResultCollector(getClass().getSimpleName(), "v0.1");

        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);
        InputStream batchXmlStructureStream = retrieveBatchStructure(batch);

        if (batchXmlStructureStream == null) {
            throw new RuntimeException("Failed to resolve batch manifest from data collector");
        }
        Document batchXmlManifest = DOM.streamToDOM(batchXmlStructureStream);

        MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
        mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigConstants.MFPAK_URL));
        mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigConstants.MFPAK_USER));
        mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigConstants.MFPAK_PASSWORD));
        try (final MfPakDAO mfPakDAO = new MfPakDAO(mfPakConfiguration)) {
            EventHandlerFactory eventHandlerFactory = new MetadataChecksFactory(resultCollector,
                    true,
                    getBatchFolder().getParentFile().getAbsolutePath(),
                    getJpylyzerPath(),
                    mfPakDAO,
                    batch,
                    batchXmlManifest, new HashSet<MetadataChecksFactory.Checks>());
            EventRunner eventRunner = new MultiThreadedEventRunner(iterator,
                    eventHandlerFactory.createEventHandlers(),
                    resultCollector,
                    new MultiThreadedEventRunner.EventCondition() {
                        @Override
                        public boolean shouldFork(ParsingEvent event) {
                            String[] splits = event.getName().split("/");
                            return splits.length == 4;
                        }

                        @Override
                        public boolean shouldJoin(ParsingEvent event) {
                            String[] splits = event.getName().split("/");
                            return splits.length == 3;
                        }
                    },
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            eventRunner.run();
            mfPakDAO.close();
            assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
        }
        //Assert.fail();
    }

    private InputStream retrieveBatchStructure(Batch batch) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("assumed-valid-structure.xml");
    }

    private String getJpylyzerPath() throws URISyntaxException {
        File placeHolderURL = new File(
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResource("METADATA_PLACEHOLDER")
                      .toURI());

        String path = placeHolderURL.getParentFile()
                                 .getParentFile().getParentFile()
                                 .getAbsolutePath() + "/src/main/extras/jpylyzer-1.10.1/jpylyzer.py";
        /*System.out
              .println(path);*/
        return path;
    }

    /**
     * Creates and returns a iteration based on the test batch file structure found in the test/ressources folder.
     *
     * @return A iterator the the test batch
     * @throws URISyntaxException
     */
    public TreeIterator getIterator() throws URISyntaxException {
        File file = getBatchFolder();
        System.out.println(file);
        return new TransformingIteratorForFileSystems(file,
                                                      TransformingIteratorForFileSystems.GROUPING_PATTERN_DEFAULT_VALUE,
                                                      TransformingIteratorForFileSystems.DATA_FILE_PATTERN_JP2_VALUE,
                                                      TransformingIteratorForFileSystems.CHECKSUM_POSTFIX_DEFAULT_VALUE,
                                                      Arrays.asList(
                                                              TransformingIteratorForFileSystems.IGNORED_FILES_DEFAULT_VALUE
                                                                      .split(",")));
    }

    private File getBatchFolder() {
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        return new File(pathToTestBatch, "small-test-batch/B" + TEST_BATCH_ID + "-RT1");
    }
}
