package org.fxmisc.flowless;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BigCellCreationAndLayoutEfficiencyTest extends FlowlessTestBase {

    private ObservableList<String> items;
    private Counter cellCreations = new Counter();
    private VirtualFlow<String, ?> flow;

    @Override
    public void start(Stage stage) {
        // set up items
        items = FXCollections.observableArrayList();
        items.addAll("red", "green", "blue", "purple");

        // set up virtual flow
        flow = VirtualFlow.createVertical(
                items,
                color -> {
                    cellCreations.inc();
                    Region reg = new Region();
                    reg.setStyle("-fx-background-color: " + color);
                    if ( color.equals( "purple" ) ) reg.setPrefHeight(500.0);
                    else reg.setPrefHeight(100.0);
                    return Cell.wrapNode(reg);
                });

        StackPane stackPane = new StackPane(flow);
        stage.setScene(new Scene(stackPane, 200, 400));
        stage.show();
    }

    @Test // Relates to issue #70
    public void having_a_very_tall_item_in_viewport_only_creates_and_lays_out_cell_once() {
    	// if this fails then it's probably because the very big purple cell is being created multiple times
        assertEquals(4, cellCreations.get());
    }

}