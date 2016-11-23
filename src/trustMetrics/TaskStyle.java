package trustMetrics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.piccolo2d.nodes.PText;

import repast.simphony.essentials.RepastEssentials;
import repast.simphony.visualization.editedStyle.DefaultEditedStyleData2D;
import repast.simphony.visualization.editedStyle.EditedStyleData;
import repast.simphony.visualization.editedStyle.EditedStyleUtils;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;
import saf.v3d.scene.VSpatial;
import repast.simphony.space.graph.RepastEdge;

public class TaskStyle extends DefaultStyleOGL2D {
	private RepastEssentials re = new RepastEssentials();
	EditedStyleData<Object> style = new DefaultEditedStyleData2D<Object>();
	Font font;

	@Override
	public Color getColor(Object o) {
		if (o instanceof Task) {
			Task a = (Task) o;
			if (a.getFinished())
				return Color.GREEN;
			if (a.getAvailable())
				return Color.YELLOW;
			else
				return Color.RED;
		}
		/*else if(o instanceof RepastEdge){
			return Color.GRAY;
		}*/else {
			return Color.BLACK;
		}
		
		
	}

	@Override
	public Font getLabelFont(Object agent) {
		font = new Font(style.getLabelFontFamily(), style.getLabelFontType(), style.getLabelFontSize());
		return font;
	}

	@Override
	public String getLabel(Object agent) {
		if (agent instanceof Task) {
			Task a = (Task) agent;
			style.setLabel(a.toString());
		} else {
			style.setLabel("unknown");
		}
		return EditedStyleUtils.getLabel(style, agent);
	}

	@Override
	public float getScale(Object agent) {
		return 5;
	}

	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		if (spatial == null) {
			spatial = shapeFactory.createRectangle(4, 4, true);
		}
		return spatial;
	}

}