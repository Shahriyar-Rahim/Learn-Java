public class VarNdata{
      public static void main(String[] args) {
            String armorname = "Mark 26";
            Float version = 0.01f;
            Double weight = 100.4534653456453;
            Character pCategory = 'A';
            String tech = "Nano-Tech";
            int missile_count = 100;

            // printing a variables data oonly
            System.out.println("Armor Name: " + armorname );

            // now printing the all vars using Object ( which is a parent class of all data types in JAVA)
            Object[] data = { armorname, version, weight, pCategory, tech, missile_count};
            for (int i = 0; i < data.length; i++) {
                  System.out.println(data[i]);
            }
      }
}
