package org.grobid.core.features;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import org.grobid.core.utilities.TextUtilities;

/**
 * Class for features used for biotech entity identification in raw texts such as scientific articles
 * and patent descriptions.
 *
 * @author Patrice Lopez
 */
public class FeaturesVectorBiotechEntity {
    // default bins for relative position, to be set experimentally
	// highly depends on the type of document to be processed 
    static private int nbBins = 12;

    public String string = null;     // lexical feature
    public String label = null;     // label if known

    public String capitalisation = null;// one of INITCAP, ALLCAPS, NOCAPS
    public String digit;                  // one of ALLDIGIT, CONTAINDIGIT, NODIGIT
    public boolean singleChar = false;

    //public boolean properName = false;
    public boolean commonName = false;

    public String punctType = null;
    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    // OPENQUOTE, ENDQUOTE

    public boolean isKnownBiotechToken = false;
    public boolean isKnownBiotechNameToken = false;

    public int relativeDocumentPosition = -1;

	public String shadowNumber = null; // Convert digits to “0” 
	
	public String wordShape = null; 
	// Convert upper-case letters to "X", lower- case letters to "x", digits to "d" and other to "c"  
	// there is also a trimmed variant where sequence of similar character shapes are reduced to one
	// converted character shape
	public String wordShapeTrimmed = null;
	
    public FeaturesVectorBiotechEntity() {
    }

    public String printVector() {
        if (string == null) return null;
        if (string.length() == 0) return null;
        StringBuffer res = new StringBuffer();

        // token string (1)
        res.append(string);

        // lowercase string
        res.append(" " + string.toLowerCase());

        // prefix (4)
        res.append(" " + string.substring(0, 1));

        if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

        if (string.length() > 3)
            res.append(" " + string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
        else
            res.append(" " + string.substring(0, 1));

		/*if (string.length() > 4)
            res.append(" " + string.substring(0, 5));
        else if (string.length() > 3)
            res.append(" " + string.substring(0, 4));
        else if (string.length() > 2)
            res.append(" " + string.substring(0, 3));
        else if (string.length() > 1)
            res.append(" " + string.substring(0, 2));
		else
            res.append(" " + string.substring(0, 1));*/

        // suffix (4)
        res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

        if (string.length() > 3)
            res.append(" " + string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.substring(string.length() - 2, string.length()));
        else
            res.append(" " + string.charAt(string.length() - 1));

		/*if (string.length() > 4)
            res.append(" " + string.substring(string.length() - 5, string.length()));
        else if (string.length() > 3)
            res.append(" " + string.substring(string.length() - 4, string.length()));
        else if (string.length() > 2)
            res.append(" " + string.substring(string.length() - 3, string.length()));
        else if (string.length() > 1)
            res.append(" " + string.charAt(string.length() - 2));
		else
            res.append(" " + string.charAt(string.length() - 1));*/

        // capitalisation (1)
        if (digit.equals("ALLDIGIT"))
            res.append(" NOCAPS");
        else
            res.append(" " + capitalisation);

        // digit information (1)
        res.append(" " + digit);

        // character information (1)
        if (singleChar)
            res.append(" 1");
        else
            res.append(" 0");

        // lexical information (2)
/*        if (properName)
            res.append(" 1");
        else
            res.append(" 0");
*/
		// propername slot is replaced by a useless feature always to 0...
		//res.append(" 0");

        if (commonName)
            res.append(" 1");
        else
            res.append(" 0");

        // biotech vocabulary information (2)
        /*if (isKnownBiotechToken)
            res.append(" 1");
        else
            res.append(" 0");*/

        /*if (isKnownBiotechNameToken)
            res.append(" 1");
        else
            res.append(" 0");*/

        // punctuation information (1)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

        // token length
        //res.append(" " + string.length());

        // relative document position
        //res.append(" " + relativeDocumentPosition);

		// shadow number
		res.append(" " + shadowNumber);
		
		// word shape
		res.append(" " + wordShape);
		
		// word shape trimmed
		res.append(" " + wordShapeTrimmed);

        // label - for training data (1)
        if (label != null)
            res.append(" " + label + "\n");
        else
            res.append(" 0\n");

        return res.toString();
    }

    /**
     * Add the features for the biotech entity extraction model.
     */
    static public FeaturesVectorBiotechEntity addFeaturesBiotechEntities(String line,
                                                                         int totalLength,
                                                                         int position,
                                                                         boolean isBiotechToken,
                                                                         boolean isBiotechNameToken) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();

        FeaturesVectorBiotechEntity featuresVector = new FeaturesVectorBiotechEntity();
        StringTokenizer st = new StringTokenizer(line, "\t ");
        if (st.hasMoreTokens()) {
            String word = st.nextToken();
            String label = null;
            if (st.hasMoreTokens())
                label = st.nextToken();

            featuresVector.string = word;
            featuresVector.label = label;

            if (word.length() == 1) {
                featuresVector.singleChar = true;
            }

            if (featureFactory.test_all_capital(word))
                featuresVector.capitalisation = "ALLCAPS";
            else if (featureFactory.test_first_capital(word))
                featuresVector.capitalisation = "INITCAP";
            else
                featuresVector.capitalisation = "NOCAPS";

            if (featureFactory.test_number(word))
                featuresVector.digit = "ALLDIGIT";
            else if (featureFactory.test_digit(word))
                featuresVector.digit = "CONTAINDIGIT";
            else
                featuresVector.digit = "NODIGIT";

            if (featureFactory.test_common(word))
                featuresVector.commonName = true;

            //if (featureFactory.test_names(word))
            //    featuresVector.properName = true;

            Matcher m0 = featureFactory.isPunct.matcher(word);
            if (m0.find()) {
                featuresVector.punctType = "PUNCT";
            }
            if ((word.equals("(")) | (word.equals("["))) {
                featuresVector.punctType = "OPENBRACKET";
            } else if ((word.equals(")")) | (word.equals("]"))) {
                featuresVector.punctType = "ENDBRACKET";
            } else if (word.equals(".")) {
                featuresVector.punctType = "DOT";
            } else if (word.equals(",")) {
                featuresVector.punctType = "COMMA";
            } else if (word.equals("-")) {
                featuresVector.punctType = "HYPHEN";
            } else if (word.equals("\"") | word.equals("\'") | word.equals("`")) {
                featuresVector.punctType = "QUOTE";
            }

            if (featuresVector.capitalisation == null)
                featuresVector.capitalisation = "NOCAPS";

            if (featuresVector.digit == null)
                featuresVector.digit = "NODIGIT";

            if (featuresVector.punctType == null)
                featuresVector.punctType = "NOPUNCT";

            featuresVector.relativeDocumentPosition =
                    featureFactory.linearScaling(position, totalLength, nbBins);

            featuresVector.isKnownBiotechToken = isBiotechToken;

            featuresVector.isKnownBiotechNameToken = isBiotechNameToken;            

			featuresVector.shadowNumber = TextUtilities.shadowNumbers(word);
			
			featuresVector.wordShape = TextUtilities.wordShape(word);
			
			featuresVector.wordShapeTrimmed = TextUtilities.wordShapeTrimmed(word);
        }

        return featuresVector;
    }

}
	
	
