package Model;

import IO.MyDecompressorInputStream;
import Server.*;
import Client.*;
import algorithms.mazeGenerators.Maze;
import algorithms.*;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;
import test.RunCommunicateWithServers;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyModel extends Observable implements IModel{
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private int[][] maze;
    private Maze mazeM;
    private Server mazeGeneratingServer;
    private Server mazeSolutionServer;
    private ArrayList<int[]> arraySol;
    public MyModel() {
        startServers();
    }

    public void startServers() {
        if (mazeGeneratingServer == null) {
            mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
            mazeGeneratingServer.start();
        }
    }

    public void stopServers() {
        mazeGeneratingServer.stop();
    }

    private int characterPositionRow = 1;
    private int characterPositionColumn = 1;
    private int endPositionRow = 1;
    private int endPositionColumn = 1;

    public int getEndPositionRow() {
        return endPositionRow;
    }

    public int getEndPositionColumn() {
        return endPositionColumn;
    }

    @Override
    public void generateMaze(int width, int height) {
        //Generate maze
        threadPool.execute(() -> {
            //generateRandomMaze(width,height);
            CommunicateWithServer_MazeGenerating(width, height);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setChanged();
            notifyObservers();
        });
    }
    public void startSolve(){
        if (mazeSolutionServer == null) {
            mazeSolutionServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
            mazeSolutionServer.start();
        }
    }
    public void solveMaze(){
        startSolve();
        threadPool.execute(() -> {
            //generateRandomMaze(width,height);
            CommunicateWithServer_SolveSearchProblem();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setChanged();
            notifyObservers();
        });
    }

    @Override
    public ArrayList<int[]> getSolution() {
        return arraySol;
    }

    private void CommunicateWithServer_MazeGenerating(int rows, int columns) {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rows, columns};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) ((byte[]) fromServer.readObject());
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[rows*columns+30];
                        is.read(decompressedMaze);
                        Maze maze = new Maze(decompressedMaze);

                        MyModel.this.maze = (maze.getMazeC());
                        mazeM = maze;
                        characterPositionRow = mazeM.getStartPosition().getRowIndex();
                        characterPositionColumn = mazeM.getStartPosition().getColumnIndex();
                        endPositionRow = mazeM.getGoalPosition().getRowIndex();
                        endPositionColumn = mazeM.getGoalPosition().getColumnIndex();


                    } catch (Exception var10) {
                        var10.printStackTrace();
                    }

                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException var1) {
            var1.printStackTrace();
        }
    }

    public void CommunicateWithServer_SolveSearchProblem() {
        arraySol = new ArrayList<>();
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();

                      //  MyMazeGenerator mg = new MyMazeGenerator();
                       // Maze maze = mg.generate(50, 50);

                        System.out.println(mazeM.getStartPosition() + " , " + mazeM.getGoalPosition());
                        toServer.writeObject(mazeM);
                        toServer.flush();
                        Solution mazeSolution = (Solution)fromServer.readObject();
                        System.out.println(String.format("Solution steps: %s", mazeSolution));
                        ArrayList<AState> mazeSolutionSteps = mazeSolution.getSolutionPath();

                        for(int i = 0; i < mazeSolutionSteps.size(); ++i) {
                            System.out.println(String.format("%s. %s", i, ((AState)mazeSolutionSteps.get(i)).toString()));
                            MazeState mazeS = (MazeState)mazeSolutionSteps.get(i);
                            int []temp = {mazeS.getPosition().getRowIndex(),mazeS.getPosition().getColumnIndex()};
                            arraySol.add(temp);
                        }

                    } catch (Exception var10) {
                        var10.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException var1) {
            var1.printStackTrace();
        }
    }

    @Override
    public int[][] getMaze() {
        return maze;
    }

    @Override
    public void moveCharacter(KeyCode movement) {
        switch (movement) {
            case NUMPAD8:
                if(characterPositionRow-1>=0 && characterPositionRow-1<maze.length && maze[characterPositionRow-1][characterPositionColumn] == 0){
                    characterPositionRow--;
                }
                //else *sound on*
                break;
            case NUMPAD2:
                if(characterPositionRow+1>=0 && characterPositionRow+1<maze.length && maze[characterPositionRow+1][characterPositionColumn] == 0){
                    characterPositionRow++;
                }
                break;
            case NUMPAD6:
                if(characterPositionColumn+1>=0 && characterPositionColumn+1<maze[characterPositionRow].length && maze[characterPositionRow][characterPositionColumn+1] == 0){
                    characterPositionColumn++;
                }
                break;
            case NUMPAD4:
                if(characterPositionColumn-1>=0 && characterPositionColumn-1<maze[characterPositionRow].length && maze[characterPositionRow][characterPositionColumn-1] == 0){
                    characterPositionColumn--;
                }
                break;
            case NUMPAD1:
                if(characterPositionColumn-1>=0 && characterPositionColumn-1<maze[characterPositionRow].length &&
                        characterPositionRow+1>=0 && characterPositionRow+1<maze.length && maze[characterPositionRow+1][characterPositionColumn-1] == 0) {
                    characterPositionRow++;
                    characterPositionColumn--;
                }
                break;
            case NUMPAD3:
                if(characterPositionColumn+1>=0 && characterPositionColumn+1<maze[characterPositionRow].length &&
                        characterPositionRow+1>=0 && characterPositionRow+1<maze.length && maze[characterPositionRow+1][characterPositionColumn+1] == 0) {
                    characterPositionRow++;
                    characterPositionColumn++;
                }
                break;
            case NUMPAD9:
                if(characterPositionColumn+1>=0 && characterPositionColumn+1<maze[characterPositionRow].length &&
                        characterPositionRow-1>=0 && characterPositionRow-1<maze.length && maze[characterPositionRow-1][characterPositionColumn+1] == 0) {
                    characterPositionRow--;
                    characterPositionColumn++;
                }
                break;
            case NUMPAD7:
                if(characterPositionColumn-1>=0 && characterPositionColumn-1<maze[characterPositionRow].length &&
                        characterPositionRow-1>=0 && characterPositionRow-1<maze.length && maze[characterPositionRow-1][characterPositionColumn-1] == 0) {
                    characterPositionRow--;
                    characterPositionColumn--;
                }
                break;

        }
        setChanged();
        notifyObservers();
    }

    @Override
    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    @Override
    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }
}