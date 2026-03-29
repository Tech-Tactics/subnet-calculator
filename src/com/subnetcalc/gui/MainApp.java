package com.subnetcalc.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * MainApp is the entry point for the subnet calculator GUI.
 * Uses a TabPane to organize the three modules: basic calculator,
 * VLSM planner, and overlap detector.
 *
 * @author Joseph Black
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab calcTab = new Tab("Subnet Calculator");
        calcTab.setContent(new CalculatorTab());

        Tab vlsmTab = new Tab("VLSM Planner");
        vlsmTab.setContent(new VLSMTab());

        Tab overlapTab = new Tab("Overlap Detector");
        overlapTab.setContent(new OverlapTab());

        tabPane.getTabs().addAll(calcTab, vlsmTab, overlapTab);

        Scene scene = new Scene(tabPane, 780, 620);
        primaryStage.setTitle("IPv4 Subnet Calculator");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
