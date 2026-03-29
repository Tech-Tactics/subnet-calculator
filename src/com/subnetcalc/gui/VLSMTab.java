package com.subnetcalc.gui;

import com.subnetcalc.logic.SubnetCalculator;
import com.subnetcalc.logic.VLSMPlanner;
import com.subnetcalc.logic.VLSMPlanner.SubnetRequest;
import com.subnetcalc.model.VLSMAllocation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * VLSMTab lets users define a parent network and add subnet
 * requirements by department or device type (e.g. "Marketing - 50 hosts",
 * "VoIP Phones - 80 hosts"). The planner allocates optimally sized
 * subnets and displays the results in a table.
 *
 * @author Joseph Black
 */
public class VLSMTab extends VBox {

    private final TextField parentIpField;
    private final TextField parentCidrField;
    private final TextField labelField;
    private final TextField hostsField;
    private final ListView<String> requestList;
    private final ObservableList<String> requestItems;
    private final List<SubnetRequest> requests;
    private final TextArea resultArea;
    private final Label statusLabel;

    public VLSMTab() {
        setPadding(new Insets(20));
        setSpacing(12);
        requests = new ArrayList<>();
        requestItems = FXCollections.observableArrayList();

        // Title
        Label title = new Label("VLSM Subnet Planner");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label instructions = new Label(
            "Define a parent network block, then add departments or " +
            "device types with the number of hosts each one needs.");

        // Parent network input
        GridPane parentGrid = new GridPane();
        parentGrid.setHgap(10);
        parentGrid.setVgap(8);

        Label parentIpLabel = new Label("Parent Network:");
        parentIpField = new TextField();
        parentIpField.setPromptText("10.0.0.0");
        parentIpField.setPrefWidth(160);

        Label parentCidrLabel = new Label("/ CIDR:");
        parentCidrField = new TextField();
        parentCidrField.setPromptText("16");
        parentCidrField.setPrefWidth(60);

        parentGrid.add(parentIpLabel, 0, 0);
        parentGrid.add(parentIpField, 1, 0);
        parentGrid.add(parentCidrLabel, 2, 0);
        parentGrid.add(parentCidrField, 3, 0);

        // Subnet request input
        Label addLabel = new Label("Add Subnet Requirements:");
        addLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        GridPane addGrid = new GridPane();
        addGrid.setHgap(10);
        addGrid.setVgap(8);

        Label nameLabel = new Label("Department/Device:");
        labelField = new TextField();
        labelField.setPromptText("Marketing");
        labelField.setPrefWidth(160);

        Label hostLabel = new Label("Hosts Needed:");
        hostsField = new TextField();
        hostsField.setPromptText("50");
        hostsField.setPrefWidth(80);

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addRequest());

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> removeSelected());

        addGrid.add(nameLabel, 0, 0);
        addGrid.add(labelField, 1, 0);
        addGrid.add(hostLabel, 2, 0);
        addGrid.add(hostsField, 3, 0);

        HBox addButtons = new HBox(10, addButton, removeButton);

        // Request list
        requestList = new ListView<>(requestItems);
        requestList.setPrefHeight(100);

        // Action buttons
        Button planButton = new Button("Plan Subnets");
        planButton.setDefaultButton(true);
        planButton.setOnAction(e -> planSubnets());

        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> clearAll());

        HBox actionButtons = new HBox(10, planButton, clearButton);

        // Results
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(10);
        resultArea.setFont(Font.font("Monospaced", 12));
        resultArea.setPromptText("VLSM allocation results will appear here...");

        // Status
        statusLabel = new Label("Add subnet requirements and click Plan Subnets");
        statusLabel.setStyle("-fx-text-fill: gray;");

        // Allow Enter in hosts field to trigger add
        hostsField.setOnAction(e -> addRequest());

        getChildren().addAll(title, instructions, parentGrid,
            addLabel, addGrid, addButtons, requestList,
            actionButtons, resultArea, statusLabel);
    }

    private void addRequest() {
        String label = labelField.getText().trim();
        String hostsText = hostsField.getText().trim();

        if (label.isEmpty()) {
            showError("Enter a department or device name.");
            return;
        }

        if (hostsText.isEmpty()) {
            showError("Enter the number of hosts needed.");
            return;
        }

        try {
            int hosts = Integer.parseInt(hostsText);
            if (hosts < 1) {
                showError("Host count must be at least 1.");
                return;
            }

            SubnetRequest req = new SubnetRequest(label, hosts);
            requests.add(req);
            requestItems.add(label + "  -  " + hosts + " hosts");

            labelField.clear();
            hostsField.clear();
            labelField.requestFocus();

            statusLabel.setText(requests.size() + " subnet(s) queued");
            statusLabel.setStyle("-fx-text-fill: gray;");

        } catch (NumberFormatException e) {
            showError("Host count must be a whole number.");
        }
    }

    private void removeSelected() {
        int index = requestList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            requests.remove(index);
            requestItems.remove(index);
            statusLabel.setText(requests.size() + " subnet(s) queued");
        }
    }

    private void planSubnets() {
        String parentIp = parentIpField.getText().trim();
        String parentCidrText = parentCidrField.getText().trim();

        if (parentIp.isEmpty() || parentCidrText.isEmpty()) {
            showError("Enter a parent network address and CIDR prefix.");
            return;
        }

        if (requests.isEmpty()) {
            showError("Add at least one subnet requirement first.");
            return;
        }

        try {
            int parentCidr = Integer.parseInt(parentCidrText);
            List<VLSMAllocation> allocations =
                VLSMPlanner.plan(parentIp, parentCidr, requests);

            displayAllocations(allocations, parentIp, parentCidr);
            statusLabel.setText("VLSM plan complete: " +
                                allocations.size() + " subnets allocated.");
            statusLabel.setStyle("-fx-text-fill: green;");

        } catch (NumberFormatException e) {
            showError("Parent CIDR must be a number between 0 and 32.");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void displayAllocations(List<VLSMAllocation> allocations,
                                    String parentIp, int parentCidr) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  Parent Block: %s/%d  (%d total addresses)\n\n",
            parentIp, parentCidr,
            SubnetCalculator.cidrToTotalHosts(parentCidr)));

        sb.append(String.format("  %-20s  %-6s  %-5s  %-16s  %-16s  %-16s  %s\n",
            "Department/Device", "Need", "CIDR", "Network", "Broadcast",
            "Mask", "Wasted"));
        sb.append("  " + "-".repeat(105) + "\n");

        for (VLSMAllocation a : allocations) {
            sb.append(String.format(
                "  %-20s  %-6d  /%-4d  %-16s  %-16s  %-16s  %d\n",
                a.getLabel(), a.getRequestedHosts(), a.getCidr(),
                a.getNetworkAddress(), a.getBroadcastAddress(),
                a.getSubnetMask(), a.getWastedAddresses()));
        }

        int[] summary = VLSMPlanner.getSummary(allocations);
        sb.append("\n");
        sb.append(String.format("  Total addresses allocated: %d\n",
                                summary[0]));
        sb.append(String.format("  Total addresses wasted:    %d\n",
                                summary[1]));
        sb.append(String.format("  Addresses remaining:       %d\n",
            SubnetCalculator.cidrToTotalHosts(parentCidr) - summary[0]));

        resultArea.setText(sb.toString());
    }

    private void showError(String message) {
        resultArea.setText("Error: " + message);
        statusLabel.setText("Error");
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void clearAll() {
        parentIpField.clear();
        parentCidrField.clear();
        labelField.clear();
        hostsField.clear();
        requests.clear();
        requestItems.clear();
        resultArea.clear();
        statusLabel.setText("Add subnet requirements and click Plan Subnets");
        statusLabel.setStyle("-fx-text-fill: gray;");
    }
}
