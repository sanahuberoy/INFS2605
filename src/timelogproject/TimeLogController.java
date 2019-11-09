package timelogproject;

import java.lang.Math;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;

public class TimeLogController implements Initializable {

    private final String URL = "jdbc:sqlite:timelog.db";
    private Connection connection;
    
    // Classes for new category
    @FXML
    private TextField categoryLabel;
    @FXML
    private ColorPicker colorPicker;
    
    // Classes for new entries
    @FXML
    private ComboBox selectCategory;
    @FXML
    private TextArea entryDescription;
    @FXML
    private ComboBox startTimeH;
    @FXML
    private ComboBox startTimeM;
    @FXML
    private ComboBox endTimeH;
    @FXML
    private ComboBox endTimeM;
    @FXML
    private Label DurationLabel;
    
    // Classes for new tasks
    @FXML
    private TextField taskTitle;
    @FXML
    private TextArea taskDescription;
    @FXML
    private Slider taskPriority;
    @FXML
    private DatePicker doDatePicker;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private Label sliderIndicatorLabel;
    
    
    
    public ArrayList<String> getCategories(String query) throws SQLException {
        
        // create new ArrayList of Strings
        ArrayList<String> catList = new ArrayList<>();
        
        Connection connection = DriverManager.getConnection(URL);
        
        //create statement
        Statement st = connection.createStatement();
        
        System.out.println("** get categories from database **");
        ResultSet rs = st.executeQuery(query);
        
        // Iterate over result set from query
        while (rs.next()) {
            
            // Get individual attributes from each row
            String category = rs.getString("label");
            
            // Populate new category with attributes and add to event array
            catList.add(category);
        }

        //close connection 
        st.close();
        connection.close();
        
        // return array of events
        return catList;
    }
    
    private double calculateDuration() {
        int startH   = Integer.parseInt(startTimeH.getValue().toString());
        int endH     = Integer.parseInt(endTimeH.getValue().toString());                    
        int startM   = Integer.parseInt(startTimeM.getValue().toString());
        int endM     = Integer.parseInt(endTimeM.getValue().toString());

        // 13:45 to 15:00
        
        int hours = endH - startH;
        int minutes = endM - startM;

        // hours should be: 0..23
        if (hours < 0) {
            if (endH < startH) {
                hours = 24 + hours;
            } else {
                hours = 0;
                if (startM < endM) {
                    minutes = 0;
                }
            }
        }              

        // mins should be 0..59
        if (minutes < 0) {
            if (hours == 0) {
                minutes = 0;
            } else if (hours > 0) {
                hours = hours - 1;
                minutes = 60 + minutes;
            }
        }
        
        double dur = hours + (minutes / 60.0);
        double rounded = Math.round(dur * 1000.0) / 1000.0;
        
        return rounded;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        try {
            // Connect to timelog database
            connection = DriverManager.getConnection(URL);
        } catch (SQLException ex) {
            Logger.getLogger(TimeLogController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Populate category list with available categories
        String catQuery = "select * from Categories";
        
        try {
            ArrayList<String> catList = getCategories(catQuery);
            selectCategory.setItems(FXCollections.observableArrayList(catList));
            
        } catch (SQLException e) {
            System.out.println("Error fetching categories from database");
            e.printStackTrace();
        }
        
        ArrayList<String> hoursList = new ArrayList<>();
        int i = 0;
        while (i < 24) {
            String h = String.valueOf(i);
            if (i < 10) {
                h = "0" + h;
            }
            hoursList.add(h);
            i++;
        }
        ArrayList<String> minsList = new ArrayList<>();
        int j = 0;
        while (j < 60) {
            String m = String.valueOf(j);
            if (j < 10) {
                m = "0" + m;
            }
            minsList.add(m);
            j++;
        }
        startTimeH.setItems(FXCollections.observableArrayList(hoursList));
        startTimeM.setItems(FXCollections.observableArrayList(minsList));
        endTimeH.setItems(FXCollections.observableArrayList(hoursList));
        endTimeM.setItems(FXCollections.observableArrayList(minsList));

        // update value as slider changes
        taskPriority.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable e) {
                String currentValue = String.valueOf((int) taskPriority.getValue());
                sliderIndicatorLabel.textProperty().setValue(currentValue);
            }
        });
        
        // add automatic calculation of totalduration. given start and endtimes.
        startTimeH.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable x) {
                try {
                    double duration = calculateDuration();
                    DurationLabel.setText(String.valueOf(duration));
                } catch (Exception e) {
                    DurationLabel.setText("0");
                }
            }
        });
        
        startTimeM.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable x) {
                try {
                    double duration = calculateDuration();
                    DurationLabel.setText(String.valueOf(duration));
                } catch (Exception e) {
                    DurationLabel.setText("0");
                }
            }
        });
        
        endTimeH.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable x) {
                try {
                    double duration = calculateDuration();
                    DurationLabel.setText(String.valueOf(duration));
                } catch (Exception e) {
                    DurationLabel.setText("0");
                }
            }
        });
        
        endTimeM.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable x) {
                try {
                    double duration = calculateDuration();
                    DurationLabel.setText(String.valueOf(duration));
                } catch (Exception e) {
                    DurationLabel.setText("0");
                }
            }
        });
    }

    @FXML
    private void saveCategory() {
        try {
            String label = categoryLabel.getText();
            String colorValue = colorPicker.getValue().toString();

            String query = "INSERT INTO categories VALUES("
                    + "'" + label + "'"
                    + ", '" + colorValue + "'"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(query);
            
            // Populate category list with available categories
            String catQuery = "select * from Categories";
            try {
                ArrayList<String> catList = getCategories(catQuery);
                selectCategory.setItems(FXCollections.observableArrayList(catList));

            } catch (SQLException e) {
                System.out.println("Error fetching categories from database");
                e.printStackTrace();
            }
            
            showAlert(AlertType.INFORMATION, "Category created successfully.", ButtonType.OK);
            clearCategory();
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, ex.getMessage(), ButtonType.OK);
        }
    }
    
    @FXML
    private void saveEntry() {
        try {
            String setCategory  = selectCategory.getValue().toString();
            String entryDesc    = entryDescription.getText();
            double duration     = calculateDuration();
            
            String query = "INSERT INTO entries VALUES("
                    + "'" + setCategory + "'"
                    + ", '" + entryDesc + "'"
                    + ", '" + duration + "'"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(query);
            showAlert(AlertType.INFORMATION, "Entry created successfully.", ButtonType.OK);
            clearEntry();
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, ex.getMessage(), ButtonType.OK);
        }
    }
    
    @FXML
    private void saveTask() {
        try {
            String tasktitle    = taskTitle.getText();
            String taskDesc     = taskDescription.getText();
            String dodate       = doDatePicker.getValue().toString();
            String duedate      = dueDatePicker.getValue().toString();
            int taskpriority    = (int) taskPriority.getValue();
            
            System.out.println("About to insert: " + tasktitle + ", " + taskDesc
                                + ", " + dodate + ", " + duedate + ", " +
                                taskpriority);
            
            String query = "INSERT INTO tasks VALUES("
                    + "'" + tasktitle + "'"
                    + ", '" + taskDesc + "'"
                    + ", '" + dodate + "'"
                    + ", '" + duedate + "'"
                    + ", '" + taskpriority + "'"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(query);
            showAlert(AlertType.INFORMATION, "Task created successfully.", ButtonType.OK);
            clearTask();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @FXML
    private void clearTask() {
        taskTitle.setText("");
        taskDescription.setText("");
        doDatePicker.getEditor().clear();
        doDatePicker.setValue(null);
        dueDatePicker.getEditor().clear();
        dueDatePicker.setValue(null);
        taskPriority.setValue(0);
    }
    
    @FXML
    private void clearCategory() {
        categoryLabel.setText("");
        colorPicker.setValue(Color.WHITE);
    }
    
    @FXML
    private void clearEntry() {
        selectCategory.getSelectionModel().select(-1);
        taskTitle.setText("");
        startTimeH.getSelectionModel().select(-1);
        startTimeM.getSelectionModel().select(-1);
        endTimeH.getSelectionModel().select(-1);
        endTimeM.getSelectionModel().select(-1);
        entryDescription.setText("");
    }
    
        
        
    private void showAlert(AlertType alertType, String message, ButtonType buttonType) {
        Alert alert = new Alert(alertType, message, buttonType);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void initializeDatabaseConnection() throws SQLException, ClassNotFoundException {
        connection = DriverManager.getConnection(URL);
    }

}
