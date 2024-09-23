package uob.oop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Toolkit {
    public static List<String> listVocabulary = null;
    public static List<double[]> listVectors = null;
    private static final String FILENAME_GLOVE = "glove.6B.50d_Reduced.csv";

    public static final String[] STOPWORDS = {"a", "able", "about", "across", "after", "all", "almost", "also", "am", "among", "an", "and", "any", "are", "as", "at", "be", "because", "been", "but", "by", "can", "cannot", "could", "dear", "did", "do", "does", "either", "else", "ever", "every", "for", "from", "get", "got", "had", "has", "have", "he", "her", "hers", "him", "his", "how", "however", "i", "if", "in", "into", "is", "it", "its", "just", "least", "let", "like", "likely", "may", "me", "might", "most", "must", "my", "neither", "no", "nor", "not", "of", "off", "often", "on", "only", "or", "other", "our", "own", "rather", "said", "say", "says", "she", "should", "since", "so", "some", "than", "that", "the", "their", "them", "then", "there", "these", "they", "this", "tis", "to", "too", "twas", "us", "wants", "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "would", "yet", "you", "your"};

    public void loadGlove() throws IOException {
        BufferedReader myReader = null;
        //TODO Task 4.1 - 5 marks
        listVocabulary = new ArrayList<String>();
        listVectors = new ArrayList<double[]>();

        File file = null;
        try {
            file = getFileFromResource(FILENAME_GLOVE);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        myReader = new BufferedReader(new FileReader(file));
        try{
            String line;
            while((line = myReader.readLine())!= null){
                String[] lineArr = line.split(",");
                listVocabulary.add(lineArr[0]);
                double[] vector = new double[50];
                for(int i = 1; i< lineArr.length; i++){
                    vector[i-1] = Double.parseDouble(lineArr[i]);
                }
                listVectors.add(vector);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally {
            myReader.close();
        }


    }

    private static File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = Toolkit.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(fileName);
        } else {
            return new File(resource.toURI());
        }
    }

    public List<NewsArticles> loadNews() {
        List<NewsArticles> listNews = new ArrayList<>();
        //TODO Task 4.2 - 5 Marks
        String fileSeperator = System.getProperty("file.separator");
        try {
            File folder = new File("src"+ fileSeperator + "main" + fileSeperator + "resources" + fileSeperator + "News");
            File[] listOfFiles = folder.listFiles();


            for(int i = 0; i<listOfFiles.length; i++){
                for(int j = 0; j<listOfFiles.length-1; j++){
                    if(listOfFiles[j].compareTo(listOfFiles[j+1])>0){
                        File temp = listOfFiles[j];
                        listOfFiles[j] = listOfFiles[j+1];
                        listOfFiles[j+1] = temp;
                    }
                }
            }

            for (int i = 0; i < listOfFiles.length; i++) {

                File file = listOfFiles[i];
                if (file.getName().endsWith(".htm")) {
                    BufferedReader myReader = new BufferedReader(new FileReader(file));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while((line = myReader.readLine())!= null){
                        sb.append(line + "\n");
                    }
                    myReader.close();
                    String newsTitle = HtmlParser.getNewsTitle(sb.toString());
                    String newsContent = HtmlParser.getNewsContent(sb.toString());
                    NewsArticles.DataType dataType = HtmlParser.getDataType(sb.toString());
                    String label = HtmlParser.getLabel(sb.toString());
                    listNews.add(new NewsArticles(newsTitle, newsContent, dataType, label));
                }

            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return listNews;
    }

    public static List<String> getListVocabulary() {
        return listVocabulary;
    }

    public static List<double[]> getlistVectors() {
        return listVectors;
    }
}
