public class Interface{
    static interface Animal{
        public void animalSOund();
        public void sleep();
    }

    static class Cow implements Animal{
        public void animalSOund() {
            System.out.println("aksdfbg");
        }
        public void sleep() {
            System.out.println("aksdfrbv");
        }
    }
        public static void main(String[] args) {
            Cow mc = new Cow();
            mc.animalSOund();;
            mc.sleep();
    }
}