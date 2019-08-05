package cs3500.controller;

import cs3500.controller.keyframelistener.KeyframeListener;
import cs3500.hw05.animatorobject.ReadOnlyAnimatorObject;
import cs3500.hw05.model.AnimatorModel;
import cs3500.view.EditView;
import cs3500.view.IEditView;
import cs3500.view.IView;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class represents an implementation of the AnimatorController interface. Specifically, it
 * implements the public methods run and initializeFields. This Implementation stores the input as a
 * FileReader, and the output as an appendable. When asked to initialize fields, it utilizes a
 * factory pattern for the view, and then a builder for the model. Its default constructor sets
 * default values for the fields.
 */
public class AnimatorControllerImpl implements EventListener, AnimatorController, ActionListener,
    ItemListener, ListSelectionListener {

  private IView view;
  private IEditView editView;
  private int speed;
  private AnimatorModel model;
  private boolean isLooping;
  private Timer timer;
  private String currentShape;
  private KeyframeListener kfl;
  private String nextShapeType;
  private final int[] tick;

  /**
   * This is a default constructor for this controller. When called, it creates a controller with
   * default values. Specifically, the tick speed is set to 1 tick per second, the input is left
   * unspecified, the output is default System.out, and the view and model are also left as
   * default.
   */
  public AnimatorControllerImpl() {
    this.view = null;
    this.model = null;
    tick = new int[1];
  }

  @Override
  public void run(String viewType, AnimatorModel model, IView view, int speed) {
    this.speed = speed;
    this.model = model;
    this.view = view;
    switch (viewType) {
      case "visual":
        runVisual();
        break;
      case "text":
        runText();
        break;
      case "svg":
        runSVG();
        break;
      case "edit":
        runEdit();
        break;
      default:
        break;
    }
  }

  private void runVisual() {
    final int[] tick = {1};
    IView finalView = this.view;
    AnimatorModel finalModel = this.model;
    this.view.setCanvas(this.model.getX(), this.model.getY(), this.model.getWidth(),
        this.model.getHeight());
    this.timer = new Timer(this.speed, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ArrayList<ReadOnlyAnimatorObject> shapesToRender = finalModel.tweening(tick[0]++);
        finalView.render(shapesToRender);
      }
    });
    this.timer.start();
  }

  private void runText() {
    this.view.render(this.model.getTextualList());
  }

  private void runSVG() {
    this.view.setCanvas(this.model.getX(), this.model.getY(), this.model.getWidth() +
        this.model.getX(), this.model.getHeight() + this.model.getY());
    this.view.render(this.model.getSVGShapesList(this.speed));
  }

  private void runEdit() {
    this.editView = (EditView) this.view;
    this.kfl = new KeyframeListener(editView);
    editView.setKeyframeListener(kfl);
    editView.setListeners(this);
    editView.changeSelectionList(model.getIDs());
    this.view.setCanvas(this.model.getX(), this.model.getY(), this.model.getWidth(),
        this.model.getHeight());
    this.timer = new Timer(this.speed, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (isLooping && model.isAnimationDone(tick[0] + 1)) {
          tick[0] = 1;
          model.tweening(1);
        } else {
          ArrayList<ReadOnlyAnimatorObject> shapesToRender = model.tweening(tick[0]++);
          editView.render(shapesToRender);
        }

      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String arg = e.getActionCommand();
    switch (arg) {
      case "restart":
        this.tick[0] = 1;
        break;
      case "rectangle":
        this.nextShapeType = "rectangle";
        break;
      case "ellipse":
        this.nextShapeType = "ellipse";
        break;
      case "start button":
        timer.start();
        break;
      case "pause button":
        timer.stop();
        break;
      case "speed up":
        timer.setDelay(Math.max(1, timer.getDelay() / 2));
        break;
      case "slow down":
        timer.setDelay(timer.getDelay() * 2);
        break;
      case "add keyframe":
        addKeyframe();
        break;
      case "modify keyframe":
        modifyKeyframe();
        break;
      case "remove shape":
        removeShape();
        break;
      case "remove keyframe":
        removeKeyframe();
        break;
      case "add shape":
        addShape();
        break;
    }
  }

  private void addKeyframe() {
    if (this.currentShape != null) {
      String nextMotionTime = JOptionPane.showInputDialog("Please enter the Keyframe's time");
      String nextMotionX = JOptionPane.showInputDialog("Please enter the Keyframe's X value");
      String nextMotionY = JOptionPane.showInputDialog("Please enter the Keyframe's Y value");
      String nextMotionW = JOptionPane.showInputDialog("Please enter the Keyframe width");
      String nextMotionH = JOptionPane.showInputDialog("Please enter the Keyframe height");
      Color nextMotionColor = JColorChooser.showDialog(new JLabel(), "Please choose a "
              + "Keyframe color",
          Color.WHITE);
      try {
        this.model.addAction(this.currentShape, Integer.parseInt(nextMotionTime),
            Integer.parseInt(nextMotionTime), Integer.parseInt(nextMotionX),
            Integer.parseInt(nextMotionY), Integer.parseInt(nextMotionW),
            Integer.parseInt(nextMotionH), nextMotionColor.getRed(), nextMotionColor.getGreen(),
            nextMotionColor.getBlue());
      } catch (IllegalArgumentException | NullPointerException ex) {
        JOptionPane.showMessageDialog(new JPanel(), "Keyframe could not be created");
      }
      this.editView.changeDisplayedMotion(this.model.getShape(this.currentShape).getActions());
    }
  }

  private void modifyKeyframe() {
    if (this.currentShape != null && this.kfl.getCurrent() != null) {
      String nextMotionX = JOptionPane.showInputDialog("Please enter the Keyframe's X value");
      String nextMotionY = JOptionPane.showInputDialog("Please enter the Keyframe's Y value");
      String nextMotionW = JOptionPane.showInputDialog("Please enter the Keyframe width");
      String nextMotionH = JOptionPane.showInputDialog("Please enter the Keyframe height");
      Color nextMotionColor = JColorChooser.showDialog(new JLabel(), "Please choose a "
              + "Keyframe color",
          Color.WHITE);
      String[] split = this.kfl.getCurrent().split(" ");
      int time = Integer.parseInt(split[0]);
      try {
        this.model.removeMotion(this.currentShape, time);
        this.model.addAction(this.currentShape, time, time, Integer.parseInt(nextMotionX),
            Integer.parseInt(nextMotionY), Integer.parseInt(nextMotionW),
            Integer.parseInt(nextMotionH), nextMotionColor.getRed(), nextMotionColor.getGreen(),
            nextMotionColor.getBlue());
      } catch (IllegalArgumentException | NullPointerException ex) {
        this.model.addAction(this.currentShape, time, time, Integer.parseInt(split[1]),
            Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]),
            Integer.parseInt(split[5]), Integer.parseInt(split[6]), Integer.parseInt(split[7]));
        JOptionPane.showMessageDialog(new JPanel(), "Keyframe could not be changed");
      }
      this.kfl.setCurrent(null);
      this.editView.changeDisplayedMotion(this.model.getShape(this.currentShape).getActions());
    }
  }

  private void removeShape() {
    if (this.currentShape != null) {
      this.model.removeShape(currentShape);
      this.editView.changeSelectionList(model.getIDs());
      this.editView.changeDisplayedMotion(new ArrayList<>());
      this.currentShape = null;
    }
  }

  private void removeKeyframe() {
    if (this.kfl.getCurrent() != null) {
      String[] split = this.kfl.getCurrent().split(" ");
      this.model.removeMotion(this.currentShape, Integer.parseInt(split[0]));
      this.editView.changeDisplayedMotion(this.model.getShape(this.currentShape).getActions());
      this.kfl.setCurrent(null);
    }
  }

  private void addShape() {
    String nextShapeID = JOptionPane.showInputDialog("Please enter the Shape ID");
    try {
      this.model.addShape(this.nextShapeType, nextShapeID, 1, 1, 1, 1, 1, 1, 1);
    } catch (IllegalArgumentException | NullPointerException ex) {
      JOptionPane.showMessageDialog(new JPanel(), "Shape could not be created");
    }
    this.editView.changeSelectionList(this.model.getIDs());
  }

  @Override
  public void itemStateChanged(ItemEvent arg0) {
    if (((JCheckBox) arg0.getItemSelectable()).getActionCommand().equals("isLooping")) {
      if (arg0.getStateChange() == ItemEvent.SELECTED) {
        this.isLooping = true;
      } else {
        this.isLooping = false;
      }
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    EditView v = (EditView) this.view;
    if (e.getValueIsAdjusting()) {
      this.currentShape = v.getSelectedShape();
      v.changeDisplayedMotion(this.model.getShape(this.currentShape).getActions());
    }
  }
}
