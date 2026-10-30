import java.util.Scanner;

public class Inheri {
      // Single shared scanner for the entire application
      public static final Scanner INPUT = new Scanner(System.in);

      static class Vehicle {
            protected String brand;
            protected String engine;
            protected String engineType;

            //usiong scanner to take input
            public void getBrand(Scanner cin) {
                  System.out.print("ENter the brand name: ");
                   this.brand = cin.nextLine();
            }

            // using only terminal input using System.console().readLine("String here");
            public void getEngine() {
                  System.out.println("\n \n using System.console().readline()");
                  this.engine = System.console().readLine("Input engine name: ");
            }

            // using single shared scanner
            public void getEngineType() {
                  System.out.print("Enter engine type: ");
                  this.engineType = Inheri.INPUT.nextLine();
            }
      }

      static class Car extends Vehicle {
            public void display() {
                  System.out.println("\n \n");
                  System.out.println("Brand name from Vehicle class: " + brand);
                  System.out.println("Engine Name: " + engine);
                  System.out.println("Engine Type: " + engineType);
            }
      }

      public static void main(String[] args) {
            Car c = new Car();
            Scanner s = new Scanner(System.in);
            c.getBrand(s);
            c.getEngine();
            c.getEngineType();
            c.display();
      }
}
