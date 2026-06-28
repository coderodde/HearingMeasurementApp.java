package io.github.coderodde.human.tool.hearing;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * This class contains the entry point to the actual hearing tool program.
 */
public class HearingMeasurementApp extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        Label label = new Label(BeepingThread.MIN_FREQUENCY + " Hz");
        AppControlStatefulButton button = new AppControlStatefulButton();
        
        Font labelFont  = Font.font("System", FontWeight.BOLD, 48);
        Font buttonFont = Font.font("System", FontWeight.BOLD, 20);
        
        label.setMaxSize (Double.MAX_VALUE, Double.MAX_VALUE);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        label .setFont(labelFont);
        button.setFont(buttonFont);
        
        label .setPrefHeight(3);
        button.setPrefHeight(1);
        
        label .setAlignment(Pos.CENTER);
        button.setAlignment(Pos.CENTER);
        
        VBox box = new VBox();
        
        VBox.setVgrow(label,  Priority.ALWAYS);
        VBox.setVgrow(button, Priority.ALWAYS);
        
        BeepingThread beepingThread = new BeepingThread(label);
        beepingThread.start();
        
        button.setOnAction(e -> { 
            button.click();
            
            switch (button.getState()) {
                case CONTINUE -> beepingThread.resumeBeeping();   
                case PAUSED   -> beepingThread.pauseBeeping();
            }
        });
        
        box.getChildren().addAll(label, button);
        
        Scene scene = new Scene(box, 350, 250);
        
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
                beepingThread.terminate();
            }
        });
        
        stage.setOnCloseRequest(e -> {
            beepingThread.interrupt();
        });
        
        stage.setTitle("Hearing test");
        stage.setScene(scene);
        stage.show();
    }
}
