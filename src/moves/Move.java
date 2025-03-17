package moves;

public class Move {
    private String name;
    private int damage;

    public Move(String name, int damage){
        this.name = name;
        this.damage = damage;
    }

    public String getName(){
        return name;
    }
    public int getDamage(){
        return damage;
    }
    
}
