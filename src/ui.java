import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import static javax.lang.model.SourceVersion.*;

public class ui extends JFrame{

    public static boolean check = false;
    String s1;

    String s2;

    public static int numKeys;

    public static int beforeFlag = 0;

    FileWriter output = new FileWriter("TestCodeTester.java");

    //Name of the class being tested
    public static String nameOfClassBeingTested;

    //Name of current function being parsed. Helps to associate the lines being read in the function body with the function
    public static String currFunction;

    //The absolute filepath taken as input from the user via the Java Swing GUI
    public static String inputFilePath;

    //Stores the external objects being referenced in a class which would be mocked
    public static Vector<String> externalObjectList = new Vector<>();

    //Stores the functions to be used in @Test
    public static Vector<String> functionsToBeTested = new Vector<>();

    public static Vector<String> autowiredObjectList = new Vector<>();

    //Hashmap storing information about the public functions being declared in a class, what external objects these functions use and what functions fo these external objects call
    public static HashMap<String, HashMap<String, List<String>>> functionData = new LinkedHashMap();

    //Hashmap to store what dummy value will be returned for the mocked functions

    public static HashMap<String, Vector<String>> whenReturnThisFunctions = new LinkedHashMap();

    public static Vector<String> beforeData = new Vector<>();

    public static String t1;

    public static String t2;

    public static String t3;

    public static String t4;

    //Flag for registering that previous line had an @Autowired object
    public static int autowiredFlag = 0;
    private JPanel panel1;
    private JTextField textField1;
    private JLabel l1;
    private JTable table1;
    private JTextField textField2;
    private JTextField textField3;
    private JButton chooseFilePathButton;
    private JLabel Before;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JButton generateBeforeButton;
    private JButton generateTestButton;
    private JTextField textField8;
    private JTextField textField9;
    private JLabel tableTitle;
    private JLabel test;

    DefaultTableModel model;

    ui() throws IOException {
        add(panel1);
        setSize(1400,1000);

        chooseFilePathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputFilePath=textField1.getText();
                try {
                    readUsingFileReader(inputFilePath);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        generateBeforeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    generateBefore();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        generateTestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    generateTest();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    //Function that parses the code to be tested
    public void readUsingFileReader(String filePath) throws IOException {

        File file = new File(filePath);
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        //Reads the file line by line
        BufferedReader br = new BufferedReader(fr);
        //Stores the current line of code that was read
        String line;

        //Creates file that will store the unit test code corresponding to the code to be tested
        File file1 = new File("TestCodeTester.java");

        //To write into the unit test code file
        //FileWriter output = new FileWriter("TestCodeTester.java");

        try {
            // create a new file with name specified
            // by the file object
            boolean value = file.createNewFile();
            if (value) {
                System.out.println("New Java File is created.");
            } else {
                System.out.println("The file already exists.");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        while (true) {
            try {
                if (!((line = br.readLine()) != null)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //This is where the actual parsing happens
            //Every line of code will be parsed to identify the tokens present in it

            //Line read was an import statement
            //Copy the import to the unit test file
            if (line.contains("import")) {
                //System.out.println("Hi in imp");
                output.write(line);
                output.write("\n");
            }

            //Detects the class which is being tested
            else if (line.contains("public class")) {
                //System.out.println("Hi in pc");
                //Importing the additional dependencies here (as to be imported only once)
                output.write("import static org.junit.Assert.assertEquals;\n");
                output.write("import static org.junit.Assert.assertFalse;\n");
                output.write("import static org.junit.Assert.assertNotNull;\n");
                output.write("import static org.junit.Assert.assertTrue;\n");
                output.write("import org.junit.Before;\n");
                output.write("import org.junit.Rule;\n");
                output.write("import org.junit.Test;\n");
                output.write("import org.junit.mockito2.*;\n");
                output.write("import static org.mockito2.Mockito.mock;\n");
                output.write("import static org.mockito2.Mockito.spy;\n");
                output.write("import static org.mockito2.Mockito.when;\n");
                output.write("import static org.mockito2.Mockito.doNothing;\n");
                output.write("\n");

                //Declares the public class which will hold the mocks and tests in the unit test code file
                output.write("public class TestCodeTester{\n\n");

                //@InjectMocks creates class instances which need to be tested in the test class
                output.write("@InjectMocks\n");

                //Logic to extract class name (Assumes starts with public class)
                int i = line.indexOf("public class") + 13;
                String temp = "";
                while (i < line.length()) {
                    if (line.charAt(i) == '{' || line.charAt(i) == ' ') {
                        break;
                    }
                    temp += line.charAt(i);
                    i++;
                }
                nameOfClassBeingTested = temp;
                output.write(temp + " " + temp.toLowerCase() + ";\n\n");
            }
            //Public function has been detected in the line
            else if (line.contains("throws") && line.contains("public")) {

                //System.out.println("Hi in throw");
                //Logic to extract function name
                int index = line.indexOf("public") + 7;
                int flag = 0;

                while (flag != 1) {
                    if (line.charAt(index) == ' ') {
                        flag = 1;
                    }
                    index++;
                }

                String functionName = line.substring(index, line.indexOf(')') + 1);
                HashMap<String, List<String>> temp = new LinkedHashMap<>();
                functionData.put(functionName, temp);
                //System.out.println(functionName);

                //To extract the parameters of the function
                int startIndex = line.indexOf('(') + 1;
                int endIndex = line.indexOf(')');

                if (startIndex != endIndex) {
                    List<String> parameters = Arrays.asList(line.substring(startIndex, endIndex).split("[ ,]"));
                    //System.out.println(parameters);
                    Vector<String> objectsReferencedInThisFunction = new Vector<>();
                    for (int i = 0; i < parameters.size(); i += 2) {
                        if (!isKeyword(parameters.get(i)) && parameters.get(i) != nameOfClassBeingTested && !parameters.contains("String")){
                            objectsReferencedInThisFunction.add(parameters.get(i + 1));
                            if (!externalObjectList.contains(parameters.get(i)))
                                externalObjectList.add(parameters.get(i));
                        }
                    }
                    //Updates the hashmap
                    for (int i = 0; i < objectsReferencedInThisFunction.size(); i++) {
                        List<String> temp1 = new ArrayList<>();
                        functionData.get(functionName).put(objectsReferencedInThisFunction.get(i), temp1);
                    }
                }
                currFunction = functionName;
                functionsToBeTested.add(functionName);

            } else if (line.contains("@Autowired")) {
                //System.out.println("In autowire");
                autowiredFlag = 1;
            } else if (autowiredFlag == 1) {
                //System.out.println("In autowire body");
                String temp = line.substring(line.indexOf("private") + 8, line.indexOf(";")); //Abc abc
                String objName = temp.substring(temp.indexOf(" ") + 1);
                autowiredObjectList.add(objName);
                autowiredFlag = 0;
            } else {
                for (int i = 0; i < autowiredObjectList.size(); i++) {
                    if (line.contains(autowiredObjectList.get(i) + ".")) {
                        //int startIndex = line.indexOf(autowiredObjectList.get(i)) + autowiredObjectList.get(i).length();
                        int startIndex=line.indexOf(autowiredObjectList.get(i));
                        int endIndex=startIndex+1;
                        while(endIndex<line.length()){
                            if(line.charAt(endIndex)=='('){
                                endIndex++;
                                break;
                            }
                            endIndex++;
                        }

                        if(!functionData.get(currFunction).containsKey(autowiredObjectList.get(i))){
                            List<String> temp = new ArrayList<>();
                            temp.add(line.substring(startIndex,endIndex)+")");
                            functionData.get(currFunction).put(autowiredObjectList.get(i),temp);
                        }
                        else{
                            functionData.get(currFunction).get(autowiredObjectList.get(i)).add(line.substring(startIndex, endIndex) + ")");
                        }

                        line = line.trim();
                        List<String> temp1 = Arrays.asList(line.split(" "));
                        String startWord = temp1.get(0);
                        //Not returning a value
                        if (startWord.contains(".")) {
                            continue;
                        }
                        break;
                    }
                }
            }
            //System.out.println(whenReturnThisFunctions);
            numKeys=functionData.size();
        }

        //Generating @Mocks in the file
        for (int i = 0; i < externalObjectList.size(); i++) {
            output.write("@Mock\n");
            output.write(externalObjectList.get(i) + " mock" + externalObjectList.get(i) + ";\n\n");
        }

        for (int i = 0; i < autowiredObjectList.size(); i++) {
            output.write("@Mock\n");
            output.write(autowiredObjectList.get(i) + " mock" + autowiredObjectList.get(i) + ";\n\n");
        }

        model=new DefaultTableModel();
        Object[] column={"Public functions being called","External objects being referenced","Functions being called by external objects"};
        final Object[] row=new Object [4];
        model.setColumnIdentifiers(column);
        table1.setModel(model);

        for(Map.Entry<String, HashMap<String, List<String>>> entry : functionData.entrySet()) {
        row[0]=entry.getKey();
        int flag = 0;
        for (Map.Entry<String, List<String>> entry2 : entry.getValue().entrySet()) {
            if(flag==0){
                flag=1;
                row[1]=entry2.getKey();
                row[2]=entry2.getValue();
                model.addRow(row);
                continue;
            }
            else{
                row[0]="";
                row[1]=entry2.getKey();
                row[2]=entry2.getValue();
                model.addRow(row);
            }
        }
    }

        output.write("@Before\n");
        output.write("public void beforeTest(){\n\n");
        output.write("MockitoAnnotations.initMocks(this)\n");
        br.close();
        fr.close();
}

    public void generateBefore() throws IOException {
        t1=textField8.getText();
        t2=textField9.getText();
        t3=textField2.getText();
        t4=textField3.getText();

        beforeData.add(t1+"."+t2);
        System.out.println(beforeData);

        output.write("when("+t1+"."+t2+"("+t3+").thenReturn("+t4+");\n");
    }

    public void generateTest() throws IOException {

        if(beforeFlag==0){
            beforeFlag=1;
            output.write("}\n\n");
        }

        for(Map.Entry<String,HashMap<String,List<String>>> entry:functionData.entrySet()){
            s1=entry.getKey();
            for(Map.Entry<String,List<String>> entry2:entry.getValue().entrySet()) {
                s2=entry2.getKey();
                String testString = functionData.get(s1).get(s2).get(0);
                if (beforeData.contains(testString)) {
                    functionData.get(s1).get(s2).remove(0);
                    if (functionData.get(s1).get(s2).size() == 0) {
                        functionData.get(s1).remove(s2);
                    }
                    return;
                }
                break;
            }
            break;
        }

        if(numKeys>functionData.size() || !check){
            for(Map.Entry<String, HashMap<String,List<String>>> entry:functionData.entrySet()){
                s1=entry.getKey();
                output.write("@Test\n");
                output.write("public void " + "test" + s1 + "{\n\n");
                check=true;
                numKeys=functionData.size();
                break;
            }
        }

        t1=textField4.getText();
        t2=textField5.getText();
        t3=textField6.getText();
        t4=textField7.getText();

        //l3.setText(whenReturnThisFunctions.get(s1).get(0));
        String temp=functionData.get(s1).get(s2).get(0);
        output.write("when("+temp.substring(0,temp.indexOf("(")+1)+t1+")).thenReturn("+t2+");\n");

        if(functionData.get(s1).size()==1 && functionData.get(s1).get(s2).size()==1){
            output.write("Assert.assertEquals("+nameOfClassBeingTested+"."+s1.substring(0,s1.indexOf("("))+"("+t3+"),"+t4+");\n");
            output.write("}\n");
            functionData.remove(s1);
        }
        else if(functionData.get(s1).get(s2).size()==1){
            functionData.get(s1).remove(s2);
            if(functionData.get(s1).size()==0){
                functionData.remove(s1);
            }
        }
        else{
            functionData.get(s1).get(s2).remove(0);
        }

        if(functionData.size()==0){
            output.write("}");
            output.close();
        }
    }

}
