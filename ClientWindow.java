/*  
    Name:             Ian Lewis
    Course:           CNT 4714 Summer 2017
    Assignment title: Project 3 – Two-Tier Client-Server Application
    Date:             June 17, 2017
*/

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SpringLayout;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class ClientWindow extends JFrame
{   
    ResultSetTableModel resultTable;
    String selectedDriver;
    String selectedURL;
    
    // Method to create a MySQL Client GUI
    public ClientWindow() throws SQLException
    {
        // Establish the window's name
        super ("MySQL Client");
        
        /*
            Since we only need the default driver and the default url to 
            project3, only the required driver and url's are included.
        */
        String[] driverList = new String[]{"com.mysql.jdbc.Driver"};
        String[] urlList = new String[]{"jdbc:mysql://localhost:3306/project3", 
                                        "jdbc:mysql://localhost:3310/project3"};
        
        /*
            Initialize all GUI elements
        */
        JPanel       clientPanel       = new JPanel();
        JSeparator   divider           = new JSeparator();
        JTextArea    commandField      = new JTextArea(5,30);
        JTextField   userField         = new JTextField(20);
        JTextField   passField         = new JPasswordField(20);
        JComboBox    driverDropDown    = new JComboBox(driverList);
        JComboBox    urlDropDown       = new JComboBox(urlList);
        JTable       resultField       = new JTable();
        JScrollPane  commandPane       = new JScrollPane(commandField);
        JScrollPane  resultPane        = new JScrollPane(resultField);
        JLabel       header            = new JLabel("Enter Database Information");
        JLabel       driverLabel       = new JLabel("JDBC Driver");
        JLabel       urlLabel          = new JLabel("Database URL");
        JLabel       userLabel         = new JLabel("Username");
        JLabel       passLabel         = new JLabel("Password");
        JLabel       connectionLabel   = new JLabel("No Connection Now");
        JLabel       commandLabel      = new JLabel("Enter An SQL Command");
        JLabel       resultLabel       = new JLabel("SQL Execution Result Window");
        JButton      connectButton     = new JButton("Connect to Database");
        JButton      executeButton     = new JButton("Execute SQL Command");
        JButton      clearSQLButton    = new JButton("Clear SQL Command");
        JButton      clearResultButton = new JButton("Clear Result Window");
        SpringLayout clientLayout      = new SpringLayout();
        
        /*
            Get the preffered dimensions for the divider to use based on screen
            size and elemtns.
        */
        Dimension size = divider.getPreferredSize();
        
        /*
            Declare our listeners.
        */
        ActionListener connectListener;
        ActionListener executeListener;
        ActionListener clearCommandListener;
        ActionListener clearResultListener;
        
        /*
            This the function for the "Connect to Database" button.
            it saves the driver and URL for later use and attempts to connect
            to the given url with the username and password provided by the user.
        */
        connectListener = (ActionEvent arg0) ->
        {
            if(!userField.getText().isEmpty())
            {   
                // If there's an active connection, cut it before reconnecting.
                if(resultTable != null && resultTable.connectedToDatabase)
                {
                    resultTable.disconnectFromDatabase();
                    resultTable = null;
                    connectionLabel.setText("Disconnected");
                }
                
                try
                {   
                    // Save the selected driver and database URL for later use.
                    selectedDriver = (String) driverDropDown.getSelectedItem();
                    selectedURL = (String) urlDropDown.getSelectedItem();
                    
                    /*
                        Using Dr. Llewellyn's ResultSetTableModel.java from 
                        module 2, we create a new table model and establish a
                        connection to our database.
                    */
                    resultTable = new ResultSetTableModel(selectedURL, 
                                                          userField.getText(), 
                                                          passField.getText());
                    
                    /*
                        Change the connection label to reflect the current
                        connection status.
                    */
                    connectionLabel.setText("Connected to " + selectedURL 
                                                            + " as " 
                                                            + userField.getText());
                    
                    /*
                        Add our table to our GUI table element and activate the
                        Execute SQL Command button.
                    */
                    resultField.setModel(resultTable);
                    executeButton.setEnabled(true);
                }
                
                /*
                    If there is an error during login, let the user know to
                    check their login credentials.
                */
                catch(SQLException e)
                {
                    JOptionPane.showMessageDialog(clientPanel, "Incorrect Login\n\n"
                            + "The username and password combination was not valid or the server URL was incorrect.");
                }
            }
        };
        
        /*
            This is the function for the Execute SQL Command button. This 
            simply determines if the SQL command is a query or an update to the
            table and sends it to be executed accordingly.
        */
        executeListener = (ActionEvent arg0) -> {
            
            String command = commandField.getText();
            
            try
            {
                if(command.toLowerCase().startsWith("select"))
                {
                    resultTable.setQuery(command);
                }
                
                else
                {
                    resultTable.setUpdate(command);
                }
            }
            
            /*
                If the SQL command contains syntax or spelling errors, the
                command won't be run and the user will be given this dialog box.
            */
            catch (SQLException ex)
            {
                JOptionPane.showMessageDialog(clientPanel, "The command could not be run.\n\n"
               + "Check your command's syntax as well as your user permissions and try again.");
            }
        };
        
        /*
            This is our clear SQL command function. This simply eliminates all 
            text in the command field.
        */
        clearCommandListener = (ActionEvent arg0) ->
        {
            commandField.setText(null);
        };
        
        /*
            This is our Clear Result Window function. When this is pressed, the
            resultTable is emptied and rebuilt.
        */
        clearResultListener = (ActionEvent arg0) ->
        {
            if(resultTable != null)
                resultTable.clearTable();
        };
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        /*
            Add our listeners as well as include an ability to close the 
            program on a window close.
        */
        connectButton.addActionListener(connectListener);
        executeButton.addActionListener(executeListener);
        clearSQLButton.addActionListener(clearCommandListener);
        clearResultButton.addActionListener(clearResultListener);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent event)
            {
                resultTable.disconnectFromDatabase();
                System.exit(0);
            }
        });
        
        // Establish our screen's size and set the divider's dimensions.
        setSize(1100,600);
        size.height = 125;
        divider.setPreferredSize(size);
        
        executeButton.setEnabled(false);
        
        // Add GUI elements to the Panel
        clientPanel.add(header);
        clientPanel.add(driverLabel);
        clientPanel.add(driverDropDown);
        clientPanel.add(urlLabel);
        clientPanel.add(urlDropDown);
        clientPanel.add(userLabel);
        clientPanel.add(userField);
        clientPanel.add(passLabel);
        clientPanel.add(passField);
        clientPanel.add(connectionLabel);
        clientPanel.add(connectButton);
        clientPanel.add(commandLabel);
        clientPanel.add(commandPane);
        clientPanel.add(clearSQLButton);
        clientPanel.add(executeButton);
        clientPanel.add(divider);
        clientPanel.add(resultLabel);
        clientPanel.add(resultPane);
        clientPanel.add(clearResultButton);
        
        // Establish the layout for the client window.
        clientLayout.putConstraint(SpringLayout.NORTH,  header,              10, SpringLayout.NORTH,             clientPanel);
        clientLayout.putConstraint(SpringLayout.WEST,   header,               5, SpringLayout.WEST,              clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  driverLabel,         20, SpringLayout.SOUTH,             header);
        clientLayout.putConstraint(SpringLayout.WEST,   driverLabel,          5, SpringLayout.WEST,              clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  driverDropDown,      15, SpringLayout.SOUTH,             header);
        clientLayout.putConstraint(SpringLayout.WEST,   driverDropDown,       0, SpringLayout.WEST,              userField);
        clientLayout.putConstraint(SpringLayout.EAST,   driverDropDown,     -10, SpringLayout.HORIZONTAL_CENTER, clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  urlLabel,            18, SpringLayout.SOUTH,             driverLabel);
        clientLayout.putConstraint(SpringLayout.WEST,   urlLabel,             5, SpringLayout.WEST,              clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  urlDropDown,         10, SpringLayout.SOUTH,             driverDropDown);
        clientLayout.putConstraint(SpringLayout.WEST,   urlDropDown,          0, SpringLayout.WEST,              userField);
        clientLayout.putConstraint(SpringLayout.EAST,   urlDropDown,        -10, SpringLayout.HORIZONTAL_CENTER, clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  userLabel,           20, SpringLayout.SOUTH,             urlLabel);
        clientLayout.putConstraint(SpringLayout.WEST,   userLabel,            5, SpringLayout.WEST,              clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  userField,           15, SpringLayout.SOUTH,             urlDropDown);
        clientLayout.putConstraint(SpringLayout.WEST,   userField,           30, SpringLayout.EAST,              userLabel);
        clientLayout.putConstraint(SpringLayout.EAST,   userField,          -10, SpringLayout.HORIZONTAL_CENTER, clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  passLabel,           15, SpringLayout.SOUTH,             userLabel);
        clientLayout.putConstraint(SpringLayout.WEST,   passLabel,            5, SpringLayout.WEST,              clientPanel);
        
        clientLayout.putConstraint(SpringLayout.WEST,   passField,            0, SpringLayout.WEST,              userField);
        clientLayout.putConstraint(SpringLayout.NORTH,  passField,           10, SpringLayout.SOUTH,             userField);
        clientLayout.putConstraint(SpringLayout.EAST,   passField,          -10, SpringLayout.HORIZONTAL_CENTER, clientPanel);
        
        clientLayout.putConstraint(SpringLayout.SOUTH,  connectionLabel,   -75, SpringLayout.VERTICAL_CENTER,    clientPanel);
        clientLayout.putConstraint(SpringLayout.WEST,   connectionLabel,    11, SpringLayout.WEST,               clientPanel);
        
        clientLayout.putConstraint(SpringLayout.SOUTH,  connectButton,     -70, SpringLayout.VERTICAL_CENTER,    clientPanel);
        clientLayout.putConstraint(SpringLayout.EAST,   connectButton,     -10, SpringLayout.HORIZONTAL_CENTER,  clientPanel);
        
        clientLayout.putConstraint(SpringLayout.WEST,   commandLabel,      160, SpringLayout.EAST, header);
        clientLayout.putConstraint(SpringLayout.NORTH,  commandLabel,       10, SpringLayout.NORTH,              clientPanel);
        clientLayout.putConstraint(SpringLayout.WEST,   commandLabel,       10, SpringLayout.HORIZONTAL_CENTER,  clientPanel);
        
        clientLayout.putConstraint(SpringLayout.WEST,   commandPane,        10, SpringLayout.HORIZONTAL_CENTER,  clientPanel);
        clientLayout.putConstraint(SpringLayout.NORTH,  commandPane,        10, SpringLayout.SOUTH,              commandLabel);
        clientLayout.putConstraint(SpringLayout.EAST,   commandPane,       -10, SpringLayout.EAST,               clientPanel);
        clientLayout.putConstraint(SpringLayout.SOUTH,  commandPane,       -10, SpringLayout.NORTH,              executeButton);
        
        clientLayout.putConstraint(SpringLayout.EAST,   executeButton,     -10, SpringLayout.EAST,               clientPanel);
        clientLayout.putConstraint(SpringLayout.SOUTH,  executeButton,     -70, SpringLayout.VERTICAL_CENTER,    clientPanel);
        
        clientLayout.putConstraint(SpringLayout.WEST,   clearSQLButton,      0, SpringLayout.WEST,               commandPane);
        clientLayout.putConstraint(SpringLayout.SOUTH,  clearSQLButton,    -70, SpringLayout.VERTICAL_CENTER,    clientPanel);
        
        clientLayout.putConstraint(SpringLayout.EAST,               divider, 0, SpringLayout.EAST,              clientPanel);
        clientLayout.putConstraint(SpringLayout.WEST,               divider, 0, SpringLayout.WEST,              clientPanel);
        clientLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER,  divider, 0, SpringLayout.HORIZONTAL_CENTER, clientPanel);
        clientLayout.putConstraint(SpringLayout.VERTICAL_CENTER,    divider, 0, SpringLayout.VERTICAL_CENTER,   clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH,  resultPane,          5, SpringLayout.SOUTH,              resultLabel);
        clientLayout.putConstraint(SpringLayout.WEST,   resultPane,         10, SpringLayout.WEST,               clientPanel);
        clientLayout.putConstraint(SpringLayout.SOUTH,  resultPane,         -5, SpringLayout.NORTH,              clearResultButton);
        clientLayout.putConstraint(SpringLayout.EAST,   resultPane,        -10, SpringLayout.EAST,               clientPanel);
        
        clientLayout.putConstraint(SpringLayout.NORTH, resultLabel,        -58, SpringLayout.VERTICAL_CENTER,    clientPanel);
        clientLayout.putConstraint(SpringLayout.WEST,  resultLabel,          5, SpringLayout.WEST,               clientPanel);
        
        clientLayout.putConstraint(SpringLayout.SOUTH, clearResultButton,  -10, SpringLayout.SOUTH,              clientPanel);
        clientLayout.putConstraint(SpringLayout.WEST,  clearResultButton,   10, SpringLayout.WEST,               clientPanel);
        
        /*
            Add the layout to the panel, add the panel to frame then set the
            window to be visible.
        */
        clientPanel.setLayout(clientLayout);
        add(clientPanel);
        setVisible(true);
    }
    
    public static void main(String args[]) throws SQLException
    {
        // Call our construction of the client Window.
        new ClientWindow();
    }
}