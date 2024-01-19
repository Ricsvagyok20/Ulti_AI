public class Card {
    String name;
    int strength;
    char type;

    public Card(String name, int strength, char type) {
        this.name = name;
        this.strength = strength;
        this.type = type;
    }

    @Override
    public String toString() {
        //Innen majd később ki lehet venni a strengthet meg vhogy át kéne alakítani olvashatóbbra amíg konzolos
        return "Card{" +
                "name='" + name + '\'' +
                ", strength=" + strength +
                ", type=" + type +
                '}';
    }
}
