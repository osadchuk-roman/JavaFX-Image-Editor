package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

public class Controller {

	private static final FileChooser FILE_CHOOSER = new FileChooser();

	@FXML
	private ImageView imageView;

	@FXML
	private Button bntOpen;

	@FXML
	private Label labelFilename;

	@FXML
	private Slider lightSlider;

	@FXML
	private Button btnSave;

	@FXML
	private RadioButton restoreImageRB;

	@FXML
	private RadioButton blackWhite1RB;

	@FXML
	private RadioButton blackWhite2RB;

	@FXML
	private RadioButton lightChangeRB;

	private double oldR, oldG, oldB;

	private BufferedImage bufferedImage;
	private BufferedImage initialImage;

	private int[][] initialR;
	private int[][] initialG;
	private int[][] initialB;

	private String imageExtension;

	private ToggleGroup toggleGroup = new ToggleGroup();

	@FXML
	public void initialize() {
		FILE_CHOOSER.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files",
				"*.bmp", "*.BMP", "*.png", "*.PNG", "*.jpg", "*.JPG", "*.JPEG"));

		lightSlider.setDisable(true);
		restoreImageRB.setToggleGroup(toggleGroup);
		blackWhite1RB.setToggleGroup(toggleGroup);
		blackWhite2RB.setToggleGroup(toggleGroup);
		lightChangeRB.setToggleGroup(toggleGroup);
		restoreImageRB.setSelected(true);


		lightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			float offset = 2.50f * newValue.floatValue();
			RescaleOp rescale = new RescaleOp(1.0f, offset, null);
			bufferedImage = rescale.filter(initialImage, null);
			showImage();
		});

		toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
			if (toggleGroup.getSelectedToggle() != null && bufferedImage != null) {
				RadioButton radioButton = (RadioButton) toggleGroup.getSelectedToggle();
				if (radioButton.getText().equals(restoreImageRB.getText())) {
					lightSlider.setDisable(true);
					restoreImage();
				} else if (radioButton.getText().equals(blackWhite1RB.getText())) {
					lightSlider.setDisable(true);
					makeImageBlackAndWhite1();
				} else if (radioButton.getText().equals(blackWhite2RB.getText())) {
					lightSlider.setDisable(true);
					ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
					ColorConvertOp colorConvertOp = new ColorConvertOp(colorSpace, null);
					bufferedImage = colorConvertOp.filter(bufferedImage, null);
					showImage();
				} else if (radioButton.getText().equals(lightChangeRB.getText())) {
					restoreImage();
					lightSlider.setDisable(false);
				}
			}
		});
	}

	private void restoreImage() {
		if (bufferedImage != null) {
			lightSlider.setValue(0);
			ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
			ColorConvertOp colorConvertOp = new ColorConvertOp(colorSpace, null);
			bufferedImage = colorConvertOp.filter(bufferedImage, null);
			int width = bufferedImage.getWidth();
			int height = bufferedImage.getHeight();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color newColor = new Color(initialR[j][i], initialG[j][i], initialB[j][i]);
					bufferedImage.setRGB(j, i, newColor.getRGB());
				}
			}
			showImage();
		}
	}


	private void makeImageBlackAndWhite1() {
		ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
		ColorConvertOp colorConvertOp = new ColorConvertOp(colorSpace, null);
		bufferedImage = colorConvertOp.filter(bufferedImage, null);
		if (bufferedImage != null) {
			int width = bufferedImage.getWidth();
			int height = bufferedImage.getHeight();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color c = new Color(bufferedImage.getRGB(j, i));
					int red = (int) (c.getRed() * 0.299);
					int green = (int) (c.getGreen() * 0.587);
					int blue = (int) (c.getBlue() * 0.114);
					Color newColor = new Color(red + green + blue,
							red + green + blue, red + green + blue);
					bufferedImage.setRGB(j, i, newColor.getRGB());
				}
			}
			showImage();
		}
	}

	private void showImage() {
		if (bufferedImage != null) {
			imageView.setImage(null);
			imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
		}
	}

	private void initRGB() {
		if (bufferedImage != null) {
			int width = bufferedImage.getWidth();
			int height = bufferedImage.getHeight();
			initialR = new int[width][height];
			initialG = new int[width][height];
			initialB = new int[width][height];

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color color = new Color(bufferedImage.getRGB(j, i));
					initialR[j][i] = color.getRed();
					initialG[j][i] = color.getGreen();
					initialB[j][i] = color.getBlue();
				}
			}
		}
	}

	@FXML
	void onClickBtnOpen(ActionEvent event) {
		File file = FILE_CHOOSER.showOpenDialog(bntOpen.getScene().getWindow());
		if (file != null) {
			String filename = file.getName();
			imageExtension = filename.substring(filename.lastIndexOf('.') + 1);
			labelFilename.setText(filename);
			try {
				bufferedImage = ImageIO.read(file);
				initialImage = ImageIO.read(file);
				initRGB();
				restoreImageRB.setSelected(true);
				showImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	void onClickBtnSave(ActionEvent event) {
		if (bufferedImage != null) {
			FileChooser fileSaver = new FileChooser();
			fileSaver.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*." + imageExtension));
			fileSaver.setInitialFileName("*." + imageExtension);
			File file = fileSaver.showSaveDialog(btnSave.getScene().getWindow());
			if (file != null) {
				try {
					ImageIO.write(bufferedImage, imageExtension, file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
