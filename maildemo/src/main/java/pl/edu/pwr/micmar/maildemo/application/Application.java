package pl.edu.pwr.micmar.maildemo.application;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ai.djl.training.util.ProgressBar;
import pl.edu.pwr.micmar.maildemo.db.SQLiteConnection;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;

public class Application extends javafx.application.Application {
    public static String getJARpath() throws URISyntaxException {
        String jarPath = Application.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        return new File(jarPath).getAbsoluteFile().getParent();
    }
    public static FXMLLoader mainController;
    public static ZooModel<String, float[]> model;
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        System.out.println(getJARpath());
        Thread thread = new Thread(new DownloadLLM());
        thread.start();
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


}
class SBERTTranslator implements Translator<String, float[]> {
    private HuggingFaceTokenizer tokenizer;

    public void prepare(NDManager manager) {
        if (tokenizer == null) {
            tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2");
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        // Ensure tokenizer is initialized
        if (tokenizer == null) {
            tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2");
        }

        // Tokenize the input text to get `input_ids` and `attention_mask`
        var encoding = tokenizer.encode(input);

        // Convert `input_ids` and `attention_mask` to NDArrays
        NDArray inputIdsArray = ctx.getNDManager().create(encoding.getIds());
        NDArray attentionMaskArray = ctx.getNDManager().create(encoding.getAttentionMask());

        // Pass both `input_ids` and `attention_mask` in the NDList
        return new NDList(inputIdsArray, attentionMaskArray);
    }

    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) throws TranslateException {
        // Check how many tensors are returned
        if (list.size() == 0) {
            throw new TranslateException("Expected at least one output tensor.");
        }

        NDArray outputArray = list.get(0);

        return outputArray.toFloatArray();
    }
}
class DownloadLLM implements Runnable {
    @Override
    public void run() {
        SQLiteConnection.connect();
        Criteria<String, float[]> criteria= Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
                .optTranslator(new SBERTTranslator())  // Ustawienie niestandardowego tłumacza
                .optProgress(new ProgressBar())
                .optEngine("PyTorch")
                .optDevice(Device.cpu())
                .build();
        try {
            Application.model = criteria.loadModel();
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new RuntimeException(e);
        }
    }
}
