package com.subnetcalc.gui;

import com.subnetcalc.logic.SubnetCalculator;
import com.subnetcalc.model.SubnetResult;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * CalculatorTab provides the interface for basic subnet calculations.
 * The user enters an IP address and CIDR prefix and gets back the
 * network address, broadcast, mask, host range, and other details.
 *
 * @author Joseph Black
 */
public class CalculatorTab extends VBox {

    private final TextField ipField;
    private final TextField cidrField;
    private final TextArea resultArea;
    private final Label statusLabel;

    public CalculatorTab() {
        setPadding(new Insets(20));
        setSpacing(15);

        // Title
        Label title = new Label("IPv4 Subnet Calculator");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label instructions = new Label(
            "Enter an IPv4 address and CIDR prefix to calculate subnet details.");

        // Input section
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        Label ipLabel = new Label("IP Address:");
        ipField = new TextField();
        ipField.setPromptText("192.168.1.0");
        ipField.setPrefWidth(200);

        Label cidrLabel = new Label("CIDR Prefix (0-32):");
        cidrField = new TextField();
        cidrField.setPromptText("24");
        cidrField.setPrefWidth(80);

        Button calcButton = new Button("Calculate");
        calcButton.setDefaultButton(true);
        calcButton.setOnAction(e -> calculate());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearFields());

        inputGrid.add(ipLabel, 0, 0);
        inputGrid.add(ipField, 1, 0);
        inputGrid.add(cidrLabel, 0, 1);
        inputGrid.add(cidrField, 1, 1);

        HBox buttonBox = new HBox(10, calcButton, clearButton);

        // Results section
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(14);
        resultArea.setFont(Font.font("Monospaced", 13));
        resultArea.setPromptText("Results will appear here...");

        // Status bar
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: gray;");

        getChildren().addAll(title, instructions, inputGrid,
                             buttonBox, resultArea, statusLabel);

        // Allow Enter key in CIDR field to trigger calculation
        cidrField.setOnAction(e -> calculate());
    }

    private void calculate() {
        String ip = ipField.getText().trim();
        String cidrText = cidrField.getText().trim();

        if (ip.isEmpty()) {
            showError("Please enter an IP address.");
            return;
        }

        if (cidrText.isEmpty()) {
            showError("Please enter a CIDR prefix.");
            return;
        }

        try {
            int cidr = Integer.parseInt(cidrText);
            SubnetResult result = SubnetCalculator.calculate(ip, cidr);
            displayResult(result);
            statusLabel.setText("Calculation complete.");
            statusLabel.setStyle("-fx-text-fill: green;");

        } catch (NumberFormatException e) {
            showError("CIDR prefix must be a number between 0 and 32.");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void displayResult(SubnetResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  IP Address:       %s\n", r.getIpAddress()));
        sb.append(String.format("  CIDR Prefix:      /%d\n", r.getCidr()));
        sb.append(String.format("  IP Class:         %s\n", r.getIpClass()));
        sb.append("\n");
        sb.append(String.format("  Network Address:  %s\n", r.getNetworkAddress()));
        sb.append(String.format("  Broadcast:        %s\n", r.getBroadcastAddress()));
        sb.append(String.format("  Subnet Mask:      %s\n", r.getSubnetMask()));
        sb.append(String.format("  Wildcard Mask:    %s\n", r.getWildcardMask()));
        sb.append("\n");
        sb.append(String.format("  First Host:       %s\n", r.getFirstHost()));
        sb.append(String.format("  Last Host:        %s\n", r.getLastHost()));
        sb.append(String.format("  Total Addresses:  %d\n", r.getTotalHosts()));
        sb.append(String.format("  Usable Hosts:     %d\n", r.getUsableHosts()));
        sb.append("\n");
        sb.append(String.format("  Binary Mask:      %s\n", r.getBinarySubnetMask()));

        resultArea.setText(sb.toString());
    }

    private void showError(String message) {
        resultArea.setText("Error: " + message);
        statusLabel.setText("Error");
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void clearFields() {
        ipField.clear();
        cidrField.clear();
        resultArea.clear();
        statusLabel.setText("Ready");
        statusLabel.setStyle("-fx-text-fill: gray;");
    }
}
