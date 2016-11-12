package trustMetrics;

import java.awt.Color; 
import java.awt.Paint; 
import java.awt.Stroke; 
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position; 

public class TaskStyle extends DefaultStyleOGL2D 
{ 
	 @Override
      public Color getColor(Object o)
      {
          if (o instanceof Task)
          {
             Task a = (Task) o;
              
              return Color.RED;
          }
          else
          {
              return Color.BLACK;
          }
      }
	 @Override
	 public String getLabel(final Object agent) {
		String str = "test";
		return str;
	 }
} 