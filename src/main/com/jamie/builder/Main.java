package com.jamie.builder;

import com.jamie.builder.components.root.RootController;
import com.jamie.builder.data.AppTime;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/root.fxml"));
        Parent root = loader.load();
        RootController controller = loader.getController();

        primaryStage.setTitle("Project Builder");
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(this.getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        controller.setStage(primaryStage);
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icon.png")));
        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> {
            controller.cancelBuild();
            AppTime.end();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
