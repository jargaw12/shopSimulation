package msk.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiApplication extends Application {
    public static GuiController controller;


//    public static void main(String[] args) {
//        launch(args);
//    }

    public void run(String[] args) {
        launch(GuiApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        GuiFederate guiFederate = new GuiFederate();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/gui_federate.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Symulacja sklepu");
        controller = loader.getController();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
