package gwt.seca.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.shapes.ShapeBuilder;
import gwt.g2d.client.math.Arc;
import gwt.g2d.client.math.Circle;
import gwt.g2d.client.math.Rectangle;
import gwt.seca.client.util.CircularGrid;
import gwt.seca.client.util.CircularGridIterator;
import gwt.seca.client.util.SecaMath;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DashboardManager {
	
	public DashboardManager() {
		mDashboardPanel = RootPanel.get("seca-Dashboard");
		if (mDashboardPanel==null)
			mDashboardPanel = RootPanel.get();
		mDefaultSurfaceWidth = 100;
		mDefaultSurfaceHeight = 100;
	}
	
	public Panel createPanel(Panel parentPanel, String title, boolean expanded, boolean vertical) {
		final Panel panel = (vertical) ? new VerticalPanel() : new HorizontalPanel();
		panel.setStylePrimaryName("seca-DashboardPanel");
		panel.getElement().setId("seca-"+title+"Panel");
		panel.setVisible(expanded);
		createContainerWidget(parentPanel, title, panel, "seca-DashboardMainContainer");
		return panel;
	}
	public Panel createPanel(Panel parentPanel, String title, boolean expanded) {
		return createPanel(parentPanel, title, expanded, false);
	}
	public Panel createPanel(Panel parentPanel, String title) {
		return createPanel(parentPanel, title, false);
	}
	public Panel createPanel(Panel parentPanel) {
		return createPanel(parentPanel, null);
	}
	public Panel getPanel(String title) {
		RootPanel rootPanel = RootPanel.get("seca-"+title+"Panel");
		if (rootPanel==null)
			rootPanel = RootPanel.get();
		return rootPanel;
	}
	
	public Panel createFlowPanel(Panel parentPanel, String title) 
	{
		Panel flowPanel = new FlowPanel();
		createContainerWidget(parentPanel, title, flowPanel);
		return flowPanel;
	}
	public Label createLabel(Panel parentPanel, String title, String text) {
		Label label = new Label();
		label.setStylePrimaryName("seca-DashboardLabel");
		label.setText(text);
		createContainerWidget(parentPanel, title, label);
		return label;
	}
	public Label createLabel(Panel parentPanel, String title) {
		return createLabel(parentPanel, title, "");
	}
	public Label createLabel(Panel parentPanel) {
		return createLabel(parentPanel, "", "");
	}
	
	public Surface createSurface(Panel parentPanel, String title, int width, int height) {
		Surface surface = new Surface(width, height);
		createContainerWidget(parentPanel, title, surface);
		return surface;
	}
	public Surface createSurface(Panel parentPanel, String title) {
		return createSurface(parentPanel, title, mDefaultSurfaceWidth, mDefaultSurfaceHeight);
	}
	public Surface createSurface(Panel parentPanel) {
		return createSurface(parentPanel, "", mDefaultSurfaceWidth, mDefaultSurfaceHeight);
	}
	
	public ListBox createListBox(Panel parentPanel, String title) {
		ListBox listBox = new ListBox();
		createContainerWidget(parentPanel, title, listBox, true);
		return listBox;
	}
	public ListBox createListBox(Panel parentPanel) {
		return createListBox(parentPanel, "");
	}
	
	public CheckBox createCheckBox(Panel parentPanel, String text) {
		CheckBox checkBox = new CheckBox();
		createContainerWidget(parentPanel, "", checkBox);
		return checkBox;
	}
	
	public RadioButton createRadioButton(Panel parentPanel, String text) {
		RadioButton radioButton = new RadioButton(text);
		createContainerWidget(parentPanel, "", radioButton);
		return radioButton;
	}

	public Frame createFrame(Panel parentPanel, String url) {
		Frame frame = new Frame(url);
		frame.setVisible(true);
		createContainerWidget(parentPanel, "", frame);
		return frame;
	}
	
	public void deleteWidget(Widget widget) {
		if (widget==null)
			return;
		widget.getParent().removeFromParent();
	}
	public void deleteWidgets(Widget... widgets) {
		for (int i = 0; i < widgets.length; i++)
			deleteWidget(widgets[i]);
	}
	public void deletePanel(Panel panel) {
		deleteWidget(panel);
	}
	public void deletePanels(Panel... panels) {
		deleteWidgets(panels);
	}
	
	/**
	 * Draw a centered pie with colored arcs. The angles are counterclockwise and 0 is directed to the top.
	 * The arcs of the pie are built counterclockwise.
	 * @param surface The surface to draw on.
	 * @param startAngle The angle in degrees to start from.
	 * @param endAngle The angle in degrees to end at.
	 * @param arcColors Colors used for the arcs.
	 */
	public void drawPie(Surface surface, float startAngle, float endAngle, Color[] arcColors) {
		int width = surface.getWidth();
		int height = surface.getHeight();
		int resolution = arcColors.length;
		//Construct the pie
		int cX = width/2;
		int cY = height/2;
		float r = .9f*Math.min(cX, cY); //The radius
		int oDeg = -90; //The offset angle in degrees (0 is the x-axis)
		float startDeg = -startAngle;
		float endDeg = -endAngle;
		float stepDeg = (endDeg-startDeg)/resolution; //The angle of one span in degrees.
		Arc[] arcs = new Arc[resolution]; //The arcs are clockwise
		for (int i = 0; i < resolution; i++) {
			arcs[i] = new Arc(cX, cY, r, toRad(oDeg+startDeg+i*stepDeg), toRad(oDeg+startDeg+(i+1)*stepDeg));
		}
		//Draw the pie
		for (int i = 0; i < resolution; i++) {
			surface.setFillStyle(arcColors[i]);
			if (arcs[i]!=null) 
				surface.fillShape(new ShapeBuilder()
				.drawArc(arcs[i])
				.build());
		}
	}
	/**
	 * Draw a centered rectangular grid.
	 * @param surface The surface to draw on.
	 * @param cellColors Colors used for the cells.
	 */
	public void drawRectangularGrid(Surface surface, Color[][] cellColors) {
		// (0, 0) is top-left
		int width = surface.getWidth();
		int height = surface.getHeight();
		int columnCount = cellColors.length;
		int lineCount = cellColors[0].length;
		//Build the grid
		float startX = .05f*width;
		float startY = .05f*height;
		float stepX = .9f*width/columnCount;
		float stepY = .9f*height/lineCount;
		Rectangle[][] grid = new Rectangle[columnCount][lineCount];
		for(int i=0; i<columnCount; i++)
			for(int j=0; j<lineCount; j++) {
				grid[i][j] = new Rectangle(startX+i*stepX, startY+j*stepY, stepX, stepY);
			}
		//Draw the grid
		for (int i=0; i<columnCount; i++)
			for (int j=0; j<lineCount; j++) {
				surface.setFillStyle(cellColors[i][j]);
				if (grid[i][j]!=null) 
					surface.fillShape(new ShapeBuilder()
					.drawRect(grid[i][j])
					.build());
			}
		
	}
	/**
	 * Draw a centered circular grid. The angle 0 is directed to the top.
	 * Only 3x3 grids for now.
	 * @param surface The surface to draw on.
	 * @param cellColors Colors used for the cells. 'colors[i][j]' is used to color the cell at column 'i' and line 'j'.
	 */
	public void drawCircularGrid2(Surface surface, Color[][] cellColors) {
		int width = surface.getWidth();
		int height = surface.getHeight();
		//Build the grid
		int cX = width/2;
		int cY = height/2;
		float r = .9f*Math.min(cX, cY); //The radius
		int oDeg = -90; //The offset angle in degrees (0 is the x-axis)
		float hDeg = 180f/8; //Half the angle of one span in degrees. There are 8 spans
		Arc[][] grid = new Arc[3][3]; //The arcs are clockwise
		grid[0][0] = new Arc(cX, cY, r, toRad(oDeg-45+hDeg), toRad(oDeg-45-hDeg));
		grid[1][0] = new Arc(cX, cY, r, toRad(oDeg+0+hDeg), toRad(oDeg+0-hDeg));
		grid[2][0] = new Arc(cX, cY, r, toRad(oDeg+45+hDeg), toRad(oDeg+45-hDeg));
		
		grid[0][1] = new Arc(cX, cY, r, toRad(oDeg-90+hDeg), toRad(oDeg-90-hDeg));
		grid[1][1] = null;
		grid[2][1] = new Arc(cX, cY, r, toRad(oDeg+90+hDeg), toRad(oDeg+90-hDeg));

		grid[0][2] = new Arc(cX, cY, r, toRad(oDeg-135+hDeg), toRad(oDeg-135-hDeg));
		grid[1][2] = new Arc(cX, cY, r, toRad(oDeg+180+hDeg), toRad(oDeg+180-hDeg));
		grid[2][2] = new Arc(cX, cY, r, toRad(oDeg+135+hDeg), toRad(oDeg+135-hDeg));
		
		//Draw the grid
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				surface.setFillStyle(cellColors[i][j]);
				if (grid[i][j]!=null) 
					surface.fillShape(new ShapeBuilder()
					.drawArc(grid[i][j])
					.build());
			}
		surface.setFillStyle(cellColors[1][1]);
		surface.fillShape(new ShapeBuilder()
		.drawCircle(new Circle(cX, cY, r/2))
		.build());
	}
	
	/**
	 * Draw a centered circular grid. The angle 0 is directed to the top.
	 * @param surface The surface to draw on.
	 * @param cellColors Colors used for the cells. 'colors[i][j]' is used to color the cell at column 'i' and line 'j'.
	 */
	public void drawCircularGrid(Surface surface, Color[][] cellColors) {
		int width = surface.getWidth();
		int height = surface.getHeight();
		int columnCount = cellColors.length;
		int lineCount = cellColors[0].length;
		if (Math.abs(columnCount-lineCount) > 1)
			throw new IllegalArgumentException("The size of the matrix is illegal.");
		boolean verticalCut = SecaMath.isEven(columnCount);
		boolean horizontalCut = SecaMath.isEven(lineCount);
		int cutCount = ((verticalCut)? 1 : 0) + ((horizontalCut)? 1 : 0);
		int ringCount = (int) Math.ceil(columnCount/2f);
		//Build the grid
		int cX = width/2;
		int cY = height/2;
		float rMax = .9f*Math.min(cX, cY); //The maximal radius
		int oDeg = -90; //The offset angle in degrees (0 is the x-axis)
		float hDeg; //Half the angle of one span in degrees.
		Arc[][] grid = new Arc[columnCount][lineCount]; //The arcs are clockwise
		int gridCenterX = (columnCount-1)/2; //First cell of the center
		int gridCenterY = (lineCount-1)/2;
		for(int i=0; i<ringCount; i++) { 
			//Loop on the rings
			float r = rMax*(i+1)/ringCount; //The radius
			int spanCount;
			if (i > 0) {
				//This is not the central ring
				spanCount = 8*i + 2*cutCount;
				hDeg = 180f/spanCount;
				//Arcs are built clockwise
				float cDeg = -45; //Angle of the current span's center
				int gridX = gridCenterX-i;
				int gridY = gridCenterY-i;
				for(int j=0; j<spanCount; j++) {
					//Loop on the spans of the current ring
					grid[gridX][gridY] = new Arc(cX, cY, r, toRad(oDeg+cDeg+hDeg), toRad(oDeg+cDeg-hDeg));
					//Update the current angle and the current position in the grid
					if (cDeg>=-45 && cDeg<44.99)
						gridX++;
					else if (cDeg>=45 && cDeg<134.99)
						gridY++;
					else if (cDeg>=135 && cDeg<224.99)
						gridX--;
					else
						gridY--;
					cDeg += 2*hDeg;
				}
			} else {
				//This is the central ring
				spanCount = (int) Math.pow(2, cutCount);
				hDeg = 180f/spanCount;
				if (spanCount == 1) {
					//The central ring is a circle
					grid[gridCenterX][gridCenterY] = null;
				} else if (spanCount == 2) {
					//The central ring is cut in 2 spans
					if (verticalCut) {
						//The cut is vertical
						// /!\ Angles in the arcs are clockwise
						grid[gridCenterX][gridCenterY] = new Arc(cX, cY, r, toRad(oDeg-90+hDeg), toRad(oDeg-90-hDeg));
						grid[gridCenterX+1][gridCenterY] = new Arc(cX, cY, r, toRad(oDeg+90+hDeg), toRad(oDeg+90-hDeg));
					} else {
						//The cut is horizontal
						grid[gridCenterX][gridCenterY] = new Arc(cX, cY, r, toRad(oDeg+0+hDeg), toRad(oDeg+0-hDeg));
						grid[gridCenterX][gridCenterY+1] = new Arc(cX, cY, r, toRad(oDeg+180+hDeg), toRad(oDeg+180-hDeg));
					}
				} else {
					//The central ring is cut in 4 spans
					assert(spanCount==4):"Illegal number of spans in the circular grid: "+spanCount;
					grid[gridCenterX][gridCenterY] = new Arc(cX, cY, r, toRad(oDeg-45+hDeg), toRad(oDeg-45-hDeg));
					grid[gridCenterX+1][gridCenterY] = new Arc(cX, cY, r, toRad(oDeg+45+hDeg), toRad(oDeg+45-hDeg));
					grid[gridCenterX][gridCenterY+1] = new Arc(cX, cY, r, toRad(oDeg-135+hDeg), toRad(oDeg-135-hDeg));
					grid[gridCenterX+1][gridCenterY+1] = new Arc(cX, cY, r, toRad(oDeg+135+hDeg), toRad(oDeg+135-hDeg));
				}
			}
		}
		
		//Draw the grid
		//Rings must be drawn from the outside to the inside
		int i = 0;
		int j = 0;
		for(int k=0; k<ringCount; ) {
			surface.setFillStyle(cellColors[i][j]);
			if (grid[i][j]!=null) 
				surface.fillShape(new ShapeBuilder()
				.drawArc(grid[i][j])
				.build());
			if (i<columnCount-1-k && j==k)
				i++;
			else if (i==columnCount-1-k && j<lineCount-1-k)
				j++;
			else if (i>k && j==lineCount-1-k)
				i--;
			else if (i==k && j>k)
				j--;
			if (i==j && i==k) {
				i++; j++; k++;
			}
		}
		if (!verticalCut && !horizontalCut) {
			//Draw the central ring as a circle
			surface.setFillStyle(cellColors[gridCenterX][gridCenterY]);
			surface.fillShape(new ShapeBuilder()
			.drawCircle(new Circle(cX, cY, rMax/ringCount))
			.build());
		}
	}
	
	/**
	 * Draw a centered circular grid. The angle 0 is directed to the top.
	 * @param surface The surface to draw on.
	 * @param cellColors Colors used for the cells.
	 */
	public void drawCircularGrid(Surface surface, CircularGrid<Color> cellColors) {
		int width = surface.getWidth();
		int height = surface.getHeight();
		//Build the grid
		int cX = width/2;
		int cY = height/2;
		float rMax = .9f*Math.min(cX, cY); //The maximal radius
		int oDeg = -90; //The offset angle in degrees (0 is the x-axis)
		CircularGrid<Arc> grid = new CircularGrid<Arc>();
		grid.copyStructure(cellColors);
		CircularGridIterator<Arc> gridIterator = grid.iteratorOverGrid();
		while (gridIterator.hasNext()) {
			gridIterator.next();
			if (!gridIterator.isCentralCircle()) {
				float r = rMax*gridIterator.getMaxRadius(); //The radius
				float minDeg = gridIterator.getMinAngle();
				float maxDeg = gridIterator.getMaxAngle();
				Arc arc = new Arc(cX, cY, r, toRad(oDeg+maxDeg), toRad(oDeg+minDeg));
				gridIterator.set(arc);
			}
		}
		
		//Draw the grid
		//Rings must be drawn from the outside to the inside
		gridIterator = grid.iteratorOverGrid();
		CircularGridIterator<Color> colorIterator = cellColors.iteratorOverGrid();
		while (gridIterator.hasPrevious()) {
			Arc arc = gridIterator.previous();
			Color color = colorIterator.previous();
			surface.setFillStyle(color);
			if (arc != null) {
				surface.fillShape(new ShapeBuilder()
				.drawArc(arc)
				.build());
			}
		}
		if (grid.hasCentralCircle()) {
			//Draw the central ring as a circle
			surface.setFillStyle(cellColors.get(0, 0));
			surface.fillShape(new ShapeBuilder()
			.drawCircle(new Circle(cX, cY, rMax*grid.getMaxRadius(0)))
			.build());
		}
	}
	
	private void createContainerWidget(Panel parentPanel, String title, final Widget widget, String styleName, boolean horizontal) {
		Panel container = (horizontal) ? new HorizontalPanel() : new VerticalPanel();
		container.setStyleName(styleName);
		if (widget.getStylePrimaryName() == null || widget.getStylePrimaryName().isEmpty())
			widget.setStyleName(styleName+"Content");
		if (title!=null && !title.isEmpty()) {
			Label label = new Label(title);
			label.setStylePrimaryName(styleName+"Title");
			container.add(label);
			//Add a mouse click handler to switch the visibility of the attached widget
			label.addClickHandler(new ClickHandler () {
				@Override
				public void onClick(ClickEvent event) {
					widget.setVisible(!widget.isVisible());
				} 
			});
		}
		container.add(widget);
		if (parentPanel!=null)
			parentPanel.add(container);
		else
			mDashboardPanel.add(container);
	}
	private void createContainerWidget(Panel parentPanel, String title, final Widget widget, boolean horizontal) {
		createContainerWidget(parentPanel, title, widget, "seca-DashboardContainer", horizontal);
	}
	private void createContainerWidget(Panel parentPanel, String title, final Widget widget, String styleName) {
		createContainerWidget(parentPanel, title, widget, styleName, false);
	}
	private void createContainerWidget(Panel parentPanel, String title, final Widget widget) {
		createContainerWidget(parentPanel, title, widget, "seca-DashboardContainer", false);
	}
	
	//Quick toRadians method
	private float toRad(double degrees) {
		return (float) Math.toRadians(degrees);
	}
	
	private RootPanel mDashboardPanel;
	private int mDefaultSurfaceWidth;
	private int mDefaultSurfaceHeight;
}
