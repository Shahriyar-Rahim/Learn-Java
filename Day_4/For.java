public class For{
      public static void main(String[] args){
            for (int i = 1; i <= 2; i++) {
                  System.out.println("Outer: " + i);

                  for ( int j = 0; j < 3; j++) {
                        System.out.println(" Innter: " + j);
                  }
            }
      }
}