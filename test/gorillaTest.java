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
}
