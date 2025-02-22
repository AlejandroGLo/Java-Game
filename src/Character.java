

public class Character {
    private String name;
    private int hp;
    private int speed;
    private int defense;
    private int attack;
    private Attack[] moves;

    // Constructors
    public Character(String name, int hp, int speed, int defense, int attack, Attack[] moves){
        this.name = name;
        this.hp = hp;
        this.speed = speed;
        this.defense = defense;
        this.attack = attack;
        this.moves = moves;
    }

    // Getters
    public String getName() {return name;}
    public int getHp() {return hp;}
    public int getSpeed() {return speed;}
    public int getDefense() {return defense;}
    public int getAttack() {return attack;}
    public Attack[] getMoves() {return moves;}

    // Take Damage
    public void takeDamage(int damage) {
        int reducedDamage = Math.max(0, damage - this.defense);
        this.hp -= reducedDamage;
        if (this.hp <0){
            this.hp = 0; //Ensure HP doesn't go below zero
        }
        System.out.println(name + " took " + reducedDamage + " damage! HP left: " + hp);
    }

    // Use an attack
    public void useAttack(int moveIndex, Character target){
        if (moveIndex < 0 || moveIndex >= moves.length){
            System.out.println("Invalid move!");
            return;
        }
        Attack move = moves[moveIndex];
        System.out.println(name + " used " + move.getName() + "!");
        target.takeDamage(move.getDamage());
    }



    
}
