package org.fxmisc.flowless;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CellCreationWithIndexTest extends FlowlessTestBase {

    private ObservableList<String> items;
    private Counter cellCreations = new Counter();
    private VirtualFlow<String, ?> flow;

    @Override
    public void start(Stage stage) {
        // set up items
        items = FXCollections.observableArrayList();
        for(int i = 0; i < 20; ++i) {
            items.addAll("red", "green", "blue", "purple");
        }

        // set up virtual flow
        flow = VirtualFlow.createVertical(
                items,
                (index, color) -> {
                    cellCreations.inc();
                    Region reg = new Label( "  "+ index +"\t"+ color );
                    reg.setPrefHeight(16.0);
                    reg.setStyle("-fx-background-color: " + color);
                    return Cell.wrapNode(reg);
                });

        StackPane stackPane = new StackPane();
        // 25 cells (each 16px high) fit into the viewport
        stackPane.getChildren().add(flow);
        stage.setScene(new Scene(stackPane, 200, 400));
        stage.show();
    }

    @Before
    public void setup() {
        cellCreations.reset();
    }

    @Test
    public void updating_an_item_in_viewport_only_creates_cell_once() {
        // update an item in the viewport
        interact(() -> items.set(10, "yellow"));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals("10", getCellText(10));
    }

    private String getCellText(int index) {
    	return ((Label) flow.getCell(index).getNode()).getText().substring(2,4);
    }

    @Test
    public void updating_an_item_outside_viewport_does_not_create_cell() {
        // update an item outside the viewport
        interact(() -> items.set(30, "yellow"));
        assertEquals(0, cellCreations.getAndReset());
    }

    @Test
    public void deleting_an_item_in_viewport_only_creates_cell_once() {
        // delete an item in the middle of the viewport
        interact(() -> items.remove(12));
        assertEquals(1, cellCreations.getAndReset());
    }

    @Test
    public void adding_an_item_in_viewport_only_creates_cell_once() {
        // add an item in the middle of the viewport
        interact(() -> items.add(12, "yellow"));
        assertEquals(1, cellCreations.getAndReset());
        assertEquals("12", getCellText(12));
    }
}