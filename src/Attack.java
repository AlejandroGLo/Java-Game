public class Attack {
    private String name;
    private int damage;
    private String effect;

    //Constructor

    public Attack(String name, int damage, String effect){
        this.name = name;
        this.damage = damage;
        this.effect = effect;
    }

    //Getters
    public String getName() { return name;}
    public int getdamage() { return damage;}
    public String getEffect() { return effect;}

    //Display attack details
    public void displayAttack(){
        System.out.println("Attack: " + name + " | Damage: " + damage + " | Effect: " + effect);
    }
    
}
