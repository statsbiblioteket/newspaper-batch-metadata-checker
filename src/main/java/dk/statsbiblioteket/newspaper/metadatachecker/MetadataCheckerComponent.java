package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/** Check Metadata of all nodes. */
public class MetadataCheckerComponent
        extends AbstractRunnableComponent {
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Initialise metadata checker component. For used properties {@link AbstractRunnableComponent#createIterator}.
     *
     * property fiels that are used
     * <ul>
     * <li>atNinestars:boolean:default false: indicates if we are at ninestars and jpylyzer should be executed. The
     * fields below are only relevant if this one is true</li>
     * <li>jpylyzerPath:String: no default: the path to the jpylyzer executable</li>
     * <li>scratch: String: no default: Path to the folder containing the batches</li>
     * <li>controlPolicies: String: default null: The path to the control policies. Optional</li>
     * </ul>
     *
     * @param properties Properties for initialising component.
     */
    public MetadataCheckerComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getComponentName() {
        return "Metadata_checker_component";

    }

    @Override
    public String getComponentVersion() {
        return "0.1";
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

        boolean atNinestars =
                Boolean.parseBoolean(getProperties().getProperty("atNinestars", Boolean.FALSE.toString()));
        MetadataChecksFactory metadataChecksFactory;
        if (atNinestars) {
            String jpylyzerPath = getProperties().getProperty("jpylyzerPath");
            String batchFolder = getProperties().getProperty("scratch");
            String controlPoliciesPath = getProperties().getProperty("controlPolicies");
            metadataChecksFactory = new MetadataChecksFactory(resultCollector,
                                                              atNinestars,
                                                              batchFolder,
                                                              jpylyzerPath,
                                                              controlPoliciesPath);
        } else {

            metadataChecksFactory = new MetadataChecksFactory(resultCollector);
        }


        List<TreeEventHandler> eventHandlers = metadataChecksFactory.createEventHandlers();
        EventRunner eventRunner = new EventRunner(createIterator(batch));
        eventRunner.runEvents(eventHandlers);
        log.info("Done validating '{}', success: {}", batch.getFullID(), resultCollector.isSuccess());
    }

}