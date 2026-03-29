package com.subnetcalc.gui;

import com.subnetcalc.logic.OverlapDetector;
import com.subnetcalc.model.OverlapResult;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * OverlapTab lets users enter two CIDR ranges and check whether
 * they overlap, whether one contains the other, or whether they
 * are completely separate. Useful for validating that a new subnet
 * allocation does not conflict with existing assignments.
 *
 * @author Joseph Black
 */
public class OverlapTab extends VBox {

    private final TextField subnetAField;
    private final TextField subnetBField;
    private final TextArea resultArea;
    private final Label statusLabel;
    private final Label relationshipLabel;

    public OverlapTab() {
        setPadding(new Insets(20));
        setSpacing(15);

        // Title
        Label title = new Label("Subnet Overlap Detector");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label instructions = new Label(
            "Enter two subnets in CIDR notation to check if they " +
            "overlap, if one contains the other, or if they are separate.");

        // Input section
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        Label labelA = new Label("Subnet A:");
        subnetAField = new TextField();
        subnetAField.setPromptText("192.168.1.0/24");
        subnetAField.setPrefWidth(220);

        Label labelB = new Label("Subnet B:");
        subnetBField = new TextField();
        subnetBField.setPromptText("192.168.1.128/25");
        subnetBField.setPrefWidth(220);

        inputGrid.add(labelA, 0, 0);
        inputGrid.add(subnetAField, 1, 0);
        inputGrid.add(labelB, 0, 1);
        inputGrid.add(subnetBField, 1, 1);

        // Buttons
        Button compareButton = new Button("Compare");
        compareButton.setDefaultButton(true);
        compareButton.setOnAction(e -> compare());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearFields());

        HBox buttonBox = new HBox(10, compareButton, clearButton);

        // Relationship indicator
        relationshipLabel = new Label("");
        relationshipLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        relationshipLabel.setPadding(new Insets(10, 0, 5, 0));

        // Results
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(10);
        resultArea.setFont(Font.font("Monospaced", 13));
        resultArea.setPromptText("Comparison results will appear here...");

        // Status
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: gray;");

        // Allow Enter in subnet B to trigger compare
        subnetBField.setOnAction(e -> compare());

        getChildren().addAll(title, instructions, inputGrid, buttonBox,
                             relationshipLabel, resultArea, statusLabel);
    }

    private void compare() {
        String a = subnetAField.getText().trim();
        String b = subnetBField.getText().trim();

        if (a.isEmpty() || b.isEmpty()) {
            showError("Both subnet fields are required.");
            return;
        }

        // Add /32 if user forgot the prefix
        if (!a.contains("/")) a += "/32";
        if (!b.contains("/")) b += "/32";

        try {
            OverlapResult result = OverlapDetector.compare(a, b);
            displayResult(result);
            statusLabel.setText("Comparison complete.");
            statusLabel.setStyle("-fx-text-fill: green;");

        } catch (NumberFormatException e) {
            showError("CIDR prefix must be a number. Use format: x.x.x.x/n");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void displayResult(OverlapResult r) {
        String relText;
        String relColor;

        switch (r.getRelationship()) {
            case DISJOINT:
                relText = "DISJOINT - No overlap";
                relColor = "-fx-text-fill: green;";
                break;
            case IDENTICAL:
                relText = "IDENTICAL - Same range";
                relColor = "-fx-text-fill: blue;";
                break;
            case A_CONTAINS_B:
                relText = "A CONTAINS B";
                relColor = "-fx-text-fill: orange;";
                break;
            case B_CONTAINS_A:
                relText = "B CONTAINS A";
                relColor = "-fx-text-fill: orange;";
                break;
            case OVERLAPPING:
                relText = "OVERLAP DETECTED";
                relColor = "-fx-text-fill: red;";
                break;
            default:
                relText = "Unknown";
                relColor = "-fx-text-fill: gray;";
        }

        relationshipLabel.setText(relText);
        relationshipLabel.setStyle(relColor);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  Subnet A:        %s\n", r.getSubnetA()));
        sb.append(String.format("  Subnet B:        %s\n", r.getSubnetB()));
        sb.append("\n");
        sb.append(String.format("  Relationship:    %s\n", r.getRelationship()));
        sb.append(String.format("  Overlap Range:   %s\n", r.getOverlapRange()));
        sb.append("\n");
        sb.append(String.format("  Details:\n  %s\n", r.getDescription()));

        resultArea.setText(sb.toString());
    }

    private void showError(String message) {
        relationshipLabel.setText("");
        resultArea.setText("Error: " + message);
        statusLabel.setText("Error");
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void clearFields() {
        subnetAField.clear();
        subnetBField.clear();
        resultArea.clear();
        relationshipLabel.setText("");
        statusLabel.setText("Ready");
        statusLabel.setStyle("-fx-text-fill: gray;");
    }
}
