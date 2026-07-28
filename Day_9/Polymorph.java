public class Polymorph {
      static class Animal {
            public void animalSound() {
                  System.out.println("ANimal SOund");
            }
      }

      static class Dog extends Animal {
            public void animalSound() {
                  super.animalSound();
                  System.out.println("DOg sdfgnb");
            }
      }

            public static void main(String[] args) {
                  Dog myDog = new Dog();
                  myDog.animalSound();
            }
}