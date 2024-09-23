package uob.oop;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import java.util.Properties;


public class ArticlesEmbedding extends NewsArticles {
    private int intSize = -1;
    private String processedText = "";

    private INDArray newsEmbedding = Nd4j.create(0);

    public ArticlesEmbedding(String _title, String _content, NewsArticles.DataType _type, String _label) {
        //TODO Task 5.1 - 1 Mark
        super(_title,_content,_type,_label);
    }

    public void setEmbeddingSize(int _size) {
        //TODO Task 5.2 - 0.5 Marks
        intSize = _size;
    }

    public int getEmbeddingSize(){
        return intSize;
    }

    @Override
    public String getNewsContent() {
        //TODO Task 5.3 - 10 Marks

        if(processedText != ""){
            return processedText;
        }
        String newsContent = super.getNewsContent();

        newsContent = textCleaning(newsContent);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument document = pipeline.processToCoreDocument(newsContent);



        StringBuilder sb = new StringBuilder();
        for (CoreLabel tok : document.tokens()) {
            sb.append(tok.lemma()).append(" ");
        }

        String[] stopWords = Toolkit.STOPWORDS;
        String[] words = sb.toString().split(" ");
        StringBuilder processed = new StringBuilder();
        for(int i = 0; i<words.length; i++) {
            boolean wordInStop = false;
            for (int j = 0; j < stopWords.length; j++) {
                if (words[i].equals(stopWords[j])) {
                    wordInStop = true;
                    break;
                }
            }
            if (!wordInStop) {
                processed.append(words[i]).append(" ");
            }
        }

        processedText = processed.toString().trim().toLowerCase();

        return processedText.trim().toLowerCase();
    }

    public INDArray getEmbedding() throws Exception {
        //TODO Task 5.4 - 20 Marks

        if(!newsEmbedding.isEmpty()){
            return Nd4j.vstack(newsEmbedding.mean(1));
        }

        if(intSize == -1){
            throw new InvalidSizeException("Invalid size");
        }
        if(processedText.isEmpty()){
            throw new InvalidTextException("Invalid text");
        }

        String[] splitText = processedText.split(" ");


        int vectorSize = AdvancedNewsClassifier.listGlove.get(0).getVector().getVectorSize();

        newsEmbedding = Nd4j.zeros(intSize, vectorSize);

        int length = splitText.length;
        int row = 0;

        for(int i = 0; i<splitText.length && row<intSize; i++){
            for(int j = 0; j<AdvancedNewsClassifier.listGlove.size(); j++){
                if(AdvancedNewsClassifier.listGlove.get(j).getVocabulary().equals(splitText[i])){
                    newsEmbedding.putRow(row, Nd4j.create(AdvancedNewsClassifier.listGlove.get(j).getVector().getAllElements()));
                    row++;
                    break;
                }
            }
        }

        return Nd4j.vstack(newsEmbedding.mean(1));
    }

    /***
     * Clean the given (_content) text by removing all the characters that are not 'a'-'z', '0'-'9' and white space.
     * @param _content Text that need to be cleaned.
     * @return The cleaned text.
     */
    private static String textCleaning(String _content) {
        StringBuilder sbContent = new StringBuilder();

        for (char c : _content.toLowerCase().toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || Character.isWhitespace(c)) {
                sbContent.append(c);
            }
        }

        return sbContent.toString().trim();
    }
}
