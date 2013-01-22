package hr.fer.hmo.projectscheduling;

import hr.fer.hmo.projectscheduling.aco.ACO;
import hr.fer.hmo.projectscheduling.ais.ClonAlg;
import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.Util;
import hr.fer.hmo.projectscheduling.common.WorkUnit;
import hr.fer.hmo.projectscheduling.configuration.ACORun;
import hr.fer.hmo.projectscheduling.configuration.ClonAlgRun;
import hr.fer.hmo.projectscheduling.configuration.CycleACOPSORun;
import hr.fer.hmo.projectscheduling.configuration.CycleAlgorithmsRun;
import hr.fer.hmo.projectscheduling.configuration.CycleEDAClonAlgRun;
import hr.fer.hmo.projectscheduling.configuration.CycleEDARun;
import hr.fer.hmo.projectscheduling.configuration.CycleGAEDARun;
import hr.fer.hmo.projectscheduling.configuration.EDARun;
import hr.fer.hmo.projectscheduling.configuration.GARun;
import hr.fer.hmo.projectscheduling.configuration.PSORun;
import hr.fer.hmo.projectscheduling.configuration.RunConfiguration;
import hr.fer.hmo.projectscheduling.eda.EDA;
import hr.fer.hmo.projectscheduling.ga.GA;
import hr.fer.hmo.projectscheduling.pso.PSO;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

/**
 * EvoScheduler - Evolutionary Project Scheduler
 * @author Petar Čolić, petar.colic@fer.hr
 * @author Karlo Knežević, karlo.knezevic@fer
 * @author Ivo Majić, ivo.majic2@fer.hr
 * @version 1.0
 */
public class EvoScheduler extends JFrame implements ActionListener {

	private static final long serialVersionUID = 5310842011527059936L;
	
	CategoryDataset dataset = new DefaultCategoryDataset();
	
	JFreeChart scheduleChart;
	ScheduleRenderer scheduleRenderer;
	
	Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
	
	JProgressBar progress = new JProgressBar(1,100);
	JButton runButton = new JButton("Run configuration");
	JLabel duration = new JLabel("0");
	JLabel wait = new JLabel("0");
	JMenuBar menuBar = new JMenuBar();

	public EvoScheduler() throws IOException {
		super();
		
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			
		});
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Initialize chart
		scheduleChart = ChartFactory.createStackedBarChart(
				null, 
				"Worker", 
				"Duration", 
				dataset, 
				PlotOrientation.HORIZONTAL, 
				true, 
				false, 
				false
		);
		
		LegendTitle legend = scheduleChart.getLegend(); 
		legend.setPosition(RectangleEdge.BOTTOM);
		legend.setMargin(10, 10, 10, 10);
		
		CategoryPlot plot = (CategoryPlot) scheduleChart.getPlot();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		scheduleRenderer = 	new ScheduleRenderer();
		scheduleRenderer.setShadowVisible(false);
		scheduleRenderer.setBarPainter(new StandardBarPainter());
		scheduleRenderer.setBaseItemLabelsVisible(true);
		plot.setRenderer(scheduleRenderer);
		
		ChartPanel scheduleChartPanel = new ChartPanel(scheduleChart);
		
		// Initialize algorithms
		Algorithm algorithm;
		algorithm = new ClonAlg();
		algorithms.put(algorithm.toString(), algorithm);
		algorithm = new ACO();
		algorithms.put(algorithm.toString(), algorithm);
		algorithm = new PSO();
		algorithms.put(algorithm.toString(), algorithm);
		algorithm = new EDA();
		algorithms.put(algorithm.toString(), algorithm);
		algorithm = new GA();
		algorithms.put(algorithm.toString(), algorithm);
		
		// Menu
		JMenu menu = new JMenu("File");
		JMenuItem menuItem = new JMenuItem("Load existing result file...");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		
		// Initialize layout
		setJMenuBar(menuBar);
		setLayout(new BorderLayout());
		scheduleChartPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(scheduleChartPanel, BorderLayout.CENTER);
		add(new Controls(), BorderLayout.EAST);
		add(new Results(), BorderLayout.SOUTH);
		
		// Start
		setSize(950, 700);
		setVisible(true);
		setTitle("HOM Evolutionary Project Scheduler");
		
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.showOpenDialog(this);
		try {
			
			ChartIndividual individual = new ChartIndividual(
					Util.readInputFile(fileChooser.getSelectedFile().getPath())
			);
			scheduleRenderer.setProjectSchedule(
					individual.getProjectSchedule()
			);
			individual.loadScheduleDataset(
					(DefaultCategoryDataset) dataset
			);
			duration.setText(String.valueOf(individual.getActualDuration()));
			wait.setText(String.valueOf(individual.getWaitDuration()));
			
		} catch (Exception e) {
			
			JOptionPane.showMessageDialog(
					null, 
					"Error while reading input file!", 
					"Error!", JOptionPane.ERROR_MESSAGE
			);
			
		}
		
	}
	
	private class Results extends JPanel {

		private static final long serialVersionUID = -3405585524114945358L;

		public Results() {
			
			super();
			
			JLabel durationLabel = new JLabel("Duration: ");
			JLabel waitLabel = new JLabel("Wait: ");
			
			add(durationLabel);
			add(duration);
			
			add(Box.createRigidArea(new Dimension(50,0)));
			
			add(waitLabel);
			add(wait);
			
		}
		
	}
	
	private class Controls extends JPanel implements ActionListener {

		private static final long serialVersionUID = 4009932158898529418L;
		
		JPanel algorithmParameters;
		JComboBox<RunConfiguration> runConfSelector;

		public Controls() {
			
			super();
			
			setBorder(new EmptyBorder(10, 0, 10, 10));
	        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	        
	        
	        JLabel algorithmSelectorLabel = new JLabel("Algorithm parameters");
	        algorithmSelectorLabel.setAlignmentX(CENTER_ALIGNMENT);
	        
	        JComboBox<Algorithm> algorithmSelector = new JComboBox<Algorithm>();
	        for (Algorithm algorithm : algorithms.values()) {
	        	algorithmSelector.addItem(algorithm);
	        }
	        algorithmSelector.setEditable(false);
	        algorithmSelector.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent event) {
					
					CardLayout layout = 
							(CardLayout) algorithmParameters.getLayout();
			        layout.show(
			        		algorithmParameters, 
			        		event.getItem().toString()
			        );
					
				}
				
			});
	        algorithmSelector.setBorder(new EmptyBorder(0, 0, 10, 0));
	        
	        // ACO parameters
	        JPanel aco = new ACOParam((ACO) algorithms.get("ACO"));

	        // ClonAlg parameters
	        JPanel clonAlg = new ClonAlgParam(
	        		(ClonAlg) algorithms.get("ClonAlg")
	        );
	        
	        // PSO parameters
	        JPanel pso = new PSOParam((PSO) algorithms.get("PSO"));
	        
	        // EDA parameters
	        JPanel eda = new EDAParam((EDA) algorithms.get("EDA"));
	        
	        // GA parameters
	        JPanel ga = new GAParam((GA) algorithms.get("GA"));
		        
	        algorithmParameters = new JPanel(new CardLayout());
	        algorithmParameters.add(aco, 	"ACO");
	        algorithmParameters.add(clonAlg,"ClonAlg");
	        algorithmParameters.add(pso, 	"PSO");
	        algorithmParameters.add(eda, 	"EDA");
	        algorithmParameters.add(ga, 	"GA");
	        
	        CardLayout layout = (CardLayout) algorithmParameters.getLayout();
	        layout.show(
	        		algorithmParameters, 
	        		algorithmSelector.getSelectedItem().toString()
	        );
	        
		    // Run configuration selector
	        JLabel runConfSelectorLabel = new JLabel("Run configuration");
	        runConfSelectorLabel.setAlignmentX(CENTER_ALIGNMENT);
	        
	        runConfSelector = new JComboBox<RunConfiguration>();
	        
	        // ADD CONFIGURATIONS.setProgress(progress)
	        runConfSelector.addItem(new ClonAlgRun(algorithms, progress));
	        runConfSelector.addItem(new ACORun(algorithms, progress));
	        runConfSelector.addItem(new EDARun(algorithms, progress));
	        runConfSelector.addItem(new PSORun(algorithms, progress));
	        runConfSelector.addItem(new GARun(algorithms, progress));
	        runConfSelector.addItem(new CycleACOPSORun(algorithms, progress));
	        runConfSelector.addItem(new CycleEDARun(algorithms, progress));
	        runConfSelector.addItem(
	        		new CycleEDAClonAlgRun(algorithms, progress)
	        );
	        runConfSelector.addItem(
	        		new CycleGAEDARun(algorithms, progress)
	        );
	        runConfSelector.addItem(
	        		new CycleAlgorithmsRun(algorithms, progress)
	        );
	        // END ADD CONFIGURATION
	        
	        runConfSelector.setEditable(false);
	        
	        runButton.addActionListener(this);
	        runButton.setAlignmentX(CENTER_ALIGNMENT);
	        
	        add(progress);
	        add(Box.createRigidArea(new Dimension(0,10)));
	        add(algorithmSelectorLabel);
	        add(algorithmSelector);
	        add(algorithmParameters);
	        add(runConfSelectorLabel);
	        add(runConfSelector);
	        add(Box.createRigidArea(new Dimension(0,20)));
	        add(runButton);
	        
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			
			new ScheduleWorker(
					(RunConfiguration) runConfSelector.getSelectedItem()
			).execute();
			
		}
		
	}
	
	private class ParamPanel extends JPanel {
		
		private static final long serialVersionUID = -8782550005010428177L;
		protected GridBagConstraints gbc;

		public ParamPanel() {
			super();
			
			gbc = new GridBagConstraints();
	        gbc.fill = GridBagConstraints.HORIZONTAL;
	        gbc.gridwidth = GridBagConstraints.REMAINDER;
	        gbc.weightx = 1;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        
		}
		
	}
	
	private class ClonAlgParam extends ParamPanel {
		
		private static final long serialVersionUID = -932541795998391911L;

		public ClonAlgParam(final ClonAlg algorithm) {
			
			super();
			setLayout(new GridBagLayout());
			
			// Spinner models
			// Iterations
			SpinnerNumberModel modelP1 = 
				new SpinnerNumberModel(algorithm.getIterations(), 25, 200, 1);
			// Population size
			SpinnerNumberModel modelP2 = 
				new SpinnerNumberModel(algorithm.getParamN(), 50, 500, 1);
			// Random antibodies per iteration
			SpinnerNumberModel modelP3 = 
				new SpinnerNumberModel(algorithm.getParamD(), 10, 100, 1);
			// β
			SpinnerNumberModel modelP4 = 
				new SpinnerNumberModel(algorithm.getParamβ(), 1, 50, 1);
	        
	        JLabel clonAlgP1Label = new JLabel("Iterations");
	        add(clonAlgP1Label, gbc);
	        JSpinner clonAlgP1 = new JSpinner(modelP1);
	        clonAlgP1.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setIterations(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(clonAlgP1, gbc);
	        
	        JLabel clonAlgP2Label = new JLabel("Population size");
	        add(clonAlgP2Label, gbc);
	        JSpinner clonAlgP2 = new JSpinner(modelP2);
	        clonAlgP2.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setParamN(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(clonAlgP2, gbc);
	        
	        JLabel clonAlgP3Label = 
	        		new JLabel("Random antibodies per iteration");
	        add(clonAlgP3Label, gbc);
	        JSpinner clonAlgP3 = new JSpinner(modelP3);
	        clonAlgP3.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setParamD(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(clonAlgP3, gbc);
	        
	        JLabel clonAlgP4Label = new JLabel("β");
	        add(clonAlgP4Label, gbc);
	        JSpinner clonAlgP4 = new JSpinner(modelP4);
	        clonAlgP4.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setParamβ(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(clonAlgP4, gbc);
	        
	        gbc.weighty = 1;
	        add(new JPanel(), gbc);
	        gbc.weighty = 0;
	        
		}
		
	}
	
	private class ACOParam extends ParamPanel {

		private static final long serialVersionUID = -2352484776304345540L;

		public ACOParam(final ACO algorithm) {
			
			super();
			setLayout(new GridBagLayout());
			
			// Spinner models
			// Iterations
			SpinnerNumberModel modelP1 = 
				new SpinnerNumberModel(algorithm.getWalkNumber(), 150, 300, 1);
			// ρ
			SpinnerNumberModel modelP2 = 
				new SpinnerNumberModel(algorithm.getRo(), 0.0, 1.0, 0.1);
			// α
			SpinnerNumberModel modelP3 = 
				new SpinnerNumberModel(algorithm.getΑ(), 0.0, 3.0, 0.1);
			// β
			SpinnerNumberModel modelP4 = 
				new SpinnerNumberModel(algorithm.getΒ(), 0.0, 3.0, 0.1);
			// Colony size
			SpinnerNumberModel modelP5 = 
				new SpinnerNumberModel(algorithm.getColonySize(), 30, 60, 1);
	        
	        JLabel acoP1Label = new JLabel("Iterations");
	        add(acoP1Label, gbc);
	        JSpinner acoP1 = new JSpinner(modelP1);
	        acoP1.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setWalkNumber(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(acoP1, gbc);
	        
	        JLabel acoP2Label = new JLabel("ρ");
	        add(acoP2Label, gbc);
	        JSpinner acoP2 = new JSpinner(modelP2);
	        acoP2.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setRo(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(acoP2, gbc);
	        
	        JLabel acoP3Label = new JLabel("α");
	        add(acoP3Label, gbc);
	        JSpinner acoP3 = new JSpinner(modelP3);
	        acoP3.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setΑ(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(acoP3, gbc);
	        
	        JLabel acoP4Label = new JLabel("β");
	        add(acoP4Label, gbc);
	        JSpinner acoP4 = new JSpinner(modelP4);
	        acoP4.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setΒ(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(acoP4, gbc);
	        
	        JLabel acoP5Label = new JLabel("Colony size");
	        add(acoP5Label, gbc);
	        JSpinner acoP5 = new JSpinner(modelP5);
	        acoP5.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setColonySize(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(acoP5, gbc);
	        
	        JCheckBox acoP6 = new JCheckBox("Best 10% make update");
	        acoP6.setSelected(algorithm.isBestAntsMakeTrailUpdate());
	        acoP6.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent event) {
					
					algorithm.setBestAntsMakeTrailUpdate(
							((JCheckBox) event.getItem()).isSelected()
					);
					
				}
				
			});
	        add(acoP6, gbc);
	        
	        gbc.weighty = 1;
	        add(new JPanel(), gbc);
	        gbc.weighty = 0;
			
		}
		
	}
	
	private class PSOParam extends ParamPanel {
		
		private static final long serialVersionUID = 2043043371193478877L;

		public PSOParam(final PSO algorithm) {
			
			super();
			setLayout(new GridBagLayout());
			
			// Spinner models
			// Iterations
			SpinnerNumberModel modelP1 = 
				new SpinnerNumberModel(
						algorithm.getSwarmIterations(), 200, 400, 1
				);
			// C1
			SpinnerNumberModel modelP2 = 
				new SpinnerNumberModel(algorithm.getC1(), 1, 10, 0.1);
			// C2
			SpinnerNumberModel modelP3 = 
				new SpinnerNumberModel(algorithm.getC2(), 1, 10, 0.1);
			// γ
			SpinnerNumberModel modelP4 = 
				new SpinnerNumberModel(algorithm.getΓ(), 0.0, 2.0, 0.1);
			// Neighborhood Size
			SpinnerNumberModel modelP5 = 
				new SpinnerNumberModel(
						algorithm.getNeighborhoodSize(), 1, 7, 2
				);
			// Particle count
			SpinnerNumberModel modelP6 = 
				new SpinnerNumberModel(
						algorithm.getSwarmParticleCount(), 20, 60, 1
				);
	        
	        JLabel psoP1Label = new JLabel("Iterations");
	        add(psoP1Label, gbc);
	        JSpinner psoP1 = new JSpinner(modelP1);
	        psoP1.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setSwarmIterations(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(psoP1, gbc);
	        
	        JLabel psoP2Label = new JLabel("C1");
	        add(psoP2Label, gbc);
	        JSpinner psoP2 = new JSpinner(modelP2);
	        psoP2.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setC1(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(psoP2, gbc);
	        
	        JLabel psoP3Label = new JLabel("C2");
	        add(psoP3Label, gbc);
	        JSpinner psoP3 = new JSpinner(modelP3);
	        psoP3.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setC2(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(psoP3, gbc);
	        
	        JLabel psoP4Label = new JLabel("γ");
	        add(psoP4Label, gbc);
	        JSpinner psoP4 = new JSpinner(modelP4);
	        psoP4.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setΓ(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(psoP4, gbc);
	        
	        JLabel psoP5Label = new JLabel("Local Neighborhood Size");
	        add(psoP5Label, gbc);
	        final JSpinner psoP5 = new JSpinner(modelP5);
	        psoP5.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setNeighborhoodSize(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(psoP5, gbc);
	        
	        JLabel psoP6Label = new JLabel("Particle Count");
	        add(psoP6Label, gbc);
	        JSpinner psoP6 = new JSpinner(modelP6);
	        psoP6.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setSwarmParticleCount(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(psoP6, gbc);
	        
	        JCheckBox psoP7 = new JCheckBox("Global neighborhood");
	        psoP7.setSelected(algorithm.isGlobalNeighborhoodSet());
	        psoP7.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent event) {
					
					algorithm.setGlobalNeighborhoodSet(
							((JCheckBox) event.getItem()).isSelected()
					);
					psoP5.setEnabled(
							!((JCheckBox) event.getItem()).isSelected()
					);
					
				}
				
			});
	        add(psoP7, gbc);
	        
	        gbc.weighty = 1;
	        add(new JPanel(), gbc);
	        gbc.weighty = 0;
	        
		}
		
	}
	
	private class EDAParam extends ParamPanel {

		private static final long serialVersionUID = 3033414968013365693L;

		public EDAParam(final EDA algorithm) {
			super();
			setLayout(new GridBagLayout());
			
			// Spinner models
			// Iterations
			SpinnerNumberModel modelP1 = 
				new SpinnerNumberModel(
					algorithm.getGenerations(), 100, 300, 1
				);
			// Individuals for ε
			SpinnerNumberModel modelP2 = 
				new SpinnerNumberModel(
					algorithm.getIndividualsProportionForEstimation(), 
					0.0, 1.0, 0.1
				);
			// Population size
			SpinnerNumberModel modelP3 = 
				new SpinnerNumberModel(algorithm.getPopulationSize(), 20, 50, 1);
			// λ
			SpinnerNumberModel modelP4 = 
				new SpinnerNumberModel(algorithm.getΛ(), 0, 10, 0.1);
			
			JLabel edaP1Label = new JLabel("Iterations");
	        add(edaP1Label, gbc);
	        JSpinner edaP1 = new JSpinner(modelP1);
	        edaP1.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setGenerations(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(edaP1, gbc);
	        
	        JLabel edaP2Label = new JLabel("Individuals for ε");
	        add(edaP2Label, gbc);
	        JSpinner edaP2 = new JSpinner(modelP2);
	        edaP2.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setIndividualsProportionForEstimation(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(edaP2, gbc);
	        
	        JLabel edaP3Label = new JLabel("Population size");
	        add(edaP3Label, gbc);
	        JSpinner edaP3 = new JSpinner(modelP3);
	        edaP3.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setPopulationSize(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(edaP3, gbc);
	        
	        JLabel edaP4Label = new JLabel("λ");
	        add(edaP4Label, gbc);
	        JSpinner edaP4 = new JSpinner(modelP4);
	        edaP4.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setΛ(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(edaP4, gbc);
			
			gbc.weighty = 1;
	        add(new JPanel(), gbc);
	        gbc.weighty = 0;
			
		}
		
	}
	
	private class GAParam extends ParamPanel {

		private static final long serialVersionUID = 6804965821364327782L;

		public GAParam(final GA algorithm) {
			super();
			setLayout(new GridBagLayout());
			
			// Spinner models
			// Number of generations
			SpinnerNumberModel modelP1 = 
				new SpinnerNumberModel(algorithm.getIterations(), 100, 2000, 1);
			// Population size
			SpinnerNumberModel modelP2 = 
				new SpinnerNumberModel(algorithm.getPopsize(), 50, 2000, 1);
			// Elitism
			SpinnerNumberModel modelP3 = 
				new SpinnerNumberModel(algorithm.getElitism(), 0, 5, 1);
			// Crossover probability
			SpinnerNumberModel modelP4 = 
				new SpinnerNumberModel(algorithm.getCrossProb(), 0.1, 1.0, 0.1);
			// Mutation probability
			SpinnerNumberModel modelP5 = 
				new SpinnerNumberModel(algorithm.getMutProb(), 0.001, 0.05, 0.001);
			
			JLabel gaP1Label = new JLabel("Number of generations");
	        add(gaP1Label, gbc);
	        JSpinner gaP1 = new JSpinner(modelP1);
	        gaP1.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setIterations(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(gaP1, gbc);
	        
	        JLabel gaP2Label = new JLabel("Population size");
	        add(gaP2Label, gbc);
	        JSpinner gaP2 = new JSpinner(modelP2);
	        gaP2.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setPopsize(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(gaP2, gbc);
	        
	        JLabel gaP3Label = new JLabel("Elitism");
	        add(gaP3Label, gbc);
	        JSpinner gaP3 = new JSpinner(modelP3);
	        gaP3.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setElitism(
							(Integer) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(gaP3, gbc);
	        
	        JLabel gaP4Label = new JLabel("Crossover probability");
	        add(gaP4Label, gbc);
	        JSpinner gaP4 = new JSpinner(modelP4);
	        gaP4.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setCrossProb(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(gaP4, gbc);
	        
	        JLabel gaP5Label = new JLabel("Mutation probability");
	        add(gaP5Label, gbc);
	        JSpinner gaP5 = new JSpinner(modelP5);
	        gaP5.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent event) {
					algorithm.setMutProb(
							(Double) (
									(JSpinner) event.getSource()
							).getValue());
				}
				
			});
	        add(gaP5, gbc);
	        
	        gbc.weighty = 1;
	        add(new JPanel(), gbc);
	        gbc.weighty = 0;
	        
		}
		
	}
	
	private class ScheduleRenderer extends StackedBarRenderer {
		
		private static final long serialVersionUID = 871982260604345046L;
		private List<ArrayList<Integer>> projectSchedule;
		private int personCount;
		private Paint[] colors;
		
		public ScheduleRenderer() {
			
			super();
			this.colors = new Paint[] {
					new Color(220, 20, 60),		// CRIMSON
					new Color(255, 69, 0),		// ORANGE RED 
					new Color(255, 140, 0), 	// DARK ORANGE
					new Color(255, 215, 0), 	// GOLD
					new Color(154, 205, 50),	// YELLOW GREEN
					new Color(173, 255, 47), 	// GREEN YELLOW
					new Color(0,139,139), 		// DARK CYAN
					new Color(0, 206, 206),		// DARK TURQUOISE 
					new Color(70, 130,180),		// STEEL BLUE 
					new Color(199, 21, 133),	// MEDIUM VIOLET RED 
					new Color(160, 82, 45),		// SIENNA 
					new Color(148, 0, 211),		// DARK VIOLET 
					new Color(189, 183, 107), 	// DARK KHAKI
					new Color(34, 139, 34),		// FOREST GREEN
					new Color(255, 105, 180),	// HOT PINK
			};
			
		}

		@Override
		public Paint getItemPaint(int row, int column) {
			
			int projectId = projectSchedule.get(column).get(row);
			if (projectId == -1) return new Color(255,255,255,0);
			return colors[projectId];
			
		}

		@Override
		public LegendItem getLegendItem(int datasetIndex, int series) {
			
			if (series < personCount)
				return new LegendItem("Project "+series, colors[series]);
			return null;
			
		}
		
		public void setProjectSchedule(
				List<ArrayList<Integer>> projectSchedule) {
			
			this.projectSchedule = projectSchedule;
			this.personCount = projectSchedule.size();
			
		}
		
	}
	
	private class ChartIndividual extends Individual {

		public ChartIndividual(List<ArrayList<WorkUnit>> projectWorkLists) {
			super(projectWorkLists);
		}
		
		public List<ArrayList<Integer>> getProjectSchedule() {
			
			List<ArrayList<Integer>> projectSchedule = 
					new ArrayList<ArrayList<Integer>>();
			
			this.calculateFitness();
			
			for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {
				
				int endTime = 0;
				
				ArrayList<Integer> personSchedule = new ArrayList<Integer>();
				
				for (WorkUnitInterval workUnit : personWorkList) {
					
					int waitDuration = workUnit.getStartTime() - endTime;
					endTime = workUnit.getEndTime();
					
					int projectId = workUnit.getWorkUnit().getProjectId();
					
					if (waitDuration > 0) 
						personSchedule.add(-1);
					
					personSchedule.add(projectId);
					
				}
				
				projectSchedule.add(personSchedule);
				
			}
			
			return projectSchedule;
			
		}

		public void loadScheduleDataset(DefaultCategoryDataset dataset) {
			
			dataset.clear();
			this.calculateFitness();
			
			for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {
				
				int endTime = 0;
				int count = 0;
				for (WorkUnitInterval workUnit : personWorkList) {
					
					int actualDuration = 
							workUnit.getEndTime() - workUnit.getStartTime();
					int waitDuration = workUnit.getStartTime() - endTime;
					endTime = workUnit.getEndTime();
					
					int personId = workUnit.getWorkUnit().getWorkerId();
					
					if (waitDuration > 0) {
						
						dataset.addValue(
								waitDuration, 
								Integer.valueOf(count), 
								Integer.valueOf(personId) 
						);
						
						count++;
						
					}
					
					dataset.addValue(
							actualDuration, 
							Integer.valueOf(count), 
							Integer.valueOf(personId)
					);
					
					count++;
					
				}
				
			}
			
		}
		
	}

	private class ScheduleWorker extends SwingWorker<Void, Object> {
		
		RunConfiguration runConf;

		public ScheduleWorker(RunConfiguration runConf) {
			super();
			this.runConf = runConf;
		}

		@Override
		protected Void doInBackground() throws Exception {
			
			runButton.setEnabled(false);
			runConf.runConfiguration();
			ChartIndividual individual = 
				new ChartIndividual(
					runConf.getRunBestIndividual().getProjectWorkLists()
				);
			scheduleRenderer.setProjectSchedule(
					individual.getProjectSchedule()
			);
			individual.loadScheduleDataset(
					(DefaultCategoryDataset) dataset
			);
			duration.setText(String.valueOf(individual.getActualDuration()));
			wait.setText(String.valueOf(individual.getWaitDuration()));
			
			runButton.setEnabled(true);
			return null;
			
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		
		new EvoScheduler();
		
	}

}
