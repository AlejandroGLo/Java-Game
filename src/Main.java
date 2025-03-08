public class Main {
    public static void main(String[] args){

        Window window = new Window(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, Constants.SCREEN_TITLE);

        Thread thread = new Thread(window);
        thread.start();




        Attack attack1 = new Attack("Quick Strike", 20, "None");
        Attack attack2 = new Attack("Heavy Slam", 80, "User Attacks second in the next turn");


        // Assign moves to characters
        Attack[] moves1 = {attack1, attack2};
        Attack[] moves2 = {attack2, attack1};

        // Create characters
        Character char1 = new Character("Blaze", 100, 80, 50, 70, moves1);
        Character char2 = new Character("Frost", 100, 60, 70, 80, moves2);

        //Start
        Battle battle = new Battle(char1, char2);
        battle.startBattle();
    }
    
}
