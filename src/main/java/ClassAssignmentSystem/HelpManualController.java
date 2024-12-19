package ClassAssignmentSystem;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class HelpManualController {

    @FXML
    private TextArea txtHelpContent;

    // Initialize method to set the help text
    @FXML
    public void initialize() {
        String helpText = """
              Welcome to Manual!
        
        This manual is designed to provide a detailed introduction to the application's interfaces and to explain the steps you need to follow to achieve your objectives while using these interfaces. You can find information on how to use all the application's functions within this document.
        
        ---
        
              Introduction to the Interfaces
        
               MainGUI
        
        When you load and open the application, the first interface you encounter is the   MainGUI  . This interface not only hosts many of the application's core functions but also allows you to access other interfaces that contain additional functionalities.
        
        -   Top Section:  
          This part is responsible for creating the necessary database by importing data from CSV files. It includes the following buttons:
          -   Select Courses CSV
          -   Select Classroom CSV
          -   Import Data
          -   Delete Data
          
          Additionally, there is a   Help   button on the opposite side of the screen that provides access to this manual.
        
        -   Middle Section: 
          Consisting of six buttons arranged side by side, this section becomes active once the database is properly set up. The buttons are:
          1.   List Courses:   Creates a Courses List that displays all course data in the database.
          2.   List Classrooms:   Creates a Classrooms List that displays all classroom data in the database.
          3.   List Students:   Accesses the StudentList interface, displaying all student data in the database.
          4.   Assign Courses:   Assigns courses to classrooms based on availability and capacity.
          5.   Change Classroom:   Allows you to change the classroom assigned to a course.
          6.   Create Course/Meeting:   Redirects you to the Create Course interface to add a new course.
        
        -   Bottom Section:  
          This area contains three columns of lists:
          
          1.   Left Column - Courses List:
             After creating the database and clicking the   List Courses   button, the Courses List appears here. It includes all registered courses, and any newly created course is automatically added to the list. All courses in this list are selectable. When a course is selected, detailed information about that course is displayed below the list. Additionally, there is an   Add Student   button at the bottom left, allowing you to add new students to the selected course. Detailed information about adding students is provided later in this manual.
          
          2.   Middle Column - Students List: 
             This list appears when you select a course from the Courses List. It displays the names of all students enrolled in the selected course. Double-clicking a student's name opens their weekly class schedule. Each time you change the selected course, the students list updates to show the enrolled students for the new course. At the bottom of this column, there is a   Delete Student   button that allows you to remove a student from the selected course. Detailed instructions for deleting students are provided later in this manual.
          
          3.   Right Column - Classrooms List: 
             Displays all classrooms registered in the system. To activate this list, click the   List Classrooms   button. Double-clicking a classroom shows a weekly schedule of courses and their respective times in that classroom. Additionally, there is a section below the list that displays the capacity of the selected classroom.
        
               Create CourseGUI
        
        This interface is designed to add new courses to the system. To access it, click the   Create Course/Meeting   button in the MainGUI. Upon clicking, the interface opens, allowing you to specify all necessary attributes for the new course.
        
        -     Course Details: 
          -   ID and Instructor Name:   Two text fields where you enter the course ID and the instructor's name.
          -   Assign Students:   A button that redirects you to the  StudentListGUI   to select and assign students to the course.
          -   Schedule:   Choice boxes for selecting the course's start day, start time, and duration. The available options in each choice box are constrained based on the students' common free times to ensure the course can be scheduled without conflicts.
          -   Assign Classroom:   After setting the schedule, click the   Assign Class   button to allocate an available classroom that meets the capacity and scheduling requirements.
          -   Finalize:   Once all details are set, click the   Done   button to save the course to the database. The new course will appear in the Courses List (when you reclick the List Courses button), the students' schedules, and the classroom's weekly schedule.
        
               StudentListGUI
        
        When you access this interface, you'll see a list of student names, each accompanied by a checkbox. Similar to other interfaces, double-clicking a student's name will display their class schedule.
        
        -   Checkboxes:   Used to select one or more students.
        -   Buttons:   Located below the student list, there are four buttons whose availability depends on the context in which the interface is accessed. The interface serves three primary purposes:
        
          1.   Viewing All Students:
             -   Accessed By:   Clicking the   List Students   button in the MainGUI.
             -   Purpose:   View all students and find common free times among selected students.
             -   Active Buttons:   "Find Available Time Slots", "Clear Choices", "Done". The "Add" button is inactive.
          
          2.   Assigning Students to a New Course:
             -   Accessed By:   Clicking the   Assign Students   button in the Create CourseGUI.
             -   Purpose:   Select students to add to the new course.
             -   Active Buttons:   "Done", "Clear Choices", "Find Available Time Slots". The "Add" button remains inactive.
          
          3.   Adding Students to an Existing Course:
             -   Accessed By:   Clicking the   Add Student   button in the MainGUI.
             -   Purpose:   Add students to an existing course, ensuring no duplicate enrollments.
             -   Active Buttons:   "Add", "Clear Choices", "Find Available Time Slots". The "Done" button is inactive.
        
        ---
        
               Using CSV Files to Load Required Data
        
        To load the necessary information from CSV files, follow these steps:
        
        1.   Locate Buttons in MainGUI:   
           In the top-left section of the MainGUI, use the following buttons:
           -   Select Courses CSV
           -   Select Classroom CSV
        
        2.   Select CSV Files:
           Clicking each button will open your computer's file explorer. Select the appropriately formatted CSV files for courses and classrooms.
        
        3.   Import Data:  
           After selecting the CSV files, the buttons will display the chosen file names next to "Courses CSV:" and "Classrooms CSV:". If you need to reselect files, click the respective buttons again and repeat the selection process.
        
        4.   Process Data: 
           Once the correct CSV files are selected, click the Import Data button. This will process and import the data into the database in a format that the system can use.
        
        5.   Resetting the System: 
           To reset the system or work with different CSV files, click the Delete Data button. This will clear the current database, allowing you to start the import process anew. If you encounter errors due to incorrectly formatted CSV files, use the   Delete Data   button to reset and then select the correct files.
        
        ---
        
               Assigning Courses to Classrooms
        
        To assign courses to classrooms within the system:
        
        1.   Activate Lists: 
           Click the   List Courses   and   List Classrooms   buttons in the MainGUI to activate the respective lists.
        
        2.   Assign Courses:  
           Click the Assign Courses button. The system will assign courses to classrooms based on availability, schedule, and classroom capacity.
        
        ---
        
               Changing a Course’s Assigned Classroom
        
        To change the classroom assigned to a specific course:
        
        1.   Prepare the Lists:  
           Ensure that both the Courses and Classrooms lists are active by clicking List Courses and List Classrooms.
        
        2.   Assign Courses:  
           Click the Assign Courses button to ensure courses are properly assigned before making changes.
        
        3.   Select the Course:  
           From the Courses List, select the course whose classroom you wish to change.
        
        4.   Initiate Change: 
           Click the Change Classroom button in the MainGUI. A mini-interface will appear.
        
        5.   Select New Classroom: 
           -   Choice Box:   Located next to "Select a new Classroom:", this box lists classrooms that can accommodate the course based on student count and availability.
           -   Confirm Change:   Select the desired classroom and click the OK button to finalize the change. The system will save the new assignment.
           -   Cancel:   If you decide not to change the classroom, click the Cancel button to exit without making changes.
        
        ---
        
               Adding a New Student to a Course
        
        To add a new student to an existing course:
        
        1.   Prepare the Lists:
           Ensure that both the Courses and Classrooms lists are active by clicking List Courses and List Classrooms.
        
        2.   Assign Courses:  
           Click the Assign Courses button to ensure courses are properly assigned to classrooms.
        
        3.   Select the Course:  
           From the Courses List at the bottom left of the MainGUI, select the course to which you want to add a student. This action activates the   Add Student   button at the bottom left.
        
        4.   Add Student:
           -   Click Add Student: If the course is not full (i.e., the number of students is below the classroom's capacity), clicking this button will open the StudentListGUI.
           -   Select Students: In the StudentListGUI, select students who are not already enrolled in the course by checking the corresponding boxes.
           -   Confirm Selection: Click the Add button. You will receive a confirmation prompt asking if you are sure about your selections.
             -   Cancel: If you are unsure, click   Cancel   to abort the process.
             -   OK: If you confirm, the system will attempt to add each selected student:
               -   No Schedule Conflict: The student is added to the course.
               -   Schedule Conflict: If a student has a conflicting schedule, you will receive a conflict warning, and the student will not be added.
           -   Completion:   After all selections are processed, you will receive a final notification confirming the completion of the addition process.
        
        ---
        
               Removing a Student from a Course
        
        To remove a student from an assigned course:
        
        1.   Prepare the Lists:  
           Ensure that both the Courses and Classrooms lists are active by clicking List Courses and List Classrooms buttons.
        
        2.   Assign Courses:  
           Click the Assign Courses button to ensure courses are properly assigned to classrooms.
        
        3.   Select the Course: 
           From the Courses List at the bottom left of the MainGUI, select the course from which you want to remove a student. This will activate the Students List in the middle column.
        
        4.   Select the Student: 
           -   Double-Click Student Name:   In the Students List, double-click the name of the student you wish to remove. This action will activate the Delete Selected Student button at the bottom of the column.
        
        5.   Delete Student:  
           -   Click Delete Selected Student:   A confirmation prompt will appear asking if you are sure you want to delete the selected student.
             -   Cancel:   Click   Cancel   to abort the deletion.
             -   OK:   Click   OK   to confirm. The student will be removed from the course, decreasing the course's enrollment count by one. The student's schedule will no longer reflect the removed course.
        
        ---
        
        This manual should provide you with all the necessary information to effectively use the application's interfaces and functionalities. If you encounter any issues or require further assistance, please refer to the   Help   section within the MainGUI.
        """;




        // Set the help text to the TextArea
        txtHelpContent.setText(helpText);
    }
}
