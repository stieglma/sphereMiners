package me.stieglmaier.sphereMiners.view;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.sun.istack.internal.Nullable;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ErrorPopup {

  public static void create(String titleText, String longMessage, @Nullable Throwable t) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("A Problem occured while running Sphere Miners");
    alert.setHeaderText(titleText);
    alert.setContentText(longMessage);

    if (t != null) {
      Label label = new Label("The exception stacktrace was:");

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      TextArea textArea = new TextArea(sw.toString());
      textArea.setEditable(false);
      textArea.setWrapText(true);

      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);
      GridPane.setVgrow(textArea, Priority.ALWAYS);
      GridPane.setHgrow(textArea, Priority.ALWAYS);

      GridPane expContent = new GridPane();
      expContent.setMaxWidth(Double.MAX_VALUE);
      expContent.add(label, 0, 0);
      expContent.add(textArea, 0, 1);

      // Set expandable Exception into the dialog pane.
      alert.getDialogPane().setExpandableContent(expContent);
    }
    alert.showAndWait();
  }
}
