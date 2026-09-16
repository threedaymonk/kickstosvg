package uk.sanshinkai.kickstosvg;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.colston.kicks.document.KicksDocument;
import org.colston.kicks.document.persistence.DocumentStoreFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ColumnizerTest {
    Path tempDir;

    ColumnizerTest(@TempDir Path tempDir) {
        this.tempDir = tempDir;
    }

    @Test
    public void testMapsToColumns() throws Exception {
        var kicksabc =
            """
            T:タイトル
            E:Title
            K:honchoshi

            10 11 12 13 20 21 22 23 30 31 32 33 34 35 36
            """;
        var options = new Options();
        var document = docFromString(kicksabc);
        var columns = new Columnizer(options).columnize(document);
        assertEquals(3, columns.size());
        assert(columns.get(0).isTitle());
        assert(columns.get(1).isMusic());
        assert(columns.get(2).isMusic());
    }

    @Test
    public void omitsTitleIfSet() throws Exception {
        var kicksabc =
            """
            T:タイトル
            E:Title
            K:honchoshi

            10 11 12 13 20 21 22 23 30 31 32 33 34 35 36
            """;
        var options = new Options();
        options.setShowTitles(false);
        var document = docFromString(kicksabc);
        var columns = new Columnizer(options).columnize(document);
        assertEquals(2, columns.size());
        assert(columns.get(0).isMusic());
        assert(columns.get(1).isMusic());
    }

    private KicksDocument docFromString(String str) throws Exception {
        var path = tempDir.resolve("testfile.kicksabc");
        Files.writeString(path, str);
        var file = path.toFile();
        return DocumentStoreFactory.create(file).get().load(file);
    }
}
