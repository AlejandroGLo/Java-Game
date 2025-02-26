public class Battle {
    private Character player1;
    private Character player2;

    public Battle(Character player1, Character player2){
        this.player1 = player1;
        this.player2 = player2;
    }

    public void startBattle(){
        System.out.println("Battle starts between " + player1.getName() + " and " + player2.getName() + "!");
        while(player1.getHp() > 0 && player2.getHp()>0) {
            takeTurn(player1, player2);
            if (player2.getHp() <=0) break;
            takeTurn(player2, player1);

        }
        declareWinner();
    }

        private void takeTurn(Character attacker, Character defender){
            System.out.println(attacker.getName() + "'s turn!");
            attacker.useAttack(0, defender);
        }

        private void declareWinner(){
            if(player1.getHp()>0){
                System.out.println(player1.getName() + " wins!");
            } else {
                System.out.println(player2.getName() + " wins!");
            }
        
    }
    
}
