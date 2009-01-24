package org.meandre.zigzag.prefuse;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.JFrame;

import org.meandre.core.repository.ConnectorDescription;
import org.meandre.core.repository.ExecutableComponentInstanceDescription;
import org.meandre.core.repository.FlowDescription;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.util.ui.JForcePanel;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/** The basic class the handles the display of a flow, as well as it
 * handles the required data structures to show a flow.
 * 
 * @author Xavier Llor&agrave;
 *
 */
public class FlowDrawer {

	/** A defaul serial ID */
	private static final long serialVersionUID = 1L;
   
	protected static final String GRAPH = "graph";
	protected static final String INSTANCE = "INSTANCE";
	protected static final String LABEL = "LABEL";
	protected static final String DESCRIPTION = "DESCRIPTION";
	protected static final String COLOR = "COLOR";
	protected static final String NODES = "graph.nodes";
	protected static final String EDGES = "graph.edges";

	/** The display panel */
	private Display dispDisplay = null;

	/** The force pannel */
	private JForcePanel jspFocePanel = null;

	/** The graph containing visualizing the flow */
	protected Graph g = null;

	/** The edge renderer */
	protected EdgeRenderer edge = null;

	/** The flow description */
	@SuppressWarnings("unused")
	private FlowDescription fd;

	/** The instance map */
	private Hashtable<String, Node> htMapIns;
	
	
	/** Initialize the flow drawer.
	 * 
	 * @param fd The set of controls containing the flow description
	 *
	 */
	public FlowDrawer (FlowDescription fd) {
		
		
		
		// Initialize the data structure
		this.fd = fd;
		
		g = new Graph(true);
		
		// Update the graph
		g.addColumn(COLOR, java.lang.Integer.class);
		g.addColumn(LABEL, java.lang.String.class);
		g.addColumn(DESCRIPTION, java.lang.String.class);
		g.addColumn(INSTANCE, ExecutableComponentInstanceDescription.class);
		
		htMapIns = new Hashtable<String,Node>();
		for ( ExecutableComponentInstanceDescription ecid:fd.getExecutableComponentInstances() )
			add(ecid);
		
		for ( ConnectorDescription cd:fd.getConnectorDescriptions() ) 
			add(cd,fd);
		
		// Update the panels
		initGraphPanel();
	}

	/** Add an executable component to the graph.
	 * 
	 * @param ecid The executable component instance description to add.
	 */
	public void add( ExecutableComponentInstanceDescription ecid ) {
		// Add a new node to the graph
		Node node = g.addNode();
		node.set(LABEL, ecid.getName());
		node.set(DESCRIPTION, ecid.getDescription());
		node.set(INSTANCE, ecid);
		node.set(COLOR,0);
		
		htMapIns.put(ecid.getExecutableComponentInstance().toString(), node);
	}


	/** Add a component descriptor to the graph.
	 * 
	 * @param cd The connector description instance description to add.
	 * @param fd The flow description
	 */
	public void add( ConnectorDescription cd, FlowDescription fd ) {
		// Add a new edge to the graph
		g.addEdge(
				htMapIns.get(cd.getSourceInstance().toString()),
						htMapIns.get(cd.getTargetInstance().toString())
			);
	
	}

	
	/** Initialize the graphs panels.
	 */
	private void initGraphPanel() {
		final Visualization viz = new Visualization();
		viz.add(GRAPH,g);
		
		final LabelRenderer r = new LabelRenderer(LABEL);
		r.setRoundedCorner(8,8);
		//ORG viz.setRendererFactory(new DefaultRendererFactory(r));
	
		//LL
		DefaultRendererFactory rf = new DefaultRendererFactory(r);
		edge = new EdgeRenderer(Constants.EDGE_TYPE_CURVE, Constants.EDGE_ARROW_FORWARD);
		edge.setArrowType(Constants.EDGE_ARROW_FORWARD);
		//edge.setArrowHeadSize(20, 20); 
		rf.add(new InGroupPredicate(EDGES), edge);
		viz.setRendererFactory(rf);
		
		
		for( int i=0, iMax=viz.getDisplayCount(); i<iMax ; i++ ) {
			viz.getDisplay(i).setVisible(true);
			//viz.getDisplay(i).setBackground(Color.WHITE);
		}
	
		
//		final int[] palette = new int[] {
//				ColorLib.color(Color.ORANGE)
//		};
//		final DataColorAction fill = new DataColorAction(NODES, "COLOR",
//			    Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		final ColorAction fill = new ColorAction(NODES,
			    VisualItem.FILLCOLOR, ColorLib.color(Color.ORANGE));
		final ColorAction text = new ColorAction(NODES,
			    VisualItem.TEXTCOLOR, ColorLib.gray(0));
		final ColorAction edges = new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(180));
		
		//LL
		final ColorAction arrow_head = new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(180));

		
		final ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edges);
		//LL
		color.add(arrow_head);
		
		
		final ActionList layout = new ActionList(Activity.DEFAULT_STEP_TIME);
		layout.setDuration(5000);
		ForceSimulator fsm = new ForceSimulator();

		
		NBodyForce nf = new NBodyForce((float)-1, -1, (float)0.5);
        SpringForce sf = new SpringForce((float)0.00001, (float)10);
        DragForce dc = new DragForce((float)0.005);
        sf.init(fsm);
        fsm.addForce(sf);
        fsm.addForce(nf);
        fsm.addForce(dc);
        
		layout.add(new ForceDirectedLayout(GRAPH,fsm,false));
		layout.add(new RepaintAction());
		
		viz.putAction("color", color);
		viz.putAction("layout", layout);
		
		dispDisplay = new Display(viz);
		dispDisplay.setHighQuality(true);
		//display.setSize(800, 500); 
		dispDisplay.addControlListener(new FocusControl(1));
	    dispDisplay.addControlListener(new DragControl());
	    dispDisplay.addControlListener(new PanControl());
	    dispDisplay.addControlListener(new ZoomControl());
	    dispDisplay.addControlListener(new WheelZoomControl());
	    dispDisplay.addControlListener(new ZoomToFitControl());
	    dispDisplay.addControlListener(new ToolTipControl(DESCRIPTION));
	    	    
		
	    jspFocePanel = new JForcePanel(fsm);
	    
	    //viz.run("color");
		//viz.run("layout");
	}
	
	/** Gets the graph display.
	 * 
	 * @return The display
	 */
	public Display getDisplay () {
		return dispDisplay;
	}
	
	/** Gets the force panel.
	 * 
	 * @return The force panel
	 */
	public JForcePanel getForcePanel () {
		return jspFocePanel;
	}

	/** Refresh the visualization after a graph update.
	 * 
	 *
	 */
	private void refreshVisualization() {
		Visualization viz = dispDisplay.getVisualization();
		viz.cancel("layout");
		viz.cancel("color");
		
		for( int i=0, iMax=viz.getDisplayCount(); i<iMax ; i++ ) {
			viz.getDisplay(i).setVisible(true);
			viz.getDisplay(i).setBackground(Color.WHITE);
		}

		viz.run("color");
		viz.run("layout");
		
	}
	
	/** Creates and opens a window for visualizing the provided flow.
	 * 
	 * @param fd The flow to visualize
	 * @return The created window
	 */
	public static JFrame fireViz ( FlowDescription fd ) {
		JFrame jfMainWindow = new JFrame("M e a n d r e |  F l o w D r a w e r");
		
		FlowDrawer fDraw = new FlowDrawer(fd);
		jfMainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
//		JSplitPane sp = new JSplitPane();
//		sp.setRightComponent(fd.getDisplay());
//		sp.setLeftComponent(fd.getForcePanel());
//		sp.setDividerLocation(0.85);
//		jfMainWindow.add(sp);
		jfMainWindow.add(fDraw.getDisplay());
		jfMainWindow.setSize(800,600);
		jfMainWindow.setVisible(true);
		
		fDraw.refreshVisualization();
		
		return jfMainWindow;
	}

}

