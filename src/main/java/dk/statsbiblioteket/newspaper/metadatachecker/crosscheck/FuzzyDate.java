package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;

/**
 * Implements functionality for working with ISO format dates without full precision. This means the dates can be
 * instantiate with the follow formats: <ol>
 *     <li>Full precision: yyyy-MM-dd.</li>
 *     <li>Month precision: yyyy-MM-00.</li>
 *     <li>Year precision: yyyy-00-00.</li>
 * </ol>
 */
public final class FuzzyDate implements Comparable<FuzzyDate> {
    private final String dateString;
    private final int myPrecision;

    public FuzzyDate(String dateString) {
        validateFormat(dateString);
        this.dateString = dateString;
        myPrecision = findPrecisionIndex(this.dateString);
    }

    /**
     * 0 if the argument Date is equal to this Date; a value less than 0 if this Date is before the Date argument and a
     * value greater than 0 if this Date is after the Date argument.<p>
     *     Examples: <ol>
     *        <li>FuzzyDate(2012-03-03).compareTo(FuzzyDate(2012-03-02)) will return 1.</li>
     *        <li>FuzzyDate(2012-03-00).compareTo(FuzzyDate(2012-03-02)) will return 0.</li>
     *        <li>FuzzyDate(2012-03-03).compareTo(FuzzyDate(2012-03-00)) will return 0.</li>
     *        <li>FuzzyDate(2012-03-00).compareTo(FuzzyDate(2012-00-00)) will return -1.</li>
     *     </ol>
     * </p>
     * @param date The date to compare this date to.
     */
    public int compareTo(FuzzyDate date) {
        int minPrecisionIndex = Math.min(myPrecision, date.getPrecision());
        return asString(minPrecisionIndex).compareTo(date.asString(minPrecisionIndex));
    }

    /**
     * Return the string value of this fuzzy date.
     */
    public String asString() {
        return dateString;
    }

    /**
     * Return the string value of this fuzzy date, where the characters outside of the indicated index are thrown away.
     * @param precisionIndex Specifies the precision the string should be trimmed to.
     */
    protected String asString(int precisionIndex) {
        return dateString.substring(0, precisionIndex);
    }

    /**
     * Will calculate the index for the date string, where the rest of the string is just padding '00's.
     */
    protected static int findPrecisionIndex(String date) {
        final int MONTH_START_INDEX = 4;
        int datePrecisionIndex = date.indexOf("00", MONTH_START_INDEX);
        return datePrecisionIndex != -1 ? datePrecisionIndex : date.length();
    }

    protected int getPrecision() {
        return myPrecision;
    }

    private void validateFormat(String dateString) {
        String[] dateParts= dateString.split("-");
        try {
        Integer.parseInt(dateParts[0]);
        if (Integer.parseInt(dateParts[0]) > 9999) {
            throw new IllegalArgumentException("Year part of date string can not be more than 9999, was " + dateParts[0]);
        }
        if (dateParts.length >= 2 && Integer.parseInt(dateParts[1]) > 12) {
            throw new IllegalArgumentException("Month part of date string can not be more than 12, was " + dateParts[1]);
        }
        if (dateParts.length >= 3 && Integer.parseInt(dateParts[2]) > 31) {
            throw new IllegalArgumentException("Month part of date string can not be more than 31, was " + dateParts[2]);
        }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Invalide date format " + dateString +
                    ", the date must be of the format yyyy-MM-dd (-MM or -MM-dd are optional)");
        }
    }
}
