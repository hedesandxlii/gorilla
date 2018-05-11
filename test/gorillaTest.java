import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;


public class gorillaTest {

    @Test
    public void fineSpecieStringShouldBeParsed() throws Exception {
        String specieString = "Sphinx the 1 and only\n" +
                                "KQRK";
        gorilla.Specie s = gorilla.specieParser(specieString).orElse(null);
        Assert.assertEquals(s, new gorilla.Specie("Sphinx", "KQRK"));
    }

    @Test
    public void shiftedStringShouldFailToParse() throws Exception {
        String shiftedString = "KQRK\nBandersnatch";

        Optional<gorilla.Specie> s = gorilla.specieParser(shiftedString);
        Assert.assertNull(s.orElse(null));
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
        Assert.assertEquals(new gorilla.Specie("Human", proteinString),s);
    }

    @Test
    public void correctNumberOfSpeciesFromFile() throws Exception {
        List<gorilla.Specie> species = gorilla.speciesFromFile("test_files/HbB_FASTAs-in.txt");
        Assert.assertEquals("HbB failed", 13, species.size());
        species = gorilla.speciesFromFile("test_files/Toy_FASTAs-in.txt");
        Assert.assertEquals("Toys failed", 3, species.size());
    }

    @Test
    public void arrayReadFromFileShouldWorkPlease() throws Exception {
        int[][] test = gorilla.readMatrixFromFile("test_files/BLOSUM62.txt", 24);
        Assert.assertEquals("test[0][0] failed" , 4, test[0][0]);
        Assert.assertEquals("test[6][9] failed" , -3, test[6][9]);
        Assert.assertEquals("test[23][23] failed" , 1, test[23][23]);
    }
}
