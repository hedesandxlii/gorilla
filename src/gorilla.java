import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class gorilla {

    public static int calls = 0;

    public static void main(String[] args) {
        List<Specie> species = speciesFromFile("test_files/HbB_FASTAs-in.txt");
        Map<Character, Integer> symbolMapping = someNastyCodePleaseDontLookAtThis();
        int[][] costs = new int[0][];
        try {
            costs = gorilla.readMatrixFromFile("test_files/BLOSUM62.txt", 24);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(similarity("KQRK", "KAK", costs, symbolMapping));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static Result similarity(Specie first, Specie second, int[][] costs, Map<Character, Integer> symbolMapping) {
        System.err.println("first l; "+first.protein.length());
        System.err.println("second l; "+second.protein.length());
        return similarity(  new StringBuffer(first.protein).reverse().toString(),
                            new StringBuffer(second.protein).reverse().toString(),
                            costs, symbolMapping);
    }

    static Result similarity(String first, String second, int[][] costs, Map<Character, Integer> symbolMapping) {
        if(first.isEmpty()) { // om ena strängen är tom måste vi hoppa över resten av tecknena i andra strängen.
            return new Result("", second, second.length() * -4); // avbrottsvilkor
        } else if(second.isEmpty()) {
            return new Result(first, "", first.length() * -4); // avbrottsvilkor
        }

        char lastOfFirstString = first.charAt(0);
        char lastOfSecondString = second.charAt(0);

        int indexFirst = symbolMapping.containsKey(lastOfFirstString) ? symbolMapping.get(lastOfFirstString) : 23;
        int indexSecond = symbolMapping.containsKey(lastOfSecondString) ? symbolMapping.get(lastOfSecondString) : 23;

        Result wrong = (similarity(first.substring(1), second.substring(1), costs, symbolMapping));
        wrong.score+=costs[indexFirst][indexSecond];

        Result missing1 = similarity(first, second.substring(1), costs, symbolMapping); // -4 kommer fr. std i matrisens kanter.
        missing1.score-=4;

        Result missing2 = similarity(first.substring(1), second, costs, symbolMapping);
        missing2.score-=4;

        Result[] sorted = {wrong, missing1, missing2};
        Arrays.sort(sorted, (a,b) -> Integer.compare(b.score, a.score));

        return sorted[0];
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

    static class Result {
        String first;
        String second;
        int score;

        public Result(String first, String second, int score) {
            this.first = first;
            this.second = second;
            this.score = score;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "first='" + first + '\'' +
                    ", second='" + second + '\'' +
                    ", score=" + score +
                    '}';
        }
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

            // idéen är att läsa in hela filen, splitta på strängen på ">" för att få alla specie-strängar(och den första är tom).
            String[] specieStrings = contents.split(">");

            return Arrays.stream(specieStrings)
                                .filter(s -> !s.isEmpty()) // ta bort tomma element.
                                .map(gorilla::specieParser)
                                .flatMap( o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty()) // oklart varför det ska va såhär jobbigt med flatMap.
                                .collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file, exiting...");
            System.exit(1);
        }
        return null;
    }

    static int[][] readMatrixFromFile(String fileName, int side) throws FileNotFoundException {
        int[][] result = new int[side][side];
        try(Scanner sc = new Scanner(new FileReader(fileName))) {
            int row = 0;
            while (sc.hasNextLine()) {
                String currentLine = sc.nextLine();
                // strunta i raden om den 1) börjar med # eller 2) inte innehåller något nummer(regex). Fungerar för filen iallafall.
                if(currentLine.startsWith("#") || !currentLine.matches(".*\\d+.*")) {
                    continue;
                }
                String[] split = currentLine.split("\\s+");

                for(int i = 0; i<side; i++) {
                    result[row][i] = Integer.parseInt(split[i+1]);
                }
                row++;
            }
        }
        return result;
    }

    // bara debug!
    static void printArray(int[][] array) {
        for(int[] row : array) {
            for (int element : row) {
                System.out.print("\t" + element);
            }
            System.out.println();
        }
    }

    private static Map<Character, Integer> someNastyCodePleaseDontLookAtThis() {
        Map<Character, Integer> result = new HashMap<>();
        result.put('A', 0);
        result.put('R', 1);
        result.put('N', 2);
        result.put('D', 3);
        result.put('C', 4);
        result.put('Q', 5);
        result.put('E', 6);
        result.put('G', 7);
        result.put('H', 8);
        result.put('I', 9);
        result.put('L', 10);
        result.put('K', 11);
        result.put('M', 12);
        result.put('F', 13);
        result.put('P', 14);
        result.put('S', 15);
        result.put('T', 16);
        result.put('W', 17);
        result.put('Y', 18);
        result.put('V', 19);
        result.put('B', 20);
        result.put('Z', 21);
        result.put('X', 22);
        return result;
    }
}
