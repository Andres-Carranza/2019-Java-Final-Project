/*
 * Programmer: Andres Carranza
 * Date: 5/16/2019
 *
 * CLASS DETAILS:
 	- This class calculates the width of a text
 */
import javafx.scene.text.*;

public class FontWidth{
	private Text internal;//Text object
	
	//Constructor
	public FontWidth(Font fnt){
		internal = new Text();
		internal.setFont(fnt);
	}

	//Calculates text width
	public float computeStringWidth(String txt)
	{
		internal.setText(txt);
		return (float) internal.getLayoutBounds().getWidth();
	}
	
}