/*
 * Andres Carranza
 * 5/28/2019
 * Class provides useful tools
 */

import java.io.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;

public class LocalToolkit {
	
	//Scales an image
	public static ImageView scale(WritableImage source, int targetWidth, int targetHeight, boolean preserveRatio) {
		ImageView imageView = new ImageView(source);
		imageView.setPreserveRatio(preserveRatio);
		imageView.setFitWidth(targetWidth);
		imageView.setFitHeight(targetHeight);
		return imageView;
	}
	
	//Useful especially for buttons
	public static void addGlowEffect(Node n) {
		//Adding an effect so that when a mouse hovers above the button, it glows
		n.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				Glow glow = new Glow();
				glow.setLevel(0.5); 
				n.setEffect(glow);
			}
		});

		//Reseting button to default once the mouse has exited
		n.addEventHandler(MouseEvent.MOUSE_EXITED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				n.setEffect(null);
			}
		});
	}
	
	//Useful especially for buttons
	public static void addGlowEffect(Node n, double level) {
		//Adding an effect so that when a mouse hovers above the button, it glows
		n.addEventHandler(MouseEvent.MOUSE_ENTERED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				Glow glow = new Glow();
				glow.setLevel(level); 
				n.setEffect(glow);
			}
		});

		//Reseting button to default once the mouse has exited
		n.addEventHandler(MouseEvent.MOUSE_EXITED,
				new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				n.setEffect(null);
			}
		});
	}
	
	//Used to create an ImageView from an image located at filepath scaled to width and height
	public static ImageView loadImg(String filepath, int width, int height)
	{
		FileInputStream inputstream = null;
		try {
			inputstream = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ImageView img = new ImageView( new Image(inputstream, width, height, false, false)); 

		return img;
	}
	
	//Loads an image
	public static Image loadImg(String filepath)
	{
		FileInputStream inputstream = null;
		try {
			inputstream = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new Image(inputstream); 
	}
}
