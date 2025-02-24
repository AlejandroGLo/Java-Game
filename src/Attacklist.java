import java.util.ArrayList;
import java.util.List;

public class Attacklist {
    private static List<Attack> attacks = new ArrayList<>();
    static {
        attacks.add(new Attack("Quick Strike", 20, "None"));
        attacks.add(new Attack("Heavy Slam", 80, "User attacks second in the next turn"));
        attacks.add(new Attack("Flame burst", 60, "30% chance to burn opponent"));
        attacks.add(new Attack("Shockwave", 35, "50% chance to paralyze opponent"));

    }

    public static List<Attack> getAttacks(){
        return attacks;
    }
    
}
