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

public class TaskStyle extends DefaultStyleOGL2D {
	private RepastEssentials re = new RepastEssentials();
	EditedStyleData<Object> style = new DefaultEditedStyleData2D<Object>();
	Font font;
	@Override
	public Color getColor(Object o) {
		if (o instanceof Task) {
			Task a = (Task) o;
			return Color.RED;
		} else {
			return Color.BLACK;
		}
	}

	
	@Override
	public Font getLabelFont(Object agent){
		font = new Font(style.getLabelFontFamily(), 
		        style.getLabelFontType(),
		        style.getLabelFontSize());
		return font;
	}
	@Override
	public String getLabel(Object agent) {
		style.setLabel("teste");
		return EditedStyleUtils.getLabel(style, agent);
	}
	
}