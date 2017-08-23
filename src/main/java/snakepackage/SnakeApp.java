package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author jd-
 *
 */
public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2,
        3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
        GridSize.GRID_HEIGHT - 2)};
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    
    private JButton start;
    private JButton pause;
    private JButton resumen;
    private JTextArea maximoActual;
    private JTextArea primeroMorir;
    private boolean startGame;
    public static Object obj = new Object();
    private String waiting = Thread.State.WAITING.toString();
    private int maximo;
    private int minimo;
    

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();
        
        
        frame.add(board,BorderLayout.CENTER);
        
        JPanel actionsBPabel=new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        
        start = new JButton("Start");
        pause = new JButton("Pause");
        resumen = new JButton("Resume");
        maximoActual = new JTextArea("Maximo Actual "+maximo);
        primeroMorir = new JTextArea("Primero en morir "+minimo);
        actionsBPabel.add(maximoActual);
        actionsBPabel.add(start);
        actionsBPabel.add(pause);
        actionsBPabel.add(resumen);
        actionsBPabel.add(primeroMorir);
        frame.add(actionsBPabel,BorderLayout.SOUTH);
        startGame = false;
        maximo = Integer.MIN_VALUE;
        minimo = Integer.MAX_VALUE;
        prepareActions();
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {
        
        
        
        for (int i = 0; i != MAX_THREADS; i++) {
            
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            //thread[i].start();
        }

        frame.setVisible(true);
        
        synchronized(obj){
            try{
                obj.wait();
            }catch(InterruptedException e){}
        }

        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++) {
            System.out.println("["+i+"] :"+thread[i].getState());
        }
        

    }

    public static SnakeApp getApp() {
        return app;
    }
    
    public void prepareActions(){
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(!startGame){
                    start();
                }startGame = true;
            }
        });
        
        pause.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pause();
            }
        });
        
        resumen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resume();
            }
        });
    }
    
    public void start(){
        for(int i = 0; i < MAX_THREADS; i++){
            thread[i].start();
        }
    }
    
    public void pause(){
        for(int i = 0; i < MAX_THREADS; i++){
            snakes[i].lock();
        }int n = 0;
        while(n<MAX_THREADS){
            if(thread[n].getState().toString().equals(waiting)){
                if(snakes[n].getBody().size()>maximo){
                    maximo = snakes[n].getBody().size();
                }n++;
            }else if(snakes[n].isSnakeEnd()){
                if(snakes[n].getBody().size()<minimo){
                    minimo = snakes[n].getBody().size();
                }n++;
            }
        }
        maximoActual.setText("Maximo Actual "+maximo);
        primeroMorir.setText("Primero en morir "+minimo);
    }
    
    public void resume(){
        for(int i = 0; i < MAX_THREADS; i++){
            snakes[i].unlock();
        }synchronized(obj){
            obj.notifyAll();
        }
    }

}
