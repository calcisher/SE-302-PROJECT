package ClassAssignmentSystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class MainApp extends Application {
    //Main Class Of ClassAssignmentSystem
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainGUI.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Main Interface");
        primaryStage.setScene(new Scene(root, 850, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
