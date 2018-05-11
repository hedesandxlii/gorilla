import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class gorilla {
    public static void main(String[] args) {
        speciesFromFile("test_files/HbB_FASTAs-in.txt").forEach(System.out::println);
    }

    /**
     * Returns a list of parsed species from a said file. Will return an empty list if nothing in the file was parsed.
     * @param fileName
     * @return
     */
    static List<Specie> speciesFromFile(String fileName) {
        try(Scanner sc = new Scanner(new FileReader(fileName))) {
            StringJoiner bob = new StringJoiner("\n");
            while(sc.hasNextLine()) {
                bob.add(sc.nextLine());
            }
            String contents = bob.toString();

            // idéen är att läsa in hela filen, splitta på ">" för att få alla specie-strängar(och den första är tom).
            String[] specieStrings = contents.split(">");

            return Arrays.stream(specieStrings)
                                .map(gorilla::specieParser)
                                .flatMap( o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty()) // oklart varför det ska va såhär jobbigt med flatMap.
                                .filter(specie -> !specie.name.isEmpty() && !specie.protein.isEmpty())
                                .collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file, exiting...");
            System.exit(1);
        }
        return null;
    }

    static Optional<Specie> specieParser(String specieString) {
        String[] lines = specieString.split("\n");

        // gets the name from the first line.
        String name = lines[0].split("\\s+")[0];

        // joins the rest of the lines to get the protein string.
        StringJoiner sj = new StringJoiner("");
        for(int i = 1; i<lines.length; i++) {
            sj.add(lines[i]);
        }

        // checks if the protein string contains any characters other than capitals, in which case its not a protein string.
        String protein = sj.toString();
        for(char c : protein.toCharArray()) {
            if(!Character.isUpperCase(c)) {
                return Optional.empty();
            }
        }

        return Optional.of(new Specie(name, protein));
    }

    static class Specie {
        String name;
        String protein;

        public Specie(String name, String protein) {
            this.name = name;
            this.protein = protein;
        }

        @Override
        public String toString() {
            return "Specie{" +
                    "name='" + name + '\'' +
                    ", protein='" + protein + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Specie specie = (Specie) o;

            if (name != null ? !name.equals(specie.name) : specie.name != null) return false;
            return protein != null ? protein.equals(specie.protein) : specie.protein == null;

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (protein != null ? protein.hashCode() : 0);
            return result;
        }
    }

}
