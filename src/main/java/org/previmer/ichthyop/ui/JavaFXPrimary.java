package org.previmer.ichthyop.ui;

import java.io.IOException;
import javafx.fxml.FXML;

public class JavaFXPrimary {

    @FXML
    private void switchToSecondary() throws IOException {
        JavaFXIchthyop.setRoot("secondary");
    }
}
