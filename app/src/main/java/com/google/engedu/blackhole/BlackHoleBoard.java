package com.google.engedu.blackhole;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/* Class that represent the state of the game.
 * Note that the buttons on screen are not updated by this class.
 */
public class
BlackHoleBoard {
    // The number of turns each player will take.
    public final static int NUM_TURNS = 10;
    // Size of the game board. Each player needs to take 10 turns and leave one empty tile.
    public final static int BOARD_SIZE = NUM_TURNS * 2 + 1;
    // Relative position of the neighbors of each tile. This is a little tricky because of the
    // triangular shape of the board.
    public final static int[][] NEIGHBORS = {{-1, -1}, {0, -1}, {-1, 0}, {1, 0}, {0, 1}, {1, 1}};
    // When we get to the Monte Carlo method, this will be the number of games to simulate.
    private static final int NUM_GAMES_TO_SIMULATE = 2000;
    // The tiles for this board.
    private BlackHoleTile[] tiles;
    // The number of the current player. 0 for user, 1 for computer.
    private int currentPlayer;
    // The value to assign to the next move of each player.
    private int[] nextMove = {1, 1};
    // A single random object that we'll reuse for all our random number needs.
    private static final Random random = new Random();


    // Constructor. Nothing to see here.
    BlackHoleBoard() {
        tiles = new BlackHoleTile[BOARD_SIZE];

        reset();
    }

    // Copy board state from another board. Usually you would use a copy constructor instead but
    // object allocation is expensive on Android so we'll reuse a board instead.
    public void copyBoardState(BlackHoleBoard other) {
        this.tiles = other.tiles.clone();
        this.currentPlayer = other.currentPlayer;
        this.nextMove = other.nextMove.clone();
    }

    // Reset this board to its default state.
    public void reset() {
        currentPlayer = 0;
        nextMove[0] = 1;
        nextMove[1] = 1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            tiles[i] = null;
        }

    }

    // Translates column and row coordinates to a location in the array that we use to store the
    // board.
    protected int coordsToIndex(int col, int row) {
        return col + row * (row + 1) / 2;
    }

    // This is the inverse of the method above.
    protected Coordinates indexToCoords(int i) {
        Coordinates result =new Coordinates(0,0);
        // TODO: Compute the column and row number for the ith location in the array.
        // The row number is the triangular root of i as explained in wikipedia:
        // https://en.wikipedia.org/wiki/Triangular_number#Triangular_roots_and_tests_for_triangular_numbers
        // The column number is i - (the number of tiles in all the previous rows).
        // This is tricky to compute correctly so use the unit test in BlackHoleBoardTest to get it
        // right.

        result.x= 0;
        if((int)(Math.sqrt(8*i+1)-1)/2>=0)
            result.y=(int)((Math.sqrt(8*i+1)-1)/2);

        int temp=i,no=1;
        while(true)
        {
            temp=temp-no;
            no++;
            if(temp-no<0) break;

        }

        result.x=temp;
        return result;
    }

    // Getter for the number of the player's next move.
    public int getCurrentPlayerValue() {
        return nextMove[currentPlayer];
    }

    // Getter for the number of the current player.
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    // Check whether the current game is over (only one blank tile).
    public boolean gameOver() {
        int empty = -1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (tiles[i] == null) {
                if (empty == -1) {
                    empty = i;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    // Pick a random valid move on the board. Returns the array index of the position to play.
    public int pickRandomMove() {
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (tiles[i] == null) {
                possibleMoves.add(i);
            }
        }
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    // Pick a good move for the computer to make. Returns the array index of the position to play.
    public int pickMove() {
        // TODO: Implement this method have the computer make a move.
         BlackHoleBoard obj=new BlackHoleBoard();

        int comp_pos,user_pos,first_move=0,best_move=pickRandomMove();
        float avg=0,min=1000;
        HashMap<Integer,ArrayList<Integer>> monte_carlo=new HashMap<Integer, ArrayList<Integer>>();
       ArrayList<Integer> n=new ArrayList<>();
      /* for(int i=0;i<21;++i)
        {
            monte_carlo.put(i,n);}*/

        for(int count=0;count<NUM_GAMES_TO_SIMULATE;++count) {
            obj.copyBoardState(this);
            first_move = obj.pickRandomMove();
            //Log.i("first move",first_move+" ");
            obj.setValue(first_move);
            while (!obj.gameOver()) {
                user_pos = obj.pickRandomMove();
                obj.setValue(user_pos);
                comp_pos = obj.pickRandomMove();
                obj.setValue(comp_pos);

            }

          if(monte_carlo.get(first_move)==null)
          {
           monte_carlo.put(first_move,new ArrayList<Integer>());
          }
            monte_carlo.get(first_move).add(obj.getScore());
          //  Log.i("array list",monte_carlo.get(first_move)+"");

        }
        ArrayList<Integer> possibleMoves = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            if (tiles[i] == null) {
                possibleMoves.add(i);
            }
        }

        for(Integer i :possibleMoves) {
            float sum=0;
            if(monte_carlo.get(i)!=null) {
                for (Integer j : monte_carlo.get(i)) {
                    sum += j;
                }
                //Log.i(i + " ", sum + " " + monte_carlo.get(i).size() + " ");
                sum = sum / monte_carlo.get(i).size();
                if (sum < min) {
                    min = sum;
                    best_move = i;

                }
            }

        }

       Log.i("move",best_move+"");
        Log.i("score",min+"");
        return best_move;
    }

    // Makes the next move on the board at position i. Automatically updates the current player.
    public void setValue(int i) {
        tiles[i] = new BlackHoleTile(currentPlayer, nextMove[currentPlayer]);
        nextMove[currentPlayer]++;
        currentPlayer++;
        currentPlayer %= 2;
    }

    /* If the game is over, computes the score for the current board by adding up the values of
     * all the tiles that surround the empty tile.
     * Otherwise, returns 0.
     */
    public int getScore() {
        int score = 0;
        // TODO: Implement this method to compute the final score for a given board.
        // Find the empty tile left on the board then add/substract the values of all the
        // surrounding tiles depending on who the tile belongs to.
        for(int i=0;i<BOARD_SIZE;++i)
            if(tiles[i]==null)
            {

               // Log.i("Index working",indexToCoords(i).x+" "+indexToCoords(i).y+" ");
               ArrayList<BlackHoleTile> result=getNeighbors(indexToCoords(i));
                for(BlackHoleTile b:result)
                {
                    if(b.player==0)
                        score-=b.value;
                    else
                        score+=b.value;
                 }

            }


        return score;
    }

    // Helper for getScore that finds all the tiles around the given coordinates.
    private ArrayList<BlackHoleTile> getNeighbors(Coordinates coords) {
        ArrayList<BlackHoleTile> result = new ArrayList<>();
        for(int[] pair : NEIGHBORS) {
            BlackHoleTile n = safeGetTile(coords.x + pair[0], coords.y + pair[1]);
            if (n != null) {
                result.add(n);
            }
        }
        return result;
    }

    // Helper for getNeighbors that gets a tile at the given column and row but protects against
    // array over/underflow.
    private BlackHoleTile safeGetTile(int col, int row) {
        if (row < 0 || col < 0 || col > row) {
            return null;
        }
        int index = coordsToIndex(col, row);
        if (index >= BOARD_SIZE) {
            return null;
        }
        return tiles[index];
    }
}
