import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExpenseTrackerGUI {       //declare various instance variables
    private JFrame frame;             //main window of the application.
    private JTable table;            //table for displaying and managing expenses.
    private JTextField dateField;    //Text fields for entering date.
    private JTextField descField;    //Text fields for entering description.
    private JTextField amountField;   //Text fields for entering amount of expenses.
    private JComboBox<String> typeComboBox;  //selecting the type of expense from expense & income
    private DefaultTableModel tableModel;  //model for the JTable to manage its data.
    private JLabel totalExpenseLabel;  // label for displaying the total expenses.

    private Connection connection;  //database connection to MySQL database.

    public static void main(String[] args) {      //entry point of the GUI which 'invokes' the intilise method
        EventQueue.invokeLater(new Runnable() {   // -connects to database and loads the information.
            public void run() {
                try {
                    ExpenseTrackerGUI window = new ExpenseTrackerGUI();  //object instance is created
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ExpenseTrackerGUI() {   //initilization of for expense tracker takes palce.
        initialize();
        connectToDatabase();
        loadExpenseData();
    }

    private void connectToDatabase() {  //JDBC connetion to MYSQL database.
        try {
            String DB_URL = "jdbc:mysql://localhost:3306/expense_tracker";
            String DB_USER = "root";
            String DB_PASSWORD = "pappu2004";

            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);  //datas to be collected and stored.
        } catch (SQLException e) {
            e.printStackTrace(); //errors to be displayed in console window
        }
    }

    private double calculateTotalExpense() {
        double totalExpense = 0.0;  //to store the current values.it will be keepon changing
        for (int row = 0; row < tableModel.getRowCount(); row++) {  //interacts in row of a table.
            String type = (String) tableModel.getValueAt(row, 3); // gets the value from 4th column when income or expense is stored
            double amount = Double.parseDouble((String) tableModel.getValueAt(row, 2)); // converts string to a double value
            if (type.equals("Expense")) {
                totalExpense += amount; // calculation for expense
            } else if (type.equals("Income")) {
                totalExpense -= amount;  // calculation for income
            }
        }
        return totalExpense;  //total value is returned
    }

    private void insertExpenseData(String date, String description, double amount, String type) { //to insert new in come or expense.
        try {
            String query = "INSERT INTO expenses (date, description, amount, type) VALUES (?, ?, ?, ?)"; //SQL query string i.e insert.
            PreparedStatement preparedStatement = connection.prepareStatement(query); //to create a precompiled SQL statement with placeholders that can be efficiently reused.
            preparedStatement.setString(1, date); //date 1st placeholder.
            preparedStatement.setString(2, description); // description 2nd placeholder.
            preparedStatement.setDouble(3, amount); // amount 3rd placeholder.
            preparedStatement.setString(4, type); // type 4th placeholder.
            preparedStatement.executeUpdate();  //to update the data entered in SQl.
        } catch (SQLException e) { //when an exception occurs catch block will be executed.
            e.printStackTrace(); // to print the exception details in the console.
        }
    }

    private void loadExpenseData() {  // loads the data from database - MYSQL (retrieves).
        tableModel.setRowCount(0);  // clears the jtable.
        try {
            String query = "SELECT * FROM expenses"; //SQL Querry to dispaly data.
            PreparedStatement preparedStatement = connection.prepareStatement(query);//for connection and for safety.
            ResultSet resultSet = preparedStatement.executeQuery(); // executes the SQL querry and contaions the data from 'expense' table

            while (resultSet.next()) { //to iterate through resultset & rest retrieves the value from database.
                String date = resultSet.getString("date");
                String description = resultSet.getString("description");
                double amount = resultSet.getDouble("amount");
                String type = resultSet.getString("type");

                String[] rowData = { date, description, String.format("%.2f", amount), type }; //array to display data.
                tableModel.addRow(rowData); //adds new row based on every input.
            }

            double totalExpense = calculateTotalExpense(); //total expense calculation.
            totalExpenseLabel.setText("Expense: " + String.format("%.2f", totalExpense)); //to display total expense.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        frame = new JFrame(); //JFrame is created.
        frame.setBounds(100, 100, 600, 400); //size of the panel.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close operation i.e closing the GUI window
        frame.getContentPane().setLayout(new BorderLayout(0, 0)); //boder layout with horizontal and vertial gaps.
        frame.setTitle("Expense Tracker"); // title name.

        JPanel inputPanel = new JPanel();   // new panel created.
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH); //panel is placed in the north region.

        JLabel dateLabel = new JLabel("Date:"); // jlabel with date is created.
        inputPanel.add(dateLabel); //date label is added to input panel.

        dateField = new JTextField(10); //data field for user input.
        inputPanel.add(dateField);//date field is added to input panel.

        JLabel descLabel = new JLabel("Description:");
        inputPanel.add(descLabel);

        descField = new JTextField(10);
        inputPanel.add(descField);

        JLabel amountLabel = new JLabel("Amount:");
        inputPanel.add(amountLabel);

        amountField = new JTextField(10);
        inputPanel.add(amountField);

        String[] types = {"Expense", "Income"};
        typeComboBox = new JComboBox<>(types);
        inputPanel.add(typeComboBox);

        JButton addExpenseButton = new JButton("Add Expense/Income");
        inputPanel.add(addExpenseButton);

        tableModel = new DefaultTableModel(); // to manage the data in the JTabel. so new Tablemodel is created.
        tableModel.addColumn("Date");  // column of tabel model.
        tableModel.addColumn("Description");
        tableModel.addColumn("Amount");
        tableModel.addColumn("Type");

        table = new JTable(tableModel); // to display expense data that is retrieved from database.
        JScrollPane scrollPane = new JScrollPane(table); //to make the data scrollable.
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER); // panel in the center position.

        JPanel totalExpensePanel = new JPanel();  // total expense panel.
        totalExpenseLabel = new JLabel("Expense: 0.00"); //displaying the total expense or income.
        totalExpensePanel.add(totalExpenseLabel); //adds expense and income.
        frame.getContentPane().add(totalExpensePanel, BorderLayout.SOUTH); // placed in south region.

        addExpenseButton.addActionListener(new ActionListener() {  //to the Add Expense/Income button.
            public void actionPerformed(ActionEvent e) {  //when clicked an event is performed.
                String date = dateField.getText(); // retrieves and stores it in the date variable as a string.
                String description = descField.getText(); //retrieves and stored in the description variable as a string.
                double amount = Double.parseDouble(amountField.getText()); // data is retrieved and converted to double.
                String type = (String) typeComboBox.getSelectedItem(); // data is retrieved from dropdown and stored in the type variable as a string.

                String[] rowData = { date, description, String.format("%.2f", amount), type }; // data for new row.
                tableModel.addRow(rowData); // updates the expenses and incomes.

                insertExpenseData(date, description, amount, type); //This line calls the insertExpenseData.
                loadExpenseData(); //This line calls the loadExpenseData.

                dateField.setText(""); //to clear the text field after a new data is entered successfully.
                descField.setText("");
                amountField.setText("");
            }
        });

        frame.pack(); //packs the frame, adjusting its size to fit its components properly.
        frame.setVisible(true); // gui to visible state.
    }
}

