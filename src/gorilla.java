import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class gorilla {

    public static int calls = 0;
    public static void main(String[] args) {
        gorilla g = new gorilla();
        if(args.length!=2) {
            System.err.println("Necessary files not specified, exiting...");
            System.exit(1);
        }
        g.solve(args[0], args[1]).forEach(System.err::println);
    }

    public List<Result> solve(String specieFile, String costMatrixFile) {
        final List<Specie> species = speciesFromFile(specieFile);
        //final List<Specie> species = speciesFromFile("test_files/hbb.txt");
        final Map<Character, Integer> symbolMapping = someNastyCodePleaseDontLookAtThis();
        final int[][] costs;
        Map<StringTuple, Integer> memoization = new HashMap<>();
        Set<Result> results = new HashSet<>();

        try {
            costs = gorilla.readMatrixFromFile(costMatrixFile, 24);

            System.out.println(similarity(species.get(0), species.get(2), costs, symbolMapping, memoization));
//            for(Specie s1 : species) {
//                for(Specie s2 : species) {
//                    if(s1!=s2 && !results.contains(new Result(new StringTuple(s1.protein, s2.protein), 0))) {
//                        results.add(similarity(s1, s2, costs, symbolMapping, memoization));
//                    }
//                }
//            }
        } catch (FileNotFoundException e) { e.printStackTrace(); System.exit(1); }
        List<Result> r = new ArrayList<>();
        r.addAll(results);
        return r;
    }

    static Result similarity(Specie first, Specie second, int[][] costs, Map<Character, Integer> symbolMapping, Map<StringTuple, Integer> memoization) {
        StringTuple reversedAndTupled = new StringTuple(new StringBuffer(first.protein).reverse().toString(), new StringBuffer(second.protein).reverse().toString());

        Result result = similarity(reversedAndTupled, costs, symbolMapping, memoization);

        result.words.first = new StringBuffer(result.words.first).reverse().toString();
        result.words.second = new StringBuffer(result.words.second).reverse().toString();
        return result;
    }

    static Result similarity(StringTuple tuple, int[][] costs, Map<Character, Integer> symbolMapping, Map<StringTuple, Integer> memoization) {
        calls++;
        long start = System.currentTimeMillis();

        // if we've already calculated the tuple, just fetch it from the map.
        if(memoization.containsKey(tuple)) {
            calls--;
            return new Result(tuple, memoization.get(tuple));
        }
        if(calls==1) System.err.println("memo "+ (System.currentTimeMillis()-start));
        // termination checks first
        Result result;
        if(tuple.bothEmpty()) {
            result = new Result(tuple, 0);
            calls--;
            return result;
        } else if(tuple.anyEmpty()) {
            result = new Result(tuple.padLesserOne(tuple.deltaLength()), tuple.deltaLength() * -4);
            calls--;
            return result;
        } else {
            // getting the cost for this call.
            int index1 = symbolMapping.getOrDefault(tuple.first.charAt(0), 23); // 23 handles dashes.
            int index2 = symbolMapping.getOrDefault(tuple.second.charAt(0), 23);

            int cost = costs[index1][index2];
            final Result thisCall = new Result(tuple.firstChars(), cost);
            if(calls==1) System.err.println("indice "+ (System.currentTimeMillis()-start));

            // recursion.
            Result wrong = thisCall.addWith(similarity(tuple.dropBoth(1), costs, symbolMapping, memoization));
            Result firstMissing = thisCall.addWith(similarity(tuple.dropBoth(1).dashFirst(), costs, symbolMapping, memoization));
            Result secondMissing = thisCall.addWith(similarity(tuple.dropBoth(1).dashSecond(), costs, symbolMapping, memoization));
            if(calls==1) System.err.println("recusion "+ (System.currentTimeMillis()-start));

            // done
            result = Arrays.stream(new Result[]{wrong, firstMissing, secondMissing})
                    .max(Comparator.comparingInt(r -> r.score))
                    .get();

            if(calls==1) System.err.println("result "+ (System.currentTimeMillis()-start));
        }
        memoization.put(result.words, result.score);
        calls--;
        return result;
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

    //
    //
    //    KLASSER
    //
    //

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
        StringTuple words;
        int score;

        public Result(StringTuple words, int score) {
            this.words = words;
            this.score = score;
        }

        public Result addWith(Result other) {
            return new Result(  new StringTuple( this.words.first+other.words.first,
                                                this.words.second+other.words.second ),
                                this.score+other.score);
        }

        @Override
        public String toString() {
            return score +""+ '\n' + words.first +'\n'+ words.second +'\n';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            //SKITER I SCORE
            return words.equals(result.words);

        }

        @Override
        public int hashCode() {
            // SKITER I SCORE
            return words != null ? words.hashCode() : 0;
        }
    }

    static class StringTuple {
        String first;
        String second;

        public StringTuple(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public StringTuple firstChars() {
            return new StringTuple(
                        first.isEmpty() ? "" : first.substring(0,1),
                        second.isEmpty() ? "" : second.substring(0,1)
                    );
        }

        public boolean anyEmpty() {
            return first.isEmpty() || second.isEmpty();
        }

        public boolean bothEmpty() {
            return first.isEmpty() && second.isEmpty();
        }

        public boolean anyBeginsWith(String prefix) {
            return first.startsWith(prefix) || second.startsWith(prefix);
        }

        public int deltaLength() {
            return Math.abs(first.length()-second.length());
        }

        public StringTuple dashFirst() {
            return new StringTuple("-"+first, second);
        }

        public StringTuple dashSecond() {
            return new StringTuple(first, "-"+second);
        }

        public StringTuple padLesserOne(int count) {
            StringBuilder bob = new StringBuilder();
            for(int i = 0; i<count; i++) bob.append("-");

            if(first.length() > second.length()) {
                return new StringTuple(first, second+bob.toString());
            } else {
                return new StringTuple(first+bob.toString(), second);
            }
        }

        public StringTuple dropFirst(int count) {
            return new StringTuple(first.length() > count ? first.substring(count) : "", second);
        }

        public StringTuple dropSecond(int count) {
            return new StringTuple(first, second.length() > count ? second.substring(count) : "");
        }

        public StringTuple dropBoth(int count) {
            return new StringTuple(first, second).dropFirst(count).dropSecond(count);
        }
        @Override
        public String toString() {
            return "(" + first + ", " + second + ')';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringTuple that = (StringTuple) o;

            return ( this.first.equals(that.first) && this.second.equals(that.second) ) ||
                    ( this.first.equals(that.second) && this.second.equals(that.first) );
        }

        @Override
        public int hashCode() {
            int result = first != null ? first.hashCode() : 0;
            result += (second != null ? second.hashCode() : 0);
            return result;
        }
    }


    //
    //
    //    FIL-RELATERAT OCH MISC
    //
    //
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
