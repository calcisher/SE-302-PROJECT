package ClassAssignmentSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class ScheduleController {

    @FXML
    private GridPane gridPane;



private final String[][] scheduleData = {
    {"SE115-C201", "", "MATH240-C301", "", "FENG101-M206"},
    {"", "SE302-C202", "", "CE215\nM101", ""},
    { "", "PHYS100-ML103", "", "CHEM100-ML104", "SE307-C203", ""},
    {"CE221-M102", "MATH153-ML105", "", "", "SE350-C204"},
    { "", "", "", "", ""},
    {"SE360-C205", "", "", "SE380-C206", ""},
    {"", "CE323-M201", "MATH250-M01", "", "SE431-C208"},
    { "SE420-C207", "", "", "CE342-M202", ""},
    {"", "FENG345-ML102", "", "CE345-M203", ""}};

    @FXML
    public void initialize() {
        // Populate GridPane Labels dynamically using scheduleData
        for (int row = 0; row < scheduleData.length; row++) {
            for (int col = 0; col < scheduleData[row].length; col++) {
                Label label = new Label(scheduleData[row][col]); // Set text from array
                label.setFont(new javafx.scene.text.Font(14));
                int finalRow = row;
                int finalCol = col;

                // Add click event to edit the label text dynamically
                label.setOnMouseClicked(event -> handleLabelClick(event, label, finalRow, finalCol));

                // Add label to gridPane (offset column by 1 to account for time column)
                gridPane.add(label, col + 1, row + 1);
            }
        }
    }

   private void handleLabelClick(MouseEvent event, Label label, int row, int col) {
       // Simulate editing the label's text (you can customize this logic)
       String newText = "Updated Course\nUpdated Room";
       label.setText(newText);
       scheduleData[row][col] = newText; // Update scheduleData array
   }

}


