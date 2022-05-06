package org.previmer.ichthyop.ui;

import java.io.IOException;
import javafx.fxml.FXML;

public class JavaFXSecondary {

    @FXML
    private void switchToPrimary() throws IOException {
        JavaFXIchthyop.setRoot("primary");
    }
}