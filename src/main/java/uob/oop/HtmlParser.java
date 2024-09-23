package uob.oop;

public class HtmlParser {
    /***
     * Extract the title of the news from the _htmlCode.
     * @param _htmlCode Contains the full HTML string from a specific news. E.g. 01.htm.
     * @return Return the title if it's been found. Otherwise, return "Title not found!".
     */
    public static String getNewsTitle(String _htmlCode) {
        String titleTagOpen = "<title>";
        String titleTagClose = "</title>";

        int titleStart = _htmlCode.indexOf(titleTagOpen) + titleTagOpen.length();
        int titleEnd = _htmlCode.indexOf(titleTagClose);

        if (titleStart != -1 && titleEnd != -1 && titleEnd > titleStart) {
            String strFullTitle = _htmlCode.substring(titleStart, titleEnd);
            return strFullTitle.substring(0, strFullTitle.indexOf(" |"));
        }

        return "Title not found!";
    }

    /***
     * Extract the content of the news from the _htmlCode.
     * @param _htmlCode Contains the full HTML string from a specific news. E.g. 01.htm.
     * @return Return the content if it's been found. Otherwise, return "Content not found!".
     */
    public static String getNewsContent(String _htmlCode) {
        String contentTagOpen = "\"articleBody\": \"";
        String contentTagClose = " \",\"mainEntityOfPage\":";

        int contentStart = _htmlCode.indexOf(contentTagOpen) + contentTagOpen.length();
        int contentEnd = _htmlCode.indexOf(contentTagClose);

        if (contentStart != -1 && contentEnd != -1 && contentEnd > contentStart) {
            return _htmlCode.substring(contentStart, contentEnd).toLowerCase();
        }

        return "Content not found!";
    }

    public static NewsArticles.DataType getDataType(String _htmlCode) {
        //TODO Task 3.1 - 1.5 Marks
        NewsArticles.DataType dataType = null;

        String datatypeTagOpen = "<datatype>";
        String datatypeTagClose = "</datatype>";

        int dataTypeStart = _htmlCode.indexOf(datatypeTagOpen) + datatypeTagOpen.length();
        int dataTypeEnd = _htmlCode.indexOf(datatypeTagClose);

        if(dataTypeStart-datatypeTagOpen.length() != -1 && dataTypeEnd != -1 && dataTypeEnd>dataTypeStart){
            String DT = _htmlCode.substring(dataTypeStart, dataTypeEnd);
            if(DT.equals("Testing")){
                dataType = NewsArticles.DataType.Testing;
            }
            else if(DT.equals("Training")){
                dataType = NewsArticles.DataType.Training;
            }
        }
        else{
            dataType = NewsArticles.DataType.Testing;
        }

        return dataType; //Please modify the return value.
    }

    public static String getLabel (String _htmlCode) {
        //TODO Task 3.2 - 1.5 Marks

        String labelOpen = "<label>";
        String labelClose = "</label>";

        int labelStart = _htmlCode.indexOf(labelOpen) + labelOpen.length();
        int labelEnd = _htmlCode.indexOf(labelClose);

        if(labelStart-labelOpen.length() != -1 && labelEnd != -1 && labelEnd>labelStart){
            return _htmlCode.substring(labelStart, labelEnd);
        }

        return "-1"; //Please modify the return value.
    }


}
