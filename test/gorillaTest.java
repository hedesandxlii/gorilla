import org.junit.Test;

import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;


public class gorillaTest {


    @Test
    public void correctNumberOfSpeciesFromFile() throws Exception {
        List<gorilla.Specie> species = gorilla.speciesFromFile("test_files/hbb.txt");
        assertEquals("HbB failed", 13, species.size());
        species = gorilla.speciesFromFile("test_files/toy.txt");
        assertEquals("Toys failed", 3, species.size());
    }

    @Test
    public void arrayReadFromFileShouldWorkPlease() throws Exception {
        int[][] test = gorilla.readMatrixFromFile("test_files/costs.txt", 24);
        assertEquals("test[0][0] failed" , 4, test[0][0]);
        assertEquals("test[6][9] failed" , -3, test[6][9]);
        assertEquals("test[23][23] failed" , 1, test[23][23]);
    }

    @Test
    public void stringTuplesShouldBeUndirected() throws Exception {
        assertEquals("Equals does not function",new gorilla.StringTuple("a","b"), new gorilla.StringTuple("b","a"));
        assertEquals("Hash does not function",new gorilla.StringTuple("a","b").hashCode(), new gorilla.StringTuple("b","a").hashCode());
    }

    @Test
    public void someTestsOfStringTuple() throws Exception {
        gorilla.StringTuple st = new gorilla.StringTuple("Arnold", "hej");
        assertFalse("Tuple claimed to one element empty when not", st.anyEmpty());
        assertTrue("Begins with error with dash with",st.dashFirst().dashSecond().anyBeginsWith("-"));
        assertFalse("Begins with gives false positves", st.anyBeginsWith("-"));
        assertEquals("Delta length is not right", 3, st.deltaLength());
        assertEquals("Drop is not working as intended", new gorilla.StringTuple("old", "hej"), st.dropBeginningFirst(3));
        assertEquals("The first and second has switched palce", new gorilla.StringTuple("nold", "ej"), st.dropBeginningFirst(2).dropBeginningSecond(1));
        assertEquals("Pad is fucked", "hej-", st.padLesserOne(1).second);
    }

    @Test
    public void resultsShouldAddUpNicely() throws Exception {
        gorilla.Result r1 = new gorilla.Result(new gorilla.StringTuple("Heej", "Ar"), 20);
        gorilla.Result r2 = new gorilla.Result(new gorilla.StringTuple("san", "ne"), 22);
        gorilla.Result test = new gorilla.Result(new gorilla.StringTuple("Heejsan", "Arne"), 42);
        assertEquals(test.words, r1.addWith(r2).words);
    }

    @Test
    public void wholeToyFileShouldBeCorrect() throws Exception {
        gorilla g = new gorilla();
        List<gorilla.Result> results = g.solve("test_files/Toy_FASTAs-in.txt", "test_files/BLOSUM62.txt");
        gorilla.StringTuple sphSna = new gorilla.StringTuple("KQR-------K","KQRIKAAKABK");
        gorilla.StringTuple sphBan = new gorilla.StringTuple("KQRK","K-AK");
        gorilla.StringTuple snaBan = new gorilla.StringTuple("KQRIKAAKABK","-------KA-K");

        results.sort(Comparator.comparingInt(r -> r.score));

        assertEquals("snark, bandersnatch does not match.", -18, results.get(0).score);
        assertEquals("Sphinx, Snark does not match.", -8, results.get(1).score);
        assertEquals("Sphinx, bandersnatch does not match.", 5, results.get(2).score);

        assertEquals("snark, bandersnatch does not match.", snaBan, results.get(0).words);
        assertEquals("Sphinx, Snark does not match.", sphSna, results.get(1).words);
        assertEquals("Sphinx, bandersnatch does not match.", sphBan, results.get(2).words);
    }
}
