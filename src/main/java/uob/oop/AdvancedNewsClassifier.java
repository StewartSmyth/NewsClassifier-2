package uob.oop;

import org.apache.commons.lang3.time.StopWatch;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdvancedNewsClassifier {
    public Toolkit myTK = null;
    public static List<NewsArticles> listNews = null;
    public static List<Glove> listGlove = null;
    public List<ArticlesEmbedding> listEmbedding = null;
    public MultiLayerNetwork myNeuralNetwork = null;

    public final int BATCHSIZE = 10;

    public int embeddingSize = 0;
    private static StopWatch mySW = new StopWatch();

    public AdvancedNewsClassifier() throws IOException {
        myTK = new Toolkit();
        myTK.loadGlove();
        listNews = myTK.loadNews();
        listGlove = createGloveList();
        listEmbedding = loadData();
    }

    public static void main(String[] args) throws Exception {
        mySW.start();
        AdvancedNewsClassifier myANC = new AdvancedNewsClassifier();

        myANC.embeddingSize = myANC.calculateEmbeddingSize(myANC.listEmbedding);
        myANC.populateEmbedding();
        myANC.myNeuralNetwork = myANC.buildNeuralNetwork(2);
        myANC.predictResult(myANC.listEmbedding);
        myANC.printResults();
        mySW.stop();
        System.out.println("Total elapsed time: " + mySW.getTime());
    }

    public List<Glove> createGloveList() {
        List<Glove> listResult = new ArrayList<>();
        //TODO Task 6.1 - 5 Marks
        List<String> vocabulary = Toolkit.getListVocabulary();
        List<double[]> vectors = Toolkit.getlistVectors();
        String[] stopWords = Toolkit.STOPWORDS;
        for (int i = 0; i< vocabulary.size(); i++) {
            boolean isStop = false;
            for(int j=0; j<stopWords.length; j++){
                if(vocabulary.get(i).equals(stopWords[j])){
                    isStop = true;
                    break;
                }
            }
            if(isStop == false) {
                listResult.add(new Glove(vocabulary.get(i), new Vector(vectors.get(i))));
            }
        }
        return listResult;
    }


    public static List<ArticlesEmbedding> loadData() {
        List<ArticlesEmbedding> listEmbedding = new ArrayList<>();
        for (NewsArticles news : listNews) {
            ArticlesEmbedding myAE = new ArticlesEmbedding(news.getNewsTitle(), news.getNewsContent(), news.getNewsType(), news.getNewsLabel());
            listEmbedding.add(myAE);
        }
        return listEmbedding;
    }

    public int calculateEmbeddingSize(List<ArticlesEmbedding> _listEmbedding) {
        int intMedian = -1;
        //TODO Task 6.2 - 5 Marks
        int[] lengths = new int[_listEmbedding.size()];

        for(int i=0;i<_listEmbedding.size();i++){
            String content = _listEmbedding.get(i).getNewsContent();
            String[] contentArr = content.split(" ");
            int length = 0;
            for(int j = 0; j<contentArr.length; j++){
                for(int k = 0; k<listGlove.size(); k++){
                    if(listGlove.get(k).getVocabulary().equals(contentArr[j])){
                        length+=1;
                    }
                }
            }
            lengths[i] = length;
        }

        for(int i=0; i<lengths.length; i++){
            for(int j=0; j<lengths.length-1; j++){
                if(lengths[j]>lengths[j+1]){
                    int temp = lengths[j];
                    lengths[j] = lengths[j+1];
                    lengths[j+1] = temp;
                }
            }
        }


        if(lengths.length % 2 == 0){
            intMedian = (lengths[lengths.length/2] + lengths[(lengths.length/2)+1])/2;
        }
        else{
            intMedian = lengths[(lengths.length+1)/2];
        }

        return intMedian;
    }

    public void populateEmbedding() {
        //TODO Task 6.3 - 10 Marks
        for(int i=0; i<listEmbedding.size(); i++){
            try {
                listEmbedding.get(i).getEmbedding();
            }
            catch(InvalidSizeException e){
                listEmbedding.get(i).setEmbeddingSize(embeddingSize);
            }catch(InvalidTextException e){
                listEmbedding.get(i).getNewsContent();
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }

    }

    public DataSetIterator populateRecordReaders(int _numberOfClasses) throws Exception {
        ListDataSetIterator myDataIterator = null;
        List<DataSet> listDS = new ArrayList<>();
        INDArray inputNDArray = null;
        INDArray outputNDArray = null;

        //TODO Task 6.4 - 8 Marks


        for(int i = 0; i<listEmbedding.size(); i++){

            if(listEmbedding.get(i).getNewsType().equals(NewsArticles.DataType.Training)){

                outputNDArray = Nd4j.zeros(1,_numberOfClasses);
                int ind = Integer.parseInt(listEmbedding.get(i).getNewsLabel())-1;
                outputNDArray.putScalar(0, ind,1);
                listDS.add(new DataSet(listEmbedding.get(i).getEmbedding(), outputNDArray));
            }
        }
        return new ListDataSetIterator(listDS, BATCHSIZE);
    }

    public MultiLayerNetwork buildNeuralNetwork(int _numOfClasses) throws Exception {
        DataSetIterator trainIter = populateRecordReaders(_numOfClasses);
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(42)
                .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .updater(Adam.builder().learningRate(0.02).beta1(0.9).beta2(0.999).build())
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder().nIn(embeddingSize).nOut(15)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.HINGE)
                        .activation(Activation.SOFTMAX)
                        .nIn(15).nOut(_numOfClasses).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        for (int n = 0; n < 100; n++) {
            model.fit(trainIter);
            trainIter.reset();
        }
        return model;
    }

    public List<Integer> predictResult(List<ArticlesEmbedding> _listEmbedding) throws Exception {
        List<Integer> listResult = new ArrayList<>();
        //TODO Task 6.5 - 8 Marks
        for(int i = 0; i<_listEmbedding.size(); i++){
            if(_listEmbedding.get(i).getNewsType().equals(NewsArticles.DataType.Testing)){
                int[] neuralNetworkResult = myNeuralNetwork.predict(_listEmbedding.get(i).getEmbedding());
                listResult.add(neuralNetworkResult[0]);
                listEmbedding.get(i).setNewsLabel(Integer.toString(neuralNetworkResult[0]+1));
            }
        }


        return listResult;
    }

    public void printResults() {
        //TODO Task 6.6 - 6.5 Marks
        StringBuilder sb = new StringBuilder();
        int numberOfClasses = 0;
        for(ArticlesEmbedding article :listEmbedding){
            if(Integer.parseInt(article.getNewsLabel())>numberOfClasses){
                numberOfClasses = Integer.parseInt(article.getNewsLabel());
            }
        }
        for(int i = 1; i<numberOfClasses+1; i++){
            System.out.println("Group "+ i);
            for(ArticlesEmbedding article :listEmbedding) {
                if (article.getNewsType().equals(NewsArticles.DataType.Testing)) {
                    if (Integer.parseInt(article.getNewsLabel()) == i) {
                        System.out.println(article.getNewsTitle());
                    }
                }
            }
        }
    }
}
