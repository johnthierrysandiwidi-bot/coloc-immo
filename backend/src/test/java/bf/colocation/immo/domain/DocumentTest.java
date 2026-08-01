package bf.colocation.immo.domain;

import static bf.colocation.immo.domain.DocumentTestSamples.*;
import static bf.colocation.immo.domain.TypeDocumentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Document.class);
        Document document1 = getDocumentSample1();
        Document document2 = new Document();
        assertThat(document1).isNotEqualTo(document2);

        document2.setId(document1.getId());
        assertThat(document1).isEqualTo(document2);

        document2 = getDocumentSample2();
        assertThat(document1).isNotEqualTo(document2);
    }

    @Test
    void typeDocumentTest() {
        Document document = getDocumentRandomSampleGenerator();
        TypeDocument typeDocumentBack = getTypeDocumentRandomSampleGenerator();

        document.setTypeDocument(typeDocumentBack);
        assertThat(document.getTypeDocument()).isEqualTo(typeDocumentBack);

        document.typeDocument(null);
        assertThat(document.getTypeDocument()).isNull();
    }
}
