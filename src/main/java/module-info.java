/**
 * The {@code com.example.demo} module defines the main module for the Moorhuhn Game application.
 *
 * <p>This module requires the {@code javafx.controls} and {@code javafx.fxml} modules for JavaFX functionalities.</p>
 *
 * <p>It opens the {@code com.example.demo} package to the {@code javafx.fxml} module and exports the
 * {@code com.example.demo} package for use by other modules.</p>
 */
module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
}