import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.*;

public class gorilla {

    public static void main(String[] args) {
        if(args.length!=1) {
            System.err.println("Necessary files not specified as arguments, exiting...");
            System.exit(1);
        }
        File f = new File(".");
        File[] matchingFiles = f.listFiles(new FilenameFilter() {
                                               @Override
                                               public boolean accept(File dir, String name) {
                                                   return name.startsWith("BLOSUM62.txt");
                                               }
                                           });
        gorilla g = new gorilla();
        g.solve(args[0], matchingFiles[0].getName());
    }

    public void solve(String specieFile, String costMatrixFile) {
        long start = System.currentTimeMillis();
        final Map<Character, Integer> symbolMapping = someNastyCodePleaseDontLookAtThis();

        final Map<String, Specie> species = new HashMap<>();
        final Queue<StringTuple> comparisons = new LinkedList<>();

        readFileAndGetTheGoodStuff(specieFile, species, comparisons);

        try {
            final int[][] costs = gorilla.readMatrixFromFile(costMatrixFile, 24);

            while (comparisons.peek() != null){
                StringTuple current = comparisons.poll();

                Result r = similarity(species.get(current.first), species.get(current.second), costs, symbolMapping);
                System.out.println(current.first +"--"+ current.second +": "+ r.score);
                //System.out.println(r);
            }
        } catch (FileNotFoundException e) { e.printStackTrace(); System.exit(1); }
        System.err.println(System.currentTimeMillis()-start);
    }

    static Result similarity(Specie first, Specie second, int[][] costs, Map<Character, Integer> symbolMapping) {
        StringTuple reversedAndTupled = new StringTuple(new StringBuffer(first.protein).reverse().toString(), new StringBuffer(second.protein).reverse().toString());
        int[][] memoization = new int[first.protein.length()+1][second.protein.length()+1];

        for(int i = 0; i<memoization.length; i++) {
            for(int j = 0; j<memoization[0].length; j++) {
                if(i == 0 && j == 0)
                    memoization[0][0] = 0;
                else if(i == 0)
                    memoization[0][j] = -4*j;
                else if(j == 0)
                    memoization[i][0] = -4*i;
                else
                    memoization[i][j] = Integer.MIN_VALUE;
            }
        }

        Result result = similarity(reversedAndTupled, costs, symbolMapping, memoization, first.protein.length(), second.protein.length());

        result.words.first  = new StringBuffer(result.words.first).reverse().toString();
        result.words.second = new StringBuffer(result.words.second).reverse().toString();


        return result;
    }

    private static Result similarity(StringTuple tuple, int[][] costs, Map<Character, Integer> symbolMapping, int[][] memoization, int x, int y) {
        if(x == 0) {
            return new Result(tuple.padLesserOne(y), memoization[x][y]);
        } else if(y == 0) {
            return new Result(tuple.padLesserOne(x), memoization[x][y]);
        } else if(memoization[x][y] != Integer.MIN_VALUE) {
            return new Result(tuple, memoization[x][y]);
        }

        char first = tuple.clean().first.charAt(0);
        char second = tuple.clean().second.charAt(0);

        int index1 = symbolMapping.getOrDefault(first, 23);
        int index2 = symbolMapping.getOrDefault(second, 23);

        int cost = costs[index1][index2];

        // recursion.
        Result wrong         = new Result(tuple.firstChars(), cost);
        wrong = wrong.addWith(similarity(tuple.dropBoth(1), costs, symbolMapping, memoization, x-1, y-1));

        Result firstMissing  = new Result(new StringTuple("-", tuple.firstChars().second), -4);
        firstMissing = firstMissing.addWith(similarity(tuple.dropSecond(1), costs, symbolMapping, memoization, x, y-1));

        Result secondMissing = new Result(new StringTuple(tuple.firstChars().first, "-"), -4);
        secondMissing = secondMissing.addWith(similarity(tuple.dropFirst(1), costs, symbolMapping, memoization, x-1, y));

        // done
        Result result = Arrays.stream(new Result[]{wrong, firstMissing, secondMissing})
                .max(Comparator.comparingInt(r -> r.score))
                .get();


        memoization[x][y] = result.score;
        return result;
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

        public StringTuple cleanStrings() {
            String first = words.first.replace("-", "");
            String second = words.second.replace("-", "");
            return new StringTuple(first, second);
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

        public StringTuple clean() {
            String first = this.first.replace("-","");
            String second = this.second.replace("-","");
            return new StringTuple(first, second);
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
    static void readFileAndGetTheGoodStuff(String fileName, Map<String, Specie> fillWithSpecies, Queue<StringTuple> fillWithComparisons) {
        try(Scanner sc = new Scanner(new FileReader(fileName))) {
            int noSpecies = 0;
            int noComparisons = 0;
            // the regex: a number - some spaces - another number
            String[] split = sc.nextLine().split("\\s+");
            noSpecies = Integer.parseInt(split[0]);
            noComparisons = Integer.parseInt(split[1]);

            System.err.println("species: " + noSpecies +"\n"+ "comps: " + noComparisons);
            for (int i = 0; i<noSpecies; i++) {
                String name = sc.nextLine();
                fillWithSpecies.put(name, new Specie(name, sc.nextLine())); // first sc.nextLine() != second sc.nextLine().
            }
            for (int i = 0; i<noComparisons; i++) {
                split = sc.nextLine().split("\\s+");
                if (split[0].startsWith("#")) continue;
                fillWithComparisons.offer(new StringTuple(split[0], split[1]));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not find input-file), exiting...");
            System.exit(1);
        }
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

    static void printArray(int[][] array) {
        for(int[] row : array) {
            for (int element : row) {
                System.out.print( "\t" + (element == Integer.MIN_VALUE ? "-" : element) );
            }
            System.out.println();
        }
        System.out.println();
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
