public class ConDes{
      static class Example{
            private int i;
            private String name; 

            public Example() {
                  System.out.println("Default constructor is called");
            }

            public Example(int x, String y) {
                  System.out.println("Parameterised constructor is called and taking input");
                  this.i = x;
                  this.name = y;
            }

            public void display() {
                  System.out.println("Id: " + i);
                  System.out.println( "Name: " + name);
            }

            public void destroy() {
                  System.out.println("Destructor is called");
            }
      }

      public static void main(String[] args) {
            new Example().destroy();

            Example e1 = new Example(11, "XXX");
            e1.display();

            // Calling custom destructor manually at the end of e1's lifecycle
            e1.destroy();
      }
}