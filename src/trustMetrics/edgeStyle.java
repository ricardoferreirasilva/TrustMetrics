package trustMetrics;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class edgeStyle extends DefaultEdgeStyleOGL2D{
	
	@Override
	public Color getColor(RepastEdge edge){
		if(edge.getSource() instanceof Worker){
			return Color.GRAY;
		}
		else
			return Color.GREEN;
	}

	
}
