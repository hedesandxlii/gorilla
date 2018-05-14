import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;


public class gorillaTest {

    @Test
    public void fineSpecieStringShouldBeParsed() throws Exception {
        String specieString = "Sphinx the 1 and only\n" +
                                "KQRK";
        gorilla.Specie s = gorilla.specieParser(specieString).orElse(null);
        assertEquals(s, new gorilla.Specie("Sphinx", "KQRK"));
    }

    @Test
    public void shiftedStringShouldFailToParse() throws Exception {
        String shiftedString = "KQRK\nBandersnatch";

        Optional<gorilla.Specie> s = gorilla.specieParser(shiftedString);
        assertNull(s.orElse(null));
    }

    @Test
    public void testWithLargerString() throws Exception {
        String specieString = "Human 2144721 HBHU 4HHB\n" +
                                "MVHLTPEEKSAVTALWGKVNVDEVGGEALGRLLVVYPWTQRFFESFGDLSTPDAVMGNPKVKAHGKKVLG\n" +
                                "AFSDGLAHLDNLKGTFATLSELHCDKLHVDPENFRLLGNVLVCVLAHHFGKEFTPPVQAAYQKVVAGVAN\n" +
                                "ALAHKYH\n";
        String proteinString = "MVHLTPEEKSAVTALWGKVNVDEVGGEALGRLLVVYPWTQRFFESFGDLSTPDAVMGNPKVKAHGKKVLGAFSDGLAHLDNLKGTFAT" +
                                "LSELHCDKLHVDPENFRLLGNVLVCVLAHHFGKEFTPPVQAAYQKVVAGVANALAHKYH";
        gorilla.Specie s = gorilla.specieParser(specieString).orElse(null);
        assertEquals(new gorilla.Specie("Human", proteinString),s);
    }

    @Test
    public void correctNumberOfSpeciesFromFile() throws Exception {
        List<gorilla.Specie> species = gorilla.speciesFromFile("test_files/HbB_FASTAs-in.txt");
        assertEquals("HbB failed", 13, species.size());
        species = gorilla.speciesFromFile("test_files/Toy_FASTAs-in.txt");
        assertEquals("Toys failed", 3, species.size());
    }

    @Test
    public void arrayReadFromFileShouldWorkPlease() throws Exception {
        int[][] test = gorilla.readMatrixFromFile("test_files/BLOSUM62.txt", 24);
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
        assertEquals("Drop is not working as intended", new gorilla.StringTuple("old", "hej"), st.dropFirst(3));
        assertEquals("The first and second has switched palce", new gorilla.StringTuple("nold", "ej"), st.dropFirst(2).dropSecond(1));
        assertEquals("Pad is fucked", "hej-", st.padLesserOne(1).second);
    }

    @Test
    public void resultsShouldAddUpNicely() throws Exception {
        gorilla.Result r1 = new gorilla.Result(new gorilla.StringTuple("Heej", "Ar"), 20);
        gorilla.Result r2 = new gorilla.Result(new gorilla.StringTuple("san", "ne"), 22);
        gorilla.Result test = new gorilla.Result(new gorilla.StringTuple("Heejsan", "Arne"), 42);
        assertEquals(test, r1.addWith(r2));
        assertNotSame(test, r1);
        assertNotSame(test, r2);
    }

}
