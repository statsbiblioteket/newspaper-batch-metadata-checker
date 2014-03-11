package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIBasedAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/** Check Metadata of all nodes. */
public class MetadataCheckerComponent
        extends SBOIBasedAbstractRunnableComponent {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final MfPakDAO mfPakDAO;

    /**
     * Initialise metadata checker component. For used properties {@link SBOIBasedAbstractRunnableComponent#createIterator}.
     *
     * property fields that are used
     * <ul>
     * <li>atNinestars:boolean:default false: indicates if we are at ninestars and jpylyzer should be executed. The
     * fields below are only relevant if this one is true</li>
     * <li>jpylyzerPath:String: no default: the path to the jpylyzer executable</li>
     * <li>scratch: String: no default: Path to the folder containing the batches</li>
     * <li>controlPolicies: String: default null: The path to the control policies. Optional</li>
     * </ul>
     *
     * @param properties Properties for initialising component.
     * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
     */
    public MetadataCheckerComponent(Properties properties, MfPakDAO mfPakDAO) {
        super(properties);
        this.mfPakDAO = mfPakDAO;
    }

    @Override
    public String getEventID() {
        return "Metadata_checked";
    }

    @Override
    /**
     * For each attribute in batch: Check metadata contents.
     *
     * @param batch The batch to check
     * @param resultCollector Collector to get the result.
     */
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {
        log.info("Starting validation of '{}'", batch.getFullID());

        InputStream batchXmlStructureStream = retrieveBatchStructure(batch);
        if (batchXmlStructureStream == null){
            throw new RuntimeException("Failed to resolve batch manifest from data collector");
        }

        Document batchXmlStructure = DOM.streamToDOM(batchXmlStructureStream);


        MetadataChecksFactory metadataChecksFactory = getMetadataChecksFactory(
                batch,
                resultCollector,
                batchXmlStructure);
        List<TreeEventHandler> eventHandlers = metadataChecksFactory.createEventHandlers();
        EventRunner eventRunner = new EventRunner(createIterator(batch));
        eventRunner.runEvents(eventHandlers, resultCollector);
        log.info("Done validating '{}', success: {}", batch.getFullID(), resultCollector.isSuccess());
    }

    protected MetadataChecksFactory getMetadataChecksFactory(Batch batch, ResultCollector resultCollector,
                                                           Document batchXmlStructure) {
        boolean atNinestars =
                Boolean.parseBoolean(getProperties().getProperty(ConfigConstants.AT_NINESTARS, Boolean.FALSE.toString()));
        MetadataChecksFactory metadataChecksFactory;
        if (atNinestars) {
            String jpylyzerPath = getProperties().getProperty(ConfigConstants.JPYLYZER_PATH);
            String batchFolder = getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER);
            String controlPoliciesPath = getProperties().getProperty(ConfigConstants.SCAPE_CONTROL_POLICIES_PATH);
            metadataChecksFactory = new MetadataChecksFactory(resultCollector,
                                                              atNinestars,
                                                              batchFolder,
                                                              jpylyzerPath,
                                                              controlPoliciesPath, mfPakDAO, batch, batchXmlStructure);
        } else {
            metadataChecksFactory = new MetadataChecksFactory(resultCollector, mfPakDAO, batch, batchXmlStructure);
        }
        return metadataChecksFactory;
    }

}
