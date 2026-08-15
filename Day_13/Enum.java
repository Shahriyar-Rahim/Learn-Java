public class Enum{
    enum Level{
        Low,
        Hig,
        xtreme
    }

    public static void main(String[] args) {
        Level myen = Level.xtreme;
        System.out.println(myen);

        for(Level myenn : Level.values()) {
            System.out.println(myenn);
        }
    }
}