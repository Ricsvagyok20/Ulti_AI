import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Stream;

public class GameState {
    //Egyenlőre csak partyit fog játszani az AI egymás ellen az lesz a test
    //Player moneyt is majd lehetne trackelni még nemtudom hogy csak legyen még egy tömb
    //vagy csináljak egy külön player osztályt
    static int NumberOfPlayers = 3;
    int numberOfHumanPlayers;
    int playerToMove;
    int tricksInRound  = 10;
    Map<Integer, List<Card>> playerHands;
    //Party számoláskor el lehet a tricksTakenByPlayerst osztani 4el
    Map<Integer, List<Card>> tricksTakenByPlayers;
    List<Pair<Integer, Card>> currentTrick;
    char trumpSuit;

    //Egyenlőre csak egy booleant idebaszok de majd később ofc a mondás alapján lesz beállítva
    boolean tenStronger = true;
    List<Card> talon;

    public GameState(int numberOfHumanPlayers, int playerToMove) {
        //Could check for bad input later
        this.numberOfHumanPlayers = numberOfHumanPlayers;
        this.playerToMove = playerToMove;
    }

    private GameState cloneState(){
        GameState gs = new GameState(this.numberOfHumanPlayers, this.playerToMove);
        gs.playerHands = this.playerHands;
        gs.tricksTakenByPlayers = this.tricksTakenByPlayers;
        gs.currentTrick = this.currentTrick;
        gs.trumpSuit = this.trumpSuit;
        gs.tenStronger = this.tenStronger;
        gs.talon = this.talon;
        return gs;
    }

    private GameState cloneAndRandomize(int currentPlayer){
        GameState gs = cloneState();
        List<Card> knownCards = playerHands.get(currentPlayer);
        knownCards.addAll(talon);
        for(int key: tricksTakenByPlayers.keySet()){
            knownCards.addAll(tricksTakenByPlayers.get(key));
        }
        List<Card> unknownCards = getCardDeck();
        unknownCards.removeAll(knownCards);
        System.out.println("Player által ismert kártyák: " + knownCards);
        System.out.println("Player által ismeretlen kártyák: " + unknownCards);
        Collections.shuffle(unknownCards);
        for(int key: playerHands.keySet()){
            if(key != currentPlayer){
                int handSize = playerHands.get(currentPlayer).size();
                gs.playerHands.put(key, unknownCards.subList(0,handSize));
                unknownCards = unknownCards.subList(handSize, unknownCards.size());
            }
        }
        System.out.println("Megkevert és randomly kiosztott player handek: " + playerHands);
        return gs;
    }

    private List<Card> getCardDeck(){
        List<Card> deck = new ArrayList<>();
        List<String> tenStrongerNames = Arrays.asList("Hét", "Nyolc", "Kilenc", "Alsó", "Felső", "Király", "Tíz", "Ász");
        List<String> kingStrongerNames = Arrays.asList("Hét", "Nyolc", "Kilenc", "Tíz", "Alsó", "Felső", "Király", "Ász");
        List<Character> types = Arrays.asList('P', 'Z', 'M', 'T');
        for(int i = 0; i < 8; i++){
            for(char j : types){
                if(tenStronger){
                    deck.add(new Card(tenStrongerNames.get(i), i, j));
                }
                else{
                    deck.add(new Card(kingStrongerNames.get(i), i, j));
                }
            }
        }
        return deck;
    }

    /**
     * Resets the game state and deals a new hand to all players
     */
    public void deal(char trumpSuit){
        this.trumpSuit = trumpSuit;
        playerHands = new HashMap<>();
        tricksTakenByPlayers = new HashMap<>();
        currentTrick = new ArrayList<>();
        List<Card> deck = getCardDeck();
        Collections.shuffle(deck);
        Random random = new Random();
        int talonNmb1 = random.nextInt(0, 33);
        int talonNmb2 = random.nextInt(0, 33);
        while(talonNmb1 == talonNmb2){
            talonNmb2 = random.nextInt(0, 33);
        }
        talon = new ArrayList<>();
        talon.add(deck.get(talonNmb1));
        talon.add(deck.get(talonNmb2));
        System.out.println(talon);
        for (int key : playerHands.keySet()) {
            playerHands.put(key, deck.subList(0, tricksInRound));
            deck = deck.subList(tricksInRound, deck.size());
        }
    }

    private void setNextPlayer(){
        if(playerToMove == 2){
            playerToMove = 0;
        }
        else{
            playerToMove++;
        }
    }

    private void doMove(Card move){
        currentTrick.add(new Pair<>(playerToMove, move));
        playerHands.get(playerToMove).remove(move);
        setNextPlayer();
        boolean endTrick = false;
        for(Pair<Integer, Card> entry: currentTrick){
            if (entry.getValue0() == playerToMove) {
                endTrick = true;
                break;
            }
        }
        if(endTrick){
            int leader = currentTrick.get(0).getValue0();
            Card leadingCard = currentTrick.get(0).getValue1();
            //Itt basically fogja az összes színt és az összes aduttat és rendezi és a végén megnézi ki nyerte a kört
            List<Pair<Integer, Card>> suitedPlays = currentTrick.stream().filter(e -> e.getValue1().type == leadingCard.type).toList();
            List<Pair<Integer, Card>> trumpPlays = currentTrick.stream().filter(e -> e.getValue1().type == trumpSuit).toList();
            suitedPlays.sort((Comparator.comparingInt(a -> a.getValue1().strength)));
            trumpPlays.sort((Comparator.comparingInt(a -> a.getValue1().strength)));
            List<Pair<Integer, Card>> sortedPlays = Stream.concat(suitedPlays.stream(),trumpPlays.stream()).toList();
            Pair<Integer, Card> trickWinner = sortedPlays.get(sortedPlays.size() - 1);
            //A winning player megkapja az ütést
            tricksTakenByPlayers.put(trickWinner.getValue0(), currentTrick.stream().map(Pair::getValue1).toList());
            currentTrick = new ArrayList<>();
            playerToMove = trickWinner.getValue0();
            if(playerHands.get(playerToMove).isEmpty()){
                System.out.println("Játék vége xy nyert majd innen meg lehet hívni a winner calculator függvényt");
                //Vhogy jelezni kéne a main fgv számára h ennek a körnek vége van
                //Esetleg egy whileban hívom a doMoveot és amikor 1-et ad vissza akk vége a gamenek számolok és
                //megkérdezem a playert hogy wanna go again
                //Majd a deal-t idk hogy innen hívjam-e meg majd vagy a mainből mert vhogy az aduttat ide kell juttatni
            }
        }
    }

}
