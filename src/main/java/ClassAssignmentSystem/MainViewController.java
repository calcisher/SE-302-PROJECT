package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainViewController {

    @FXML
    private Button showCourseListButton;

    @FXML
    private Button showClassListButton;

    @FXML
    public void initialize() {
        // Ders Listesi Butonu İçin Aksiyon
        showCourseListButton.setOnAction(event -> showCourseList());

        // Sınıf Listesi Butonu İçin Aksiyon
        showClassListButton.setOnAction(event -> showClassList());
    }

    private void showCourseList() {
        Stage courseListStage = new Stage();
        courseListStage.setTitle("Course List");
        String[] studentList = {
                "İlker Korkmaz", "Mert Kıracıoğlu", "Emir Aydın", "Muzaffer Koray Cengiz", "Berke Işık",
                "Nesibe Nur Pekçakar", "Hilal Sinem Sayar", "Hüseyin Ege Akın", "Berat Bora Altaş", "Kadir Ay",
                "Doğa Güneş", "Mert Koçuş", "Aleyna Kök", "Elif Sude Özmen", "Betül Özsan", "Eser Poyraz",
                "Arda Sarı", "Ege Sevinci", "Özcan Burak Şanlılar", "Bekir Can Türkmen", "İpek Sude Yavaş",
                "Ebru Burhan", "Hulki Enes Uysal", "Ege Çakıcı", "Osman Serhan Aydoğan", "Hüseyin Eren Ceyhan",
                "Betül Sinem Çetiner", "Meltem Demir", "Tuna Demirci", "Emiray Durmaz", "Aras Fırat",
                "Hasan Berk Görgülü", "Mahmut Özgür Kızıl", "Ediz Arkın Kobak", "Alp Koçak", "Yasemin Güler Koçar",
                "Benhur Rahman Okur", "Orkun Efe Özdemir", "Selen Öznur", "Mehmet Akın Savaşçı", "Efe Sönmez",
                "Tunay Koluaçık", "Eren Topçu", "İlayda Buzbuz", "Filiznur Demir", "Tarık Ali Dinçel", "Doğa Orhan",
                "Murat Emir Selvi", "Efe Serin", "Cenker Efe Tahan", "Burak Can Yılmaz", "Ege Yılmaz",
                "Matthew Ozan Eanes", "Demir Cücü", "Melih Alperen Kabukçu", "Şerife Şevval Koç", "İdil Toprakkale",
                "Beyazıt Tur", "Yunus Emre Yalçınkaya", "Ege Orhan", "Yasaman Haghshenas", "Sude Teslime Daka",
                "Ali Veli", "Veli Kurnaz", "Kurnaz Ali", "Ali Kurnaz", "Veli Ali"
        };

        ObservableList<String> courses = FXCollections.observableArrayList(
                "SE115",
                "SE302",
                "SE307",
                "SE350",
                "SE360"
        );


        ListView<String> courseListView = new ListView<>(courses);
        ScrollPane scrollPane = new ScrollPane(courseListView);


        courseListView.setOnMouseClicked(event -> {
            if(event.getClickCount()==2) {
                String selectedItem = courseListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {


                    switch (selectedItem) {
                        case "SE115":
                            courseListView.getItems().clear();
                            courseListView.getItems().addAll(studentList);
                            break;
                        case "SE302":
                            courseListView.getItems().clear();
                            courseListView.getItems().addAll(studentList);
                            break;
                        case "SE307":
                            courseListView.getItems().clear();
                            courseListView.getItems().addAll(studentList);
                            break;
                        case "SE350":
                            courseListView.getItems().clear();
                            courseListView.getItems().addAll(studentList);
                            break;
                        case "SE360":
                            courseListView.getItems().clear();
                            courseListView.getItems().addAll(studentList);
                            break;
                    }
                }
            }
        });

        VBox layout = new VBox(new Label("Courses"), scrollPane);
        Scene scene = new Scene(layout, 300, 400);

        courseListStage.setScene(scene);
        courseListStage.show();
    }

    private void showClassList() {
        Stage classListStage = new Stage();
        classListStage.setTitle("Class List");

        ObservableList<String> classes = FXCollections.observableArrayList(
                "C201",
                "C202",
                "C203",
                "C204",
                "C205"
        );

        ListView<String> classListView = new ListView<>(classes);
        ScrollPane scrollPane = new ScrollPane(classListView);

        VBox layout = new VBox(new Label("Classes"), scrollPane);
        Scene scene = new Scene(layout, 300, 400);

        classListStage.setScene(scene);
        classListStage.show();
    }
}
