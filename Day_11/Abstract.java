public class Abstract {
    abstract static class Animal {
        public abstract void animalSOund();

        public void sleep() {
            System.out.println("skdnfb");
        }
    }

    static class Cow extends Animal {
        @Override
        public void animalSOund() {
            System.out.println("askdlfgb");
        }
    }

    public static void main(String[] args) {
        Cow myCow = new Cow();
        myCow.animalSOund();
        myCow.sleep();
    }
}