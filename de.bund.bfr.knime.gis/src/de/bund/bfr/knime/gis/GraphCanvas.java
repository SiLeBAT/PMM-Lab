package de.bund.bfr.knime.gis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.collections15.Transformer;

import de.bund.bfr.knime.gis.GraphCanvas.Node;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;

public class GraphCanvas extends JPanel implements ActionListener,
		GraphMouseListener<Node> {

	private static final long serialVersionUID = 1L;

	private static final String CIRCLE_LAYOUT = "Circle Layout";
	private static final String FR_LAYOUT = "FR Layout";
	private static final String[] LAYOUTS = { CIRCLE_LAYOUT, FR_LAYOUT };

	private static final String DEFAULT_LAYOUT = CIRCLE_LAYOUT;
	private static final int DEFAULT_NODESIZE = 10;

	private Graph<Node, Edge> graph;
	private VisualizationViewer<Node, Edge> viewer;

	private JComboBox<String> layoutBox;
	private JTextField nodeSizeField;
	private JButton applyButton;

	public GraphCanvas(List<Node> nodes, List<Edge> edges) {
		graph = new SparseMultigraph<Node, Edge>();

		for (Node node : nodes) {
			graph.addVertex(node);
		}

		for (Edge edge : edges) {
			graph.addEdge(edge, edge.getN1(), edge.getN2());
		}

		viewer = createViewer(DEFAULT_LAYOUT, DEFAULT_NODESIZE);
		setLayout(new BorderLayout());
		add(viewer, BorderLayout.CENTER);
		add(createOptionsPanel(), BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == applyButton) {
			try {
				remove(viewer);
				viewer = createViewer((String) layoutBox.getSelectedItem(),
						Integer.parseInt(nodeSizeField.getText()));
				add(viewer, BorderLayout.CENTER);
				revalidate();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Node Size must be Integer Value", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void graphClicked(Node v, MouseEvent me) {
		NodePropertiesDialog dialog = new NodePropertiesDialog(
				me.getComponent(), v);

		dialog.setLocation(me.getLocationOnScreen());
		dialog.setVisible(true);
	}

	@Override
	public void graphPressed(Node v, MouseEvent me) {
	}

	@Override
	public void graphReleased(Node v, MouseEvent me) {
	}

	private VisualizationViewer<Node, Edge> createViewer(String layoutType,
			int nodeSize) {
		Dimension size = null;
		Layout<Node, Edge> layout = null;

		if (viewer != null) {
			size = viewer.getSize();
		} else {
			size = new Dimension(400, 600);
		}

		if (layoutType.equals(CIRCLE_LAYOUT)) {
			layout = new CircleLayout<Node, Edge>(graph);
		} else if (layoutType.equals(FR_LAYOUT)) {
			layout = new FRLayout<Node, Edge>(graph);
			((FRLayout<Node, Edge>) layout).setMaxIterations(100);
		}

		layout.setSize(size);

		VisualizationViewer<Node, Edge> vv = new VisualizationViewer<Node, Edge>(
				layout);

		vv.setPreferredSize(size);
		vv.setGraphMouse(new DefaultModalGraphMouse<Integer, String>());
		vv.addGraphMouseListener(this);
		vv.getRenderContext().setVertexShapeTransformer(
				new ShapeTransformer(nodeSize));

		return vv;
	}

	private JPanel createOptionsPanel() {
		layoutBox = new JComboBox<String>(LAYOUTS);
		layoutBox.setSelectedItem(DEFAULT_LAYOUT);
		layoutBox.addActionListener(this);
		nodeSizeField = new JTextField("" + DEFAULT_NODESIZE);
		nodeSizeField.setPreferredSize(new Dimension(50, nodeSizeField
				.getPreferredSize().height));
		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);

		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout());
		panel.add(new JLabel("Layout Type:"));
		panel.add(layoutBox);
		panel.add(new JLabel("Node Size:"));
		panel.add(nodeSizeField);
		panel.add(applyButton);

		return panel;
	}

	public static class Node {

		private String region;
		private Map<String, String> properties;

		public Node(String region, Map<String, String> properties) {
			this.region = region;
			this.properties = properties;
		}

		public String getRegion() {
			return region;
		}

		public Map<String, String> getProperties() {
			return properties;
		}
	}

	public static class Edge {

		private Node n1;
		private Node n2;
		private double value;

		public Edge(Node n1, Node n2, double value) {
			this.n1 = n1;
			this.n2 = n2;
			this.value = value;
		}

		public double getValue() {
			return value;
		}

		public Node getN1() {
			return n1;
		}

		public Node getN2() {
			return n2;
		}
	}

	private static class ShapeTransformer implements Transformer<Node, Shape> {

		private int size;

		public ShapeTransformer(int size) {
			this.size = size;
		}

		@Override
		public Shape transform(Node i) {
			Ellipse2D circle = new Ellipse2D.Double(-size / 2, -size / 2, size,
					size);

			return circle;
		}

	}

	private static class NodePropertiesDialog extends JDialog implements
			ActionListener {

		private static final long serialVersionUID = 1L;

		public NodePropertiesDialog(Component parent, Node node) {
			super(JOptionPane.getFrameForComponent(parent), "Properties", true);

			JPanel centerPanel = new JPanel();
			JPanel leftCenterPanel = new JPanel();
			JPanel rightCenterPanel = new JPanel();

			leftCenterPanel.setLayout(new GridLayout(node.getProperties()
					.size(), 1, 5, 5));
			rightCenterPanel.setLayout(new GridLayout(node.getProperties()
					.size(), 1, 5, 5));

			for (Map.Entry<String, String> property : node.getProperties()
					.entrySet()) {
				JTextField field = new JTextField(property.getValue());

				field.setEditable(false);
				leftCenterPanel.add(new JLabel(property.getKey() + ":"));
				rightCenterPanel.add(field);
			}

			centerPanel.setLayout(new BorderLayout(5, 5));
			centerPanel.add(leftCenterPanel, BorderLayout.WEST);
			centerPanel.add(rightCenterPanel, BorderLayout.CENTER);

			JButton okButton = new JButton("OK");
			JPanel bottomPanel = new JPanel();

			okButton.addActionListener(this);
			bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			bottomPanel.add(okButton);

			setLayout(new BorderLayout());
			add(centerPanel, BorderLayout.CENTER);
			add(bottomPanel, BorderLayout.SOUTH);
			pack();

			setResizable(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}

}
