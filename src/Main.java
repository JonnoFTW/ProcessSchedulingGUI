import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import net.miginfocom.swing.MigLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultCaret;

// This is a class
class Main  extends JFrame{
    /**
     * 
     */
    private static final long serialVersionUID = -4166466089214148491L;
    // This is the time to run each process for
    private JPanel simulation;
    private JTextArea textArea;
    private LinkedList<Proc> processList = new LinkedList<Proc>();
    private SpinnerNumberModel processModel, quantumModel,newProcProbModel,newProcsModel,delayModel;
    private Worker worker;
    private boolean isStarted = false;
    private JComboBox algorithmSelect;
    private JToggleButton tglbtnStart;
    private int pid = 0;
    
    Main(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setTitle("Process Scheduling");
        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));
        
        JPanel setup = new JPanel();
        panel.setPreferredSize(new Dimension(800,500));
        setup.setPreferredSize(new Dimension(275,500));
        panel.add(setup, BorderLayout.EAST);
        setup.setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][][][][][][][grow]"));
        
        JLabel lblSetup = new JLabel("Setup");
        setup.add(lblSetup, "cell 0 0");
        
        JButton btnGenereateProcesses = new JButton("Reset");
        btnGenereateProcesses.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupProcesses();
            }
        });
        setup.add(btnGenereateProcesses, "cell 0 1,growx");
        
        tglbtnStart = new JToggleButton("Start");
        tglbtnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(tglbtnStart.isSelected()) {
                    if(!isStarted) {
                        worker = new Worker();
                        worker.execute();
                        tglbtnStart.setText("Stop");
                    }
                } else {
                    worker.cancel(true);
                    tglbtnStart.setText("Start");
                }
            }
        });
        setup.add(tglbtnStart, "cell 0 2,growx");
        
        JLabel lblQuantum = new JLabel("Quantum (ms)");
        setup.add(lblQuantum, "cell 0 3");
        
        quantumModel = new SpinnerNumberModel(250, 1, 1000, 10);
        JSpinner quantumSpinner = new JSpinner(quantumModel);
        setup.add(quantumSpinner, "cell 0 4,growx");
        
        JLabel lblAlgorithm = new JLabel("Algorithm");
        setup.add(lblAlgorithm, "cell 0 5");
        
        String[] algStrings = { "Round Robin",  "FIFO", "SJF", };
        algorithmSelect = new JComboBox(algStrings);
        setup.add(algorithmSelect, "cell 0 6,growx");
        
        
        JLabel lblProcesses = new JLabel("Initial Processes");
        setup.add(lblProcesses, "cell 0 7");
        
        processModel = new SpinnerNumberModel(10, 1, 100, 1);
        JSpinner processCount = new JSpinner(processModel);
        processCount.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent arg0) {
                setupProcesses();
            }
        });
        setup.add(processCount, "cell 0 8,growx");
        
        JLabel lblProbabilityOfNew = new JLabel("Probability of New Process");
        setup.add(lblProbabilityOfNew, "cell 0 9");
        
        newProcProbModel = new SpinnerNumberModel(25,0,100,1);
        JSpinner newProcProb = new JSpinner(newProcProbModel);
        setup.add(newProcProb, "cell 0 10,growx");
        
        
        JLabel lblMaxNumberOf = new JLabel("Max Number of New Processes");
        setup.add(lblMaxNumberOf, "cell 0 11");
        
        newProcsModel = new SpinnerNumberModel(2,0,100,1);
        JSpinner newProcs = new JSpinner();
        setup.add(newProcs, "cell 0 12,growx");
        
        JLabel lblSimulationDelay = new JLabel("Simulation Delay");
        setup.add(lblSimulationDelay, "cell 0 13");
        
        delayModel = new SpinnerNumberModel(5,1,20,1);
        JSpinner delay = new JSpinner(delayModel);
        setup.add(delay, "cell 0 14,growx");
        
        JScrollPane scrollPane = new JScrollPane();
        setup.add(scrollPane, "cell 0 15,grow");
        
        textArea = new JTextArea();
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane.setViewportView(textArea);
        
        simulation = new JPanel();
        panel.add(simulation, BorderLayout.CENTER);
        simulation.setLayout(new GridLayout(0, 5, 4, 4));

        setupProcesses();
            
            
        }; 
        /**
         * A SwingWorker that runs the simulation
         *
         */
        private class Worker extends SwingWorker<Void, String> {

            @Override
            protected Void doInBackground() throws Exception {
                switch (algorithmSelect.getSelectedIndex()) {
                case 0:
                    // Run round robin
                    while(!processList.isEmpty()) {
                        ListIterator<Proc> it = processList.listIterator();
                        while(it.hasNext()) {
                            int quantum = quantumModel.getNumber().intValue();
                            Proc p = it.next(); 
                            int sleep = quantum;
                            if(p.time - quantum <= 0) {
                                sleep = p.time;
                            }
                            publish("Process "+p.id+" executed for: "+sleep+"ms");
                            if(p.takeTime(quantum)){
                                publish("Process "+p.id+" finished");
                                it.remove();
                            } 
                        }
                        recruitProcess();
                    }
                    break;
                case 1:
                    // Run FIFO
                    while(!processList.isEmpty()) {
                        recruitProcess();
                        Proc p = processList.removeFirst();
                        publish("Process "+p.id+" finished in "+p.time+"ms");
                        p.finish();
                    }
                    break;
                case 2:
                    // Run SJF
                    while(!processList.isEmpty()) {
                        recruitProcess();
                        ListIterator<Proc> it = processList.listIterator();
                        Proc shortest = it.next();
                        while(it.hasNext()) {
                            Proc p = it.next();
                            if(p.time < shortest.time)
                                shortest = p;
                        }
                        publish("Process "+shortest.id+" finished in "+shortest.time+"ms");
                        shortest.finish();
                        // Takes O(n) time to remove
                        processList.remove(shortest);
                    }
                default:
                    break;
                }
                
                return null;
            }
            @Override
            protected void process(List<String> chunk) {
                for (String string : chunk) {
                    textArea.append(string+"\n");
                }
            }
            
            @Override
            protected void done() {
                tglbtnStart.setText("Start");
                tglbtnStart.setSelected(false);
                isStarted = false;
                publish("Simulation Complete");
            }    
    }
    private void recruitProcess() {
        Random rng = new Random();
        for(int i = 0; i < newProcsModel.getNumber().intValue(); i++) {
            if(rng.nextInt(100) <= newProcProbModel.getNumber().intValue())
                addProcess();
        }
    }
    private void addProcess() {
        Random rng = new Random();
        Proc p = new Proc(pid++,rng.nextInt(1000));
        processList.add(p);
        textArea.append("Added pid "+p.id+" with time "+p.time+"ms\n");
        simulation.add(p);
    }
    private void setupProcesses() {
        pid = 0;
        int procs = processModel.getNumber().intValue();
        processList.clear();
        textArea.setText("");
        simulation.removeAll();
        
        for(int i = 0; i < procs; i++ ) {
            addProcess();
        }
        simulation.revalidate();
    }
    /**
     * This runs the process
     * @param args
     */
    public static void main(String args[]) {
        Main r = new Main();
        r.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        r.pack();
        r.setVisible(true);
    }
    /**
     * This class represents a process, each process has an id and time remaining
     */
    public class Proc extends JPanel{
        
        /**
         * 
         */
        private static final long serialVersionUID = -2070799490577412344L;
        private int time,id;
        private JLabel timeLbl, idLbl;
        Proc(int id,int t) {
            time = t;
            this.id = id;
            setLayout(new GridLayout(2,1));
            resetBorder(Color.green);
            idLbl = new JLabel(""+id,JLabel.CENTER);
            timeLbl = new JLabel(""+time,JLabel.CENTER);
            add(idLbl);
            add(timeLbl);
        }
        /**
         * Finish the process
         * @throws InterruptedException 
         */
        public void finish() throws InterruptedException {
            takeTime(time);
        }
        public String toString() {
            return String.format("%d,%d",id,time);
        }
        
        public int getId() {
            return id;
        }
        private void resetBorder(Color c) {
            setBorder(BorderFactory.createLineBorder(c, 3));
        }
        public boolean takeTime(int q) throws InterruptedException {
            resetBorder(Color.red);
            time -= q;
            int delayFactor = delayModel.getNumber().intValue();
            if(time <= 0) {
                Thread.sleep((time+q)*delayFactor);
                resetBorder(Color.black);
                time = 0;
            } else {
                Thread.sleep(q*delayFactor);
                resetBorder(Color.green);
            }
            timeLbl.setText(""+time);
            if(time == 0)
                return true;
            else
                return false;
        }
    }
}