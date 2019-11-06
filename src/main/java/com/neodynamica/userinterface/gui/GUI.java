package com.neodynamica.userinterface.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.neodynamica.backendinterface.Backend;
import com.neodynamica.backendinterface.GenerationBean;
import com.neodynamica.backendinterface.InvalidRunStateException;
import com.neodynamica.lib.gp.ErrorFunction;
import com.neodynamica.lib.parameter.SearchParameterException;

public class GUI implements PropertyChangeListener {

    private static final String INPUT_NAME = "";
    private static final String INPUT_DATASET = "";
    private static final String NODES_TEXT_FIELD = "100";
    private static final String DEPTH_TEXT_FIELD = "2";
    private static final String GENERATIONS_TEXT_FIELD = "200";
    private static final String POPULATION_TEXT_FIELD = "200";
    private static final String CURRENT_DIRECTORY_PATH = "./datasets/";
    private static final String SKELETON = "";

    /**
     * Binds GUI.form objects to this class and into the GUI.form as JavaSwing objects
     */
    private JPanel GUIView;

    private JPanel Header;
    private JButton Home;
    private JButton Back;
    private JPanel Content;

    private JPanel GUIHomeView;
    private JButton NewFormulaButton;
    private JButton ListFormulaButton;
    private JButton Quit;

    private JPanel GUINewSolutionAView;
    private JTextField InputDataset;
    private JButton selectDatasetButton;
    private JTextField InputName;
    private JButton Submit;
    private JButton Details;

    private JPanel DetailsView;
    private JTextField NodesTextField;
    private JTextField DepthTextField;
    private JTextField GenerationsTextField;
    private JTextField PopulationTextField;
    private JTextField skeletonTextField;
    private JComboBox<String> ErrorListBox;

    private JPanel GUINewSolutionBView;
    private JPanel ResultsView;
    private JPanel LoadingView;
    private JButton pauseButton;
    private JButton cancelButton1;
    private JPanel RunningPanel;
    private JPanel PausedPanel;
    private JButton resumeButton;
    private JButton cancelButton2;
    private JButton PauseEditButton;
    private JPanel PauseEditPanel;
    private JProgressBar progressBar1;
    private JComboBox<String> PauseErrorListBox;
    private JButton detailResetButton;
    private JTextField PauseNodesTextField;
    private JTextField PausePopulationTextField;
    private JTextField PauseDepthTextField;
    private JTextField PauseGenerationsTextField;
    private JTextField PauseSkeletonTextField;
    private JPanel ResultsPanel;
    private JButton detailsButton;
    private JPanel ResultsDetails;
    private JTextField EquationResult;
    private JTextField ErrorField;
    private JTextArea VerboseOutput;
    private JPanel ErrorPanel;

    private JPanel GUIListSolutionsView;
    private JScrollPane FormulaListScroll;
    private JTable FormulaListTable;
    private JButton Run;
    private JButton Edit;

    private JPanel GUIRunSolutionView;
    private JButton RunFormulaButton;
    private JTextField ResultValue;
    private JButton deleteButton;
    private JButton helpButton;
    private JLabel InputLabel;
    private JPanel InputPanel;
    private JScrollPane InputPanelContainer;
    private JTextField UsingFormula;


    /**
     * Object for managing CardLayout JPanels
     */
    private String currCard;
    private CardLayout cl;
    private CardLayout c2;
    private CardLayout c3;


    private ArrayList<State> history;
    private LinkedHashMap<String, FormulaData> formulaList;
    private FormulaData currSolution;
    private AnswerWorker worker;
    private Backend runner;
    private int stateFlag;
    private boolean isEditingFlag;
    private ArrayList<JTextField> runInputList;

    private GUI() {
        currCard = "Card1";
        cl = (CardLayout) Content.getLayout();
        c2 = (CardLayout) ResultsView.getLayout();
        c3 = (CardLayout) LoadingView.getLayout();
        history = new ArrayList<>();
        formulaList = new LinkedHashMap<>();
        stateFlag = 0;
        isEditingFlag = false;

        /**
         * Header JPanel Buttons
         */
        Home.addActionListener(e -> shiftState("Card1"));
        Back.addActionListener(e -> {
            isEditingFlag = true;
            currCard = history.get(0).getFrame();
            currSolution = history.get(0).getData();
            cl.show(Content, currCard);
            history.remove(0);
        });

        /**
         * Home JPanel Buttons
         */
        NewFormulaButton.addActionListener(e -> {
            newFormula();
            shiftState("Card2");
            try {
                readFile();
            } catch (Exception ignored) {
                //do nothing
            }
        });
        ListFormulaButton.addActionListener(e -> {
            try {
                readFile();
            } catch (Exception ignored) {
                //do nothing
            }
            createFormulaList();
            shiftState("Card4");

        });
        helpButton.addActionListener(e -> {
        });
        Quit.addActionListener(e -> System.exit(0));

        /**
         * NewSolutionA JPanel Buttons
         */
        Submit.addActionListener(e -> {
            boolean flag = false;
            try {
                flag = createFormula();
            } catch (Exception ex) {
                flag = true;
                ex.printStackTrace();
            }
            if (!flag) {
                shiftState("Card3");
                c2.show(ResultsView, "Card1");
                c3.show(LoadingView, "Card1");
                worker = new AnswerWorker();
                worker.execute();
                Header.setVisible(false);
                ResultsDetails.setVisible(false);
                VerboseOutput.setVisible(false);
            }

        });
        Details.addActionListener(e -> DetailsView.setVisible(!DetailsView.isVisible()));
        detailResetButton.addActionListener(e -> {
            NodesTextField.setText(NODES_TEXT_FIELD);
            DepthTextField.setText(DEPTH_TEXT_FIELD);
            GenerationsTextField.setText(GENERATIONS_TEXT_FIELD);
            PopulationTextField.setText(POPULATION_TEXT_FIELD);
            skeletonTextField.setText(SKELETON);
        });
        selectDatasetButton.addActionListener(e -> InputDataset.setText(getDataset(1)));

        /**
         * NewSolutionB JPanel Buttons
         */
        pauseButton.addActionListener(e -> {
            try {
                runner.pause();
            } catch (InvalidRunStateException ex) {
                ex.printStackTrace();
            }
            c3.show(LoadingView, "Card2");
            PauseEditPanel.setVisible(false);
        });
        cancelButton1.addActionListener(e -> {
            try {
                runner.stop();
                worker.cancel(true);
            } catch (InvalidRunStateException ex) {
                ex.printStackTrace();
            }
            Header.setVisible(true);
            c2.show(ResultsView, "Card3");
        });
        resumeButton.addActionListener(e -> {
            try {
                runner.resume();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            c3.show(LoadingView, "Card1");
        });
        cancelButton2.addActionListener(e -> {
            try {
                runner.stop();
            } catch (InvalidRunStateException ex) {
                ex.printStackTrace();
            }

            worker.cancel(true);
            Header.setVisible(true);
            c2.show(ResultsView, "Card3");
        });
        PauseEditButton.addActionListener(e -> {
            PauseEditPanel.setVisible(!PauseEditPanel.isVisible());
            if (resumeButton.isVisible()) {
                editPausedFormula();
                resumeButton.setVisible(false);
                PauseEditButton.setText("Save");
            } else {
                try {
                    writePausedFormula();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                resumeButton.setVisible(true);
                PauseEditButton.setText("Edit");
            }

        });
        detailsButton
                .addActionListener(e -> ResultsDetails.setVisible(!ResultsDetails.isVisible()));

        /**
         * ListSolution JPanel Buttons
         */
        Run.addActionListener(e -> {
            String val = FormulaListTable.getValueAt(FormulaListTable.getSelectedRow(), 0)
                    .toString();
            currSolution = formulaList.get(val);
            UsingFormula.setText(currSolution.getBean().getBestSolutionParenthesesString());
            createInputList();
            shiftState("Card5");
        });
        Edit.addActionListener(e -> {
            editFormula();
            shiftState("Card2");
        });
        deleteButton.addActionListener(e -> {
            String val = FormulaListTable.getValueAt(FormulaListTable.getSelectedRow(), 0)
                    .toString();
            formulaList.remove(val);
            createFormulaList();
            try {
                writeFile();
            } catch (Exception ignored) {
                //do nothing
            }
        });

        /**
         * RunSolution JPanel Buttons
         */
        RunFormulaButton.addActionListener(e -> {
            runFormula();
        });


    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("GUI");
        frame.setContentPane(new GUI().GUIView);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Prepares the NewSolutionA JPanel for a new algorithm
     */
    private void newFormula() {
        InputName.setText(INPUT_NAME);
        InputDataset.setText(INPUT_DATASET);
        NodesTextField.setText(NODES_TEXT_FIELD);
        DepthTextField.setText(DEPTH_TEXT_FIELD);
        GenerationsTextField.setText(GENERATIONS_TEXT_FIELD);
        PopulationTextField.setText(POPULATION_TEXT_FIELD);
        skeletonTextField.setText(SKELETON);
        isEditingFlag = false;
        final String[] supportedErrorFunctions = ErrorFunction.supportedErrors()
                .toArray(new String[0]);
        ErrorListBox.setModel(new DefaultComboBoxModel<>(supportedErrorFunctions));
        DetailsView.setVisible(false);
    }


    /**
     * Error checks and packs the current values of the NewSolutionA JPanel into an object
     *
     * @return true if an error is found with the input values, false otherwise
     */
    private boolean createFormula()
            throws IOException, SearchParameterException, InvalidRunStateException {
        boolean isErrorFlag = false;
        if (InputDataset.getText().equals("") || InputDataset.getText()
                .equals("Please select a dataset")) {
            isErrorFlag = true;
            InputDataset.setText("Please select a dataset");
        }
        if (InputName.getText().equals("")
                || InputName.getText().equals("Please select a name")
                || InputName.getText().equals("Name in use, please use another")
        ) {
            isErrorFlag = true;
            InputName.setText("Please select a name");
        }
        if (formulaList.containsValue(InputName.getText()) && !isEditingFlag) {
            isErrorFlag = true;
            InputName.setText("Name in use, please use another");
        }
        if (!isErrorFlag) {
            FormulaData formula = new FormulaData();
            runner = new Backend();
            formula.setName(InputName.getText());
            formula.setPopulation(Integer.parseInt(PopulationTextField.getText()));
            formula.setGenerations(Integer.parseInt(GenerationsTextField.getText()));
            formula.setNodes(Integer.parseInt(NodesTextField.getText()));
            if (Integer.parseInt(DepthTextField.getText()) >= 30) {
                formula.setDepth(29);
                runner.setInitialSolutionDepth(29);
            } else {
                formula.setDepth(Integer.parseInt(DepthTextField.getText()));
                runner.setInitialSolutionDepth(Integer.parseInt(DepthTextField.getText()));
            }
            formula.setDataset(InputDataset.getText());
            formula.setErrorFunction((String) ErrorListBox.getSelectedItem());
            formula.setSkeleton(skeletonTextField.getText());
            currSolution = formula;
            runner.addPropertyChangeListener(this);
            runner.setPopulationSize(Integer.parseInt(PopulationTextField.getText()));
            runner.setMaxGenerations(Integer.parseInt(GenerationsTextField.getText()));
            runner.setMaxSolutionNodes(Integer.parseInt(NodesTextField.getText()));
            runner.setDataFilePath(InputDataset.getText());
            runner.setErrorFunction((String) ErrorListBox.getSelectedItem());
            runner.setOperators("ADD,SUB,MUL,DIV,SIN,COS,TAN");
            if (!skeletonTextField.getText().equals("")) {
                runner.setSkeleton(skeletonTextField.getText());
            }
        }
        return isErrorFlag;
    }

    /**
     * Prepares the NewSolutionA JPanel to edit a existing algorithm
     */
    private void editFormula() {
        String val = FormulaListTable.getValueAt(FormulaListTable.getSelectedRow(), 0).toString();
        currSolution = formulaList.get(val);
        isEditingFlag = true;
        InputName.setText(currSolution.getName());
        InputDataset.setText(currSolution.getDataset());
        NodesTextField.setText(String.valueOf(currSolution.getNodes()));
        DepthTextField.setText(String.valueOf(currSolution.getDepth()));
        GenerationsTextField.setText(String.valueOf(currSolution.getGenerations()));
        PopulationTextField.setText(String.valueOf(currSolution.getPopulation()));
    }

    /**
     * Prepares the NewSolutionB JPanel to edit a paused algorithm
     */
    private void editPausedFormula() {
        PauseNodesTextField.setText(String.valueOf(currSolution.getNodes()));
        PauseDepthTextField.setText(String.valueOf(currSolution.getDepth()));
        PauseGenerationsTextField.setText(String.valueOf(currSolution.getGenerations()));
        PausePopulationTextField.setText(String.valueOf(currSolution.getPopulation()));
        PauseSkeletonTextField.setText(String.valueOf(currSolution.getSkeleton()));
        final String[] supportedErrorFunctions = ErrorFunction.supportedErrors()
                .toArray(new String[0]);
        PauseErrorListBox.setModel(new DefaultComboBoxModel<>(supportedErrorFunctions));
    }

    /**
     * Writes the data from the NewSolutionB JPanel to edit a paused algorithm
     */
    private void writePausedFormula()
            throws InvalidRunStateException, SearchParameterException, IOException {
        currSolution.setGenerations(Integer.parseInt(PauseGenerationsTextField.getText()));
        currSolution.setNodes(Integer.parseInt(PauseNodesTextField.getText()));
        currSolution.setPopulation(Integer.parseInt(PausePopulationTextField.getText()));
        currSolution.setErrorFunction((String) PauseErrorListBox.getSelectedItem());
        if (!"".equals(skeletonTextField.getText())) {
            currSolution.setSkeleton(PauseSkeletonTextField.getText());
            runner.setSkeleton(currSolution.getSkeleton());
        } else {
            currSolution.setSkeleton("");
        }
        if (Integer.parseInt(PauseDepthTextField.getText()) >= 30) {
            currSolution.setDepth(29);
        } else {
            currSolution.setDepth(Integer.parseInt(PauseDepthTextField.getText()));
        }
        runner.setPopulationSize(currSolution.getPopulation());
        runner.setMaxGenerations(currSolution.getGenerations());
        runner.setMaxSolutionNodes(currSolution.getNodes());
        runner.setDataFilePath(currSolution.getDataset());
        runner.setErrorFunction(currSolution.getErrorFunction());
        progressBar1.setMaximum(currSolution.getGenerations());
    }

    /**
     * Retrieves the filepath of a selected file from JFileChooser
     *
     * @param a Just a placeholder for now
     * @return The filepath of file
     */
    private String getDataset(int a) {
        JFileChooser jfc = new JFileChooser(CURRENT_DIRECTORY_PATH);
        int returnValue = jfc.showOpenDialog(null);
        String outString = "";
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            outString = selectedFile.getAbsolutePath();
        }
        return outString;
    }

    /**
     * Changes the current JPanel from one to another, and saves it to the history
     *
     * @param card The card name to switch to
     */
    private void shiftState(String card) {

        State curr = new State(currCard, currSolution);
        history.add(0, curr);
        //prevents memory leak
        if (history.size() > 100) {
            int j = history.size() - 1;
            history.subList(j - 19, j + 1).clear();
        }
        cl.show(Content, card);
        currCard = card;
    }

    /**
     * Generates a table of formulas for the ListSolutions JPanel
     */
    private void createFormulaList() {
        String[] header = {"Name", "Formula"};
        DefaultTableModel dataModel = new DefaultTableModel(null, header) {
            public int getColumnCount() {
                return 2;
            }
        };
        for (Map.Entry<String, FormulaData> entry : formulaList.entrySet()) {
            String key = entry.getKey();
            FormulaData formulaData = entry.getValue();
            Object[] data = new Object[]{formulaData.getName(),
                    formulaData.getBean().getBestSolutionParenthesesString()};
            dataModel.addRow(data);
        }
        JTable dynaTable = new JTable(dataModel);
        dynaTable.setPreferredScrollableViewportSize(new Dimension(400, 100));
        dynaTable.setColumnSelectionAllowed(false);
        dynaTable.setRowSelectionAllowed(true);
        dynaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        FormulaListTable = dynaTable;
        FormulaListScroll.setViewportView(dynaTable);
    }

    /**
     * Set the appropriate number of input boxes while running a formula
     */
    private void createInputList() {
        String[] labels = currSolution.getBean().getInputVariableLabels();
        int i = labels.length;
        ArrayList<JTextField> inputList = new ArrayList<>();
        ArrayList<JLabel> labelList = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            JTextField textField = new JTextField(8);
            inputList.add(j, textField);
            JLabel label = new JLabel(labels[j]);
            labelList.add(j, label);
        }
        runInputList = inputList;
        InputPanel = new JPanel();
        InputPanel.setPreferredSize(new Dimension(400, 100));
        InputPanelContainer.setViewportView(InputPanel);
        for (int j = 0; j < i; j++) {
            InputPanel.add(labelList.get(j));
            InputPanel.add(runInputList.get(j));
        }
    }

    /**
     * Error checks and passes in input values to run a formula
     */
    private void runFormula() {
        int i = runInputList.size();
        double[] in = new double[i];
        String out = "";
        boolean isErrorFlag = true;
        boolean isMissing = false;
        try {
            for (int j = 0; j < i; j++) {
                if (runInputList.get(j).getText().isEmpty()) {
                    isMissing = true;
                } else {
                    in[j] = Double.parseDouble(runInputList.get(j).getText());
                }
            }
        } catch (NumberFormatException nfe) {
            out = "Invalid input, limit input to numbers only";
            isErrorFlag = false;
        }
        if (isMissing) {
            out = "Missing value, please fill out all boxes";
            isErrorFlag = false;
        }
        if (isErrorFlag) {
            double outNum = 0;
            switch (i) {
                case 1:
                    outNum = currSolution.getBean().predictWithBestSolution(in[0]);
                    break;
                case 2:
                    outNum = currSolution.getBean().predictWithBestSolution(in[0], in[1]);
                    break;
                case 3:
                    outNum = currSolution.getBean().predictWithBestSolution(in[0], in[1], in[2]);
                    break;
                case 4:
                    outNum = currSolution.getBean()
                            .predictWithBestSolution(in[0], in[1], in[2], in[3]);
                    break;
                case 5:
                    outNum = currSolution.getBean()
                            .predictWithBestSolution(in[0], in[1], in[2], in[3], in[4]);
                    break;
                case 6:
                    outNum = currSolution.getBean()
                            .predictWithBestSolution(in[0], in[1], in[2], in[3], in[4], in[5]);
                    break;
                case 7:
                    outNum = currSolution.getBean()
                            .predictWithBestSolution(in[0], in[1], in[2], in[3], in[4], in[5],
                                    in[6]);
                    break;
                case 8:
                    outNum = currSolution.getBean()
                            .predictWithBestSolution(in[0], in[1], in[2], in[3], in[4], in[5],
                                    in[6],
                                    in[7]);
                    break;
                default:
                    break;
            }
            out = Double.toString(outNum);
        }
        ResultValue.setText(out);
    }

    /**
     * Writes the formulaList to a serialized java file
     *
     * @return false if an error occurs, otherwise true
     */
    private boolean writeFile() {
        boolean flag = true;
        try {
            File outputFile = new File("src/main/resources/formulaSaveData.ser");
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(formulaList);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * Reads a serialized java file and applies it to the formulaList object
     *
     * @return false if an error occurs, otherwise true
     */
    private boolean readFile() {
        boolean flag = true;
        InputStream fis = null;
        ObjectInputStream ois = null;
        try {
            URL resource = getClass().getClassLoader().getResource("formulaSaveData.ser");
            fis = resource.openStream();
            ois = new ObjectInputStream(fis);

            formulaList = (LinkedHashMap<String, FormulaData>) ois.readObject();
        } catch (IOException | ClassNotFoundException ioe) {
            flag = false;
            ioe.printStackTrace();
        } finally {
            try {
                assert ois != null;
                ois.close();
                fis.close();
            } catch (IOException e) {
                flag = false;
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * Updates the GUI loading bar
     *
     * @param input GenerationBean holding current backend state
     */
    private void update(GenerationBean input) {
        try {
            int progress = (int) input.getIndex();
            progressBar1.setValue(progress);
        } catch (NullPointerException npe) {
            //do nothing
        }
    }

    /**
     * Modifies GUI display and saves data once backend task ends
     *
     * @param input GenerationBean holding current backend state
     */
    private void endTraining(GenerationBean input) {
        currSolution.setBean(input);
        EquationResult
                .setText(currSolution.getBean().getBestSolutionParenthesesString());
        ErrorField.setText(currSolution.getBean().getBestFitness().toString());
        formulaList.put(currSolution.getName(), currSolution);
        Header.setVisible(true);
        c2.show(ResultsView, "Card2");
        try {
            writeFile();
        } catch (Exception ignored) {
            //do nothing
        }
        stateFlag = 1;
    }

    /**
     * Listens for events passed from the backend
     *
     * @param evt event object broadcasted by the backend
     */
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            GenerationBean newGeneration = runner.getNextGeneration();
            update(newGeneration);
            if (newGeneration.getIndex() >= runner.getMaxGenerations()) {
                endTraining(newGeneration);
            }
        } catch (SearchParameterException | NullPointerException ignored) {
            // do nothing
        }
    }

    /**
     * Performs multi-threading operations for the GUI
     */
    class AnswerWorker extends SwingWorker<FormulaData, Integer> {

        /**
         * Creates a thread to run loading screen
         *
         * @return FormulaData object with completed thread data
         * @throws Exception if the thread is interrupted
         */
        protected FormulaData doInBackground() throws Exception {
            FormulaData formula = currSolution;
            progressBar1.setMinimum(0);
            progressBar1.setMaximum(currSolution.getGenerations());
            runner.start();
            while (stateFlag == 0) {
                Thread.sleep(200);
            }
            Thread.sleep(500);
            stateFlag = 0;
            return formula;
        }

        /**
         * Performs after the thread is complete
         */
        protected void done() {
            try {
                FormulaData placeholder = get();
                if (currSolution.isException()) {
                    runner.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

