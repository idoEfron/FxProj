package ViewModel;

import Model.IModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel  extends Observable implements Observer {
    private IModel model;

    private int characterPositionRowIndex;
    private int characterPositionColumnIndex;
    private int endPositionRow;

    public int getEndPositionRow() {
        return endPositionRow;
    }

    public int getEndPositionColumn() {
        return endPositionColumn;
    }

    private int endPositionColumn;

    public StringProperty characterPositionRow = new SimpleStringProperty("1"); //For Binding
    public StringProperty characterPositionColumn = new SimpleStringProperty("1"); //For Binding

    public MyViewModel(IModel model){
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            characterPositionRowIndex = model.getCharacterPositionRow();
            characterPositionRow.set(characterPositionRowIndex + "");
            characterPositionColumnIndex = model.getCharacterPositionColumn();
            characterPositionColumn.set(characterPositionColumnIndex + "");
            endPositionRow = model.getEndPositionRow();
            endPositionColumn = model.getEndPositionColumn();
            setChanged();
            notifyObservers();
        }
    }

    public void generateMaze(int width, int height){
        model.generateMaze(width, height);
    }

    public void moveCharacter(KeyCode movement){
        model.moveCharacter(movement);
    }

    public int[][] getMaze() {
        return model.getMaze();
    }

    public int getCharacterPositionRow() {
        return characterPositionRowIndex;
    }

    public int getCharacterPositionColumn() {
        return characterPositionColumnIndex;
    }

    public ArrayList<int[]> getsolution(){
        return model.getSolution();
    }
    public void solveMaze() throws InterruptedException {
        model.solveMaze();
    }

}
