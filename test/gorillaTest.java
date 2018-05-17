import org.junit.Test;

import static org.junit.Assert.*;


public class gorillaTest {




    @Test
    public void arrayReadFromFileShouldWorkPlease() throws Exception {
        int[][] test = gorilla.readMatrixFromFile("BLOSUM62.txt", 24);
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
        assertEquals("Delta length is not right", 3, st.deltaLength());
        assertEquals("Drop is not working as intended", new gorilla.StringTuple("old", "hej"), st.drop(3,0));
        assertEquals("The first and second has switched palce", new gorilla.StringTuple("nold", "ej"), st.drop(2, 1));
        assertEquals("Pad is fucked", "hej---", st.padLesserOne(st.deltaLength()).second);
    }

    @Test
    public void resultsShouldAddUpNicely() throws Exception {
        gorilla.Result r1 = new gorilla.Result(new gorilla.StringTuple("Heej", "Ar"), 20);
        gorilla.Result r2 = new gorilla.Result(new gorilla.StringTuple("san", "ne"), 22);
        gorilla.Result test = new gorilla.Result(new gorilla.StringTuple("Heejsan", "Arne"), 42);
        assertEquals(test.words, r1.addWith(r2).words);
    }

}
