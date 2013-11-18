package dk.statsbiblioteket.newspaper.metadatachecker;


import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class MixValidationTest {

    private ResultCollector resultCollector = null;

    @BeforeTest
    public void setUp() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void shouldSucceed() {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch
                , 9304, 11408);

        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        handler.handleAttribute(event);
        assertTrue(resultCollector.isSuccess(),resultCollector.toReport());
    }

    @Test
    public void shouldFailDueToMissingFormatName() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:FormatDesignation>" +
                             "      <mix:formatName></mix:formatName>" +
                             "      <mix:formatVersion>ISO-IEC 15444-1:2004</mix:formatVersion>" +
                             "    </mix:FormatDesignation>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongFormatName() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:FormatDesignation>" +
                             "      <mix:formatName>Wrong format name</mix:formatName>" +
                             "      <mix:formatVersion>ISO-IEC 15444-1:2004</mix:formatVersion>" +
                             "    </mix:FormatDesignation>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingFormatVersion() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:FormatDesignation>" +
                             "      <mix:formatName>JPEG2000 – part 1</mix:formatName>" +
                             "      <mix:formatVersion></mix:formatVersion>" +
                             "    </mix:FormatDesignation>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongFormatVersion() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:FormatDesignation>" +
                             "      <mix:formatName>JPEG2000 – part 1</mix:formatName>" +
                             "      <mix:formatVersion>Wrong format version</mix:formatVersion>" +
                             "    </mix:FormatDesignation>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingCompressionScheme() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:Compression>" +
                             "      <mix:compressionScheme></mix:compressionScheme>" +
                             "    </mix:Compression>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongCompressionScheme() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:Compression>" +
                             "      <mix:compressionScheme>Wrong compression scheme</mix:compressionScheme>" +
                             "    </mix:Compression>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingMessageDigestAlgorithm() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:Fixity>" +
                             "      <mix:messageDigestAlgorithm></mix:messageDigestAlgorithm>" +
                             "      <mix:messageDigestOriginator>Ninestars</mix:messageDigestOriginator>" +
                             "    </mix:Fixity>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongMessageDigestAlgorithm() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:Fixity>" +
                             "      <mix:messageDigestAlgorithm>Wrong message digest algorithm</mix:messageDigestAlgorithm>" +
                             "      <mix:messageDigestOriginator>Ninestars</mix:messageDigestOriginator>" +
                             "    </mix:Fixity>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingMessageDigestOriginator() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:Fixity>" +
                             "      <mix:messageDigestAlgorithm>MD5</mix:messageDigestAlgorithm>" +
                             "      <mix:messageDigestOriginator></mix:messageDigestOriginator>" +
                             "    </mix:Fixity>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongMessageDigestOriginator() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicDigitalObjectInformation>" +
                             "    <mix:Fixity>" +
                             "      <mix:messageDigestAlgorithm>MD5</mix:messageDigestAlgorithm>" +
                             "      <mix:messageDigestOriginator>Wrong message digest originator</mix:messageDigestOriginator>" +
                             "    </mix:Fixity>" +
                             "  </mix:BasicDigitalObjectInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingColorSpace() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicImageInformation>" +
                             "    <mix:BasicImageCharacteristics>" +
                             "      <mix:PhotometricInterpretation>" +
                             "        <mix:colorSpace></mix:colorSpace>" +
                             "      </mix:PhotometricInterpretation>" +
                             "    </mix:BasicImageCharacteristics>" +
                             "  </mix:BasicImageInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongColorSpace() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:BasicImageInformation>" +
                             "    <mix:BasicImageCharacteristics>" +
                             "      <mix:PhotometricInterpretation>" +
                             "        <mix:colorSpace>Wrong color space</mix:colorSpace>" +
                             "      </mix:PhotometricInterpretation>" +
                             "    </mix:BasicImageCharacteristics>" +
                             "  </mix:BasicImageInformation>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingSourceType() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:SourceInformation>" +
                             "      <mix:sourceType></mix:sourceType>" +
                             "    </mix:SourceInformation>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongSourceType() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:SourceInformation>" +
                             "      <mix:sourceType>Wrong source type</mix:sourceType>" +
                             "    </mix:SourceInformation>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingImageProducer() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:GeneralCaptureInformation>" +
                             "      <mix:imageProducer></mix:imageProducer>" +
                             "    </mix:GeneralCaptureInformation>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongImageProducer() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:GeneralCaptureInformation>" +
                             "      <mix:imageProducer>Wrong image producer</mix:imageProducer>" +
                             "    </mix:GeneralCaptureInformation>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingScannerManufacturer() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:ScannerCapture>" +
                             "      <mix:scannerManufacturer></mix:scannerManufacturer>" +
                             "      <mix:ScannerModel>" +
                             "        <mix:scannerModelName>Scanstation</mix:scannerModelName>" +
                             "        <mix:scannerModelNumber>RS325</mix:scannerModelNumber>" +
                             "        <mix:scannerModelSerialNo>SN#8060359</mix:scannerModelSerialNo>" +
                             "      </mix:ScannerModel>" +
                             "      <mix:ScanningSystemSoftware><!--Repeatable-->" +
                             "      <mix:scanningSoftwareName>Rollfilm</mix:scanningSoftwareName>" +
                             "      <mix:scanningSoftwareVersionNo>2.6g</mix:scanningSoftwareVersionNo>" +
                             "      </mix:ScanningSystemSoftware>" +
                             "    </mix:ScannerCapture>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingScannerModelName() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:ScannerCapture>" +
                             "      <mix:scannerManufacturer>Wicks and Wilson</mix:scannerManufacturer>" +
                             "      <mix:ScannerModel>" +
                             "        <mix:scannerModelName></mix:scannerModelName>" +
                             "        <mix:scannerModelNumber>RS325</mix:scannerModelNumber>" +
                             "        <mix:scannerModelSerialNo>SN#8060359</mix:scannerModelSerialNo>" +
                             "      </mix:ScannerModel>" +
                             "      <mix:ScanningSystemSoftware><!--Repeatable-->" +
                             "      <mix:scanningSoftwareName>Rollfilm</mix:scanningSoftwareName>" +
                             "      <mix:scanningSoftwareVersionNo>2.6g</mix:scanningSoftwareVersionNo>" +
                             "      </mix:ScanningSystemSoftware>" +
                             "    </mix:ScannerCapture>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingScannerModelNumber() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:ScannerCapture>" +
                             "      <mix:scannerManufacturer>Wicks and Wilson</mix:scannerManufacturer>" +
                             "      <mix:ScannerModel>" +
                             "        <mix:scannerModelName>Scanstation</mix:scannerModelName>" +
                             "        <mix:scannerModelNumber></mix:scannerModelNumber>" +
                             "        <mix:scannerModelSerialNo>SN#8060359</mix:scannerModelSerialNo>" +
                             "      </mix:ScannerModel>" +
                             "      <mix:ScanningSystemSoftware><!--Repeatable-->" +
                             "      <mix:scanningSoftwareName>Rollfilm</mix:scanningSoftwareName>" +
                             "      <mix:scanningSoftwareVersionNo>2.6g</mix:scanningSoftwareVersionNo>" +
                             "      </mix:ScanningSystemSoftware>" +
                             "    </mix:ScannerCapture>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingScannerModelSerialNo() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:ScannerCapture>" +
                             "      <mix:scannerManufacturer>Wicks and Wilson</mix:scannerManufacturer>" +
                             "      <mix:ScannerModel>" +
                             "        <mix:scannerModelName>Scanstation</mix:scannerModelName>" +
                             "        <mix:scannerModelNumber>RS325</mix:scannerModelNumber>" +
                             "        <mix:scannerModelSerialNo></mix:scannerModelSerialNo>" +
                             "      </mix:ScannerModel>" +
                             "      <mix:ScanningSystemSoftware><!--Repeatable-->" +
                             "      <mix:scanningSoftwareName>Rollfilm</mix:scanningSoftwareName>" +
                             "      <mix:scanningSoftwareVersionNo>2.6g</mix:scanningSoftwareVersionNo>" +
                             "      </mix:ScanningSystemSoftware>" +
                             "    </mix:ScannerCapture>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingScanningSoftwareName() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:ScannerCapture>" +
                             "      <mix:scannerManufacturer>Wicks and Wilson</mix:scannerManufacturer>" +
                             "      <mix:ScannerModel>" +
                             "        <mix:scannerModelName>Scanstation</mix:scannerModelName>" +
                             "        <mix:scannerModelNumber>RS325</mix:scannerModelNumber>" +
                             "        <mix:scannerModelSerialNo>SN#8060359</mix:scannerModelSerialNo>" +
                             "      </mix:ScannerModel>" +
                             "      <mix:ScanningSystemSoftware><!--Repeatable-->" +
                             "      <mix:scanningSoftwareName></mix:scanningSoftwareName>" +
                             "      <mix:scanningSoftwareVersionNo>2.6g</mix:scanningSoftwareVersionNo>" +
                             "      </mix:ScanningSystemSoftware>" +
                             "    </mix:ScannerCapture>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingScanningSoftwareVersionNo() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageCaptureMetadata>" +
                             "    <mix:ScannerCapture>" +
                             "      <mix:scannerManufacturer>Wicks and Wilson</mix:scannerManufacturer>" +
                             "      <mix:ScannerModel>" +
                             "        <mix:scannerModelName>Scanstation</mix:scannerModelName>" +
                             "        <mix:scannerModelNumber>RS325</mix:scannerModelNumber>" +
                             "        <mix:scannerModelSerialNo>SN#8060359</mix:scannerModelSerialNo>" +
                             "      </mix:ScannerModel>" +
                             "      <mix:ScanningSystemSoftware><!--Repeatable-->" +
                             "      <mix:scanningSoftwareName>Rollfilm</mix:scanningSoftwareName>" +
                             "      <mix:scanningSoftwareVersionNo></mix:scanningSoftwareVersionNo>" +
                             "      </mix:ScanningSystemSoftware>" +
                             "    </mix:ScannerCapture>" +
                             "  </mix:ImageCaptureMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingBitsPerSampleValue() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageAssessmentMetadata>" +
                             "    <mix:ImageColorEncoding>" +
                             "      <mix:BitsPerSample>" +
                             "        <mix:bitsPerSampleValue></mix:bitsPerSampleValue>" +
                             "        <mix:bitsPerSampleUnit>integer</mix:bitsPerSampleUnit>" +
                             "      </mix:BitsPerSample>" +
                             "      <mix:samplesPerPixel>1</mix:samplesPerPixel>" +
                             "    </mix:ImageColorEncoding>" +
                             "  </mix:ImageAssessmentMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongBitsPerSampleValue() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageAssessmentMetadata>" +
                             "    <mix:ImageColorEncoding>" +
                             "      <mix:BitsPerSample>" +
                             "        <mix:bitsPerSampleValue>Wrong bits per sample value</mix:bitsPerSampleValue>" +
                             "        <mix:bitsPerSampleUnit>integer</mix:bitsPerSampleUnit>" +
                             "      </mix:BitsPerSample>" +
                             "      <mix:samplesPerPixel>1</mix:samplesPerPixel>" +
                             "    </mix:ImageColorEncoding>" +
                             "  </mix:ImageAssessmentMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingBitsPerSampleUnit() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageAssessmentMetadata>" +
                             "    <mix:ImageColorEncoding>" +
                             "      <mix:BitsPerSample>" +
                             "        <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>" +
                             "        <mix:bitsPerSampleUnit></mix:bitsPerSampleUnit>" +
                             "      </mix:BitsPerSample>" +
                             "      <mix:samplesPerPixel>1</mix:samplesPerPixel>" +
                             "    </mix:ImageColorEncoding>" +
                             "  </mix:ImageAssessmentMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongBitsPerSampleUnit() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageAssessmentMetadata>" +
                             "    <mix:ImageColorEncoding>" +
                             "      <mix:BitsPerSample>" +
                             "        <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>" +
                             "        <mix:bitsPerSampleUnit>Wrong bits per sample unit</mix:bitsPerSampleUnit>" +
                             "      </mix:BitsPerSample>" +
                             "      <mix:samplesPerPixel>1</mix:samplesPerPixel>" +
                             "    </mix:ImageColorEncoding>" +
                             "  </mix:ImageAssessmentMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingSamplesPerPixel() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageAssessmentMetadata>" +
                             "    <mix:ImageColorEncoding>" +
                             "      <mix:BitsPerSample>" +
                             "        <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>" +
                             "      </mix:BitsPerSample>" +
                             "      <mix:samplesPerPixel></mix:samplesPerPixel>" +
                             "    </mix:ImageColorEncoding>" +
                             "  </mix:ImageAssessmentMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongSamplesPerPixel() {
        final String input = "" +
                             "<mix:mix xmlns:mix='http://www.loc.gov/mix/v20'>" +
                             "  <mix:ImageAssessmentMetadata>" +
                             "    <mix:ImageColorEncoding>" +
                             "      <mix:BitsPerSample>" +
                             "        <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>" +
                             "      </mix:BitsPerSample>" +
                             "      <mix:samplesPerPixel>Wrong samples per pixel</mix:samplesPerPixel>" +
                             "    </mix:ImageColorEncoding>" +
                             "  </mix:ImageAssessmentMetadata>" +
                             "</mix:mix>";

        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    private void handleTestEvent(final String input, ResultCollector resultCollector) {
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent event = new AttributeParsingEvent("test.mix.xml") {
            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(input.getBytes("UTF-8"));
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(event);
    }

    @Test
    public void testXpathValidationScannedDate() throws ParseException, SQLException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch, 9304, 11408);


        MfPakDAO mfpakDao = mock(MfPakDAO.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date shipmentDate = formatter.parse("2010-01-02");
        when(mfpakDao.getBatchShipmentDate(batchId)).thenReturn(shipmentDate);
        Document batchXmlStructure = DOM.streamToDOM(
                        Thread.currentThread()
                              .getContextClassLoader()
                              .getResourceAsStream("assumed-valid-structure.xml"));

        MixXPathEventHandler handler = new MixXPathEventHandler(resultCollector, mfpakDao, batch, batchXmlStructure);

        handler.handleAttribute(event);
        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationScannedBeforeShipment() throws ParseException, SQLException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch, 9304, 11408);

        MfPakDAO mfpakDao = mock(MfPakDAO.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        // The in the mix file is 2010-11-11
        Date shipmentDate = new Date();
        when(mfpakDao.getBatchShipmentDate(batchId)).thenReturn(shipmentDate);
        Document batchXmlStructure = DOM.streamToDOM(
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("assumed-valid-structure.xml"));

        MixXPathEventHandler handler = new MixXPathEventHandler(resultCollector, mfpakDao, batch, batchXmlStructure);


        handler.handleAttribute(event);
        String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(report.contains("2K-1:"));
    }

    @Test
    public void testXpathValidationObjectIdentifier() throws ParseException, SQLException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch, 9304, 11408);
        AttributeParsingEvent event2 = MixerMockup.getMixWorkshiftIso("000001", "0001", batch);

        MfPakDAO mfpakDao = mock(MfPakDAO.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date shipmentDate = formatter.parse("2010-01-02");
        when(mfpakDao.getBatchShipmentDate("400022028241")).thenReturn(shipmentDate);
        Document batchXmlStructure = DOM.streamToDOM(
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("assumed-valid-structure.xml"));

        MixXPathEventHandler handler = new MixXPathEventHandler(resultCollector, mfpakDao, batch, batchXmlStructure);


        handler.handleAttribute(event);
        handler.handleAttribute(event2);
        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

}
