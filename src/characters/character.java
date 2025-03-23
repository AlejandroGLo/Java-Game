package characters;
import moves.*;
import java.util.List;
import java.util.ArrayList;

public abstract class Character {
    protected String name;
    protected int health;
    protected List<Move> moves;

    public Character(String name, int health){
        this.name = name;
        this.health = health;
        this.moves = new ArrayList<>();
    }

    public void addMove(Move move){
        if(moves.size() <4){
            moves.add(move);
        } else {
            System.out.println(name + " already has the maximum number of moves");
        }
    }

    public void attack(Character target, Move move){
        System.out.println(name + " used "+ move.getName() + " on "+ target.getName());

    }

    public void takeDamage(int damage){
        health = health - damage;
        if(health < 0) {
            health = 0;
        }

    }
    public boolean isAlive(){
        return health >0;
    }

    public String getName(){
        return name;
    }
    
}
